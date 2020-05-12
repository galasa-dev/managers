/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.spi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos3270.AttentionIdentification;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.IScreenUpdateListener;
import dev.galasa.zos3270.IScreenUpdateListener.Direction;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.datastream.AbstractCommandCode;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.AbstractQueryReply;
import dev.galasa.zos3270.internal.datastream.BufferAddress;
import dev.galasa.zos3270.internal.datastream.CommandEraseWrite;
import dev.galasa.zos3270.internal.datastream.CommandReadBuffer;
import dev.galasa.zos3270.internal.datastream.CommandWriteStructured;
import dev.galasa.zos3270.internal.datastream.IAttribute;
import dev.galasa.zos3270.internal.datastream.OrderInsertCursor;
import dev.galasa.zos3270.internal.datastream.OrderRepeatToAddress;
import dev.galasa.zos3270.internal.datastream.OrderSetAttribute;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.internal.datastream.OrderStartFieldExtended;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.QueryReplyCharactersets;
import dev.galasa.zos3270.internal.datastream.QueryReplyImplicitPartition;
import dev.galasa.zos3270.internal.datastream.QueryReplyNull;
import dev.galasa.zos3270.internal.datastream.QueryReplySummary;
import dev.galasa.zos3270.internal.datastream.QueryReplyUsableArea;
import dev.galasa.zos3270.internal.datastream.StructuredField;
import dev.galasa.zos3270.internal.datastream.StructuredField3270DS;
import dev.galasa.zos3270.internal.datastream.StructuredFieldReadPartition;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.internal.terminal.ScreenUpdateTextListener;

/**
 * Screen representation of the 3270 terminal
 * 
 * @author Michael Baylis
 *
 */
public class Screen {

    private static final String                     CANT_FIND_TEXT  = "Unable to find a field containing '";

    private final Log                               logger          = LogFactory.getLog(getClass());

    private final Network                           network;

    private final IBufferHolder[]                   buffer;
    private final int                               columns;
    private final int                               rows;
    private final int                               screenSize;

    private int                                     workingCursor   = 0;
    private int                                     screenCursor    = 0;

    private Semaphore                               keyboardLock    = new Semaphore(1, true);
    private boolean                                 keyboardLockSet = false;

    private final LinkedList<IScreenUpdateListener> updateListeners = new LinkedList<>();

    public Screen() throws TerminalInterruptedException {
        this(80, 24, null);
    }

    public Screen(int columns, int rows, Network network) throws TerminalInterruptedException {
        this.network = network;
        this.columns = columns;
        this.rows = rows;
        this.screenSize = this.columns * this.rows;
        this.buffer = new IBufferHolder[this.screenSize];
        lockKeyboard();
    }

    private synchronized void lockKeyboard() throws TerminalInterruptedException {
        if (!keyboardLockSet) {
            logger.trace("Locking keyboard");
            keyboardLockSet = true;
            try {
                keyboardLock.acquire();
            } catch (InterruptedException e) {
                throw new TerminalInterruptedException("Lock keyboard was interrupted", e);
            }
        }
    }

    private synchronized void unlockKeyboard() {
        if (keyboardLockSet) {
            logger.trace("Unlocking keyboard");
            keyboardLockSet = false;
            keyboardLock.release();
        }
    }

    public void processInboundMessage(Inbound3270Message inbound) throws DatastreamException {
        AbstractCommandCode commandCode = inbound.getCommandCode();
        if (commandCode instanceof CommandWriteStructured) {
            processStructuredFields(inbound.getStructuredFields());
        } else if (commandCode instanceof CommandReadBuffer) {
            processReadBuffer();
        } else {
            WriteControlCharacter writeControlCharacter = inbound.getWriteControlCharacter();
            List<AbstractOrder> orders = inbound.getOrders();

            if (commandCode instanceof CommandEraseWrite) {
                erase();
            }
            processOrders(orders);

            if (writeControlCharacter.isKeyboardReset()) {
                unlockKeyboard();
            }
        }

    }
    
    private synchronized void processReadBuffer() throws DatastreamException {
        try {
            ByteArrayOutputStream outboundBuffer = new ByteArrayOutputStream();

            outboundBuffer.write(AttentionIdentification.ENTER.getKeyValue());

            BufferAddress cursor = new BufferAddress(this.screenCursor);
            outboundBuffer.write(cursor.getCharRepresentation());
            
            for(IBufferHolder bh : this.buffer) {
                if (bh == null) {
                    outboundBuffer.write(0);
                } else if (bh instanceof BufferChar) {
                    BufferChar bc = (BufferChar) bh;
                    outboundBuffer.write(bc.getChar());
                } else if (bh instanceof BufferStartOfField) {
                    BufferStartOfField sf = (BufferStartOfField) bh;
                    OrderStartField osf = new OrderStartField(sf.isProtected(), sf.isNumeric(), sf.isDisplay(), sf.isIntenseDisplay(), sf.isSelectorPen(), sf.isFieldModifed());
                    outboundBuffer.write(osf.getBytes());
                } else {
                    throw new DatastreamException("Unrecognised Buffer Holder - " + bh.getClass().getName());
                }
            }
            this.network.sendDatastream(outboundBuffer.toByteArray());
        } catch(Exception e) {
            throw new DatastreamException("Error whilst processing READ BUFFER", e);
        }
        
    }

    private synchronized void processStructuredFields(List<StructuredField> structuredFields)
            throws DatastreamException {
        for (StructuredField structuredField : structuredFields) {
            if (structuredField instanceof StructuredFieldReadPartition) {
                processReadPartition((StructuredFieldReadPartition) structuredField);
            } else if (structuredField instanceof StructuredField3270DS) {
                processInboundMessage(((StructuredField3270DS) structuredField).getInboundMessage());
            } else {
                throw new DatastreamException("Unsupported Structured Field - " + structuredField.getClass().getName());
            }
        }
    }

    private synchronized void processReadPartition(StructuredFieldReadPartition readPartition)
            throws DatastreamException {
        switch (readPartition.getType()) {
            case QUERY:
                processReadPartitionQuery();
                return;
            case QUERY_LIST:
                processReadPartitionQueryList(readPartition);
                return;
            default:
                throw new DatastreamException(
                        "Unsupported Read Partition Type - " + readPartition.getType().toString());
        }

    }

    private synchronized void processReadPartitionQuery() throws DatastreamException {
        List<AbstractQueryReply> replies = getAllSupportedReplies();
        QueryReplySummary summary = new QueryReplySummary(replies);

        sendQueryReplies(summary, replies);
    }

    private synchronized void processReadPartitionQueryList(StructuredFieldReadPartition readPartition) throws DatastreamException {
        switch (readPartition.getRequestType()) {
            case StructuredFieldReadPartition.REQTYP_LIST:
                List<AbstractQueryReply> supportedReplies = getAllSupportedReplies();
                ArrayList<AbstractQueryReply> replies = prepareQueryListResponse(supportedReplies, readPartition.getQcodes());

                sendQueryReplies(new QueryReplySummary(supportedReplies), replies);
                return;
            case StructuredFieldReadPartition.REQTYP_ALL:
            case StructuredFieldReadPartition.REQTYP_EQUIVALENT:
                processReadPartitionQuery();
                return;
            default:
                throw new DatastreamException(
                        "Unsupported Read Partition Request Type code = " + readPartition.getRequestType());
        }
    }

    private ArrayList<AbstractQueryReply> prepareQueryListResponse(List<AbstractQueryReply> supportedReplies, Set<Byte> requestedQcodes) {
        ArrayList<AbstractQueryReply> replies = new ArrayList<>();
        for (AbstractQueryReply reply : supportedReplies) {
            if (requestedQcodes.contains(reply.getID())) {
                replies.add(reply);
            }
        }
        if (replies.isEmpty()) {
            replies.add(new QueryReplyNull());
        }
        return replies;
    }

    private List<AbstractQueryReply> getAllSupportedReplies() {
        ArrayList<AbstractQueryReply> replies = new ArrayList<>();

        replies.add(new QueryReplyUsableArea(this));
        replies.add(new QueryReplyImplicitPartition(this));
        replies.add(new QueryReplyCharactersets());
        return replies;
    }

    private void sendQueryReplies(QueryReplySummary summary, List<AbstractQueryReply> replies) throws DatastreamException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(AttentionIdentification.STRUCTURED_FIELD.getKeyValue());
            baos.write(summary.toByte());

            for (AbstractQueryReply reply : replies) {
                baos.write(reply.toByte());
            }

            String hex = new String(Hex.encodeHex(baos.toByteArray()));
            logger.trace("outbound sf=" + hex);

            network.sendDatastream(baos.toByteArray());
        } catch (Exception e) {
            throw new DatastreamException("Unable able to write Query Reply", e);
        }
    }

    public synchronized void processOrders(List<AbstractOrder> orders) throws DatastreamException {
        logger.trace("Processing orders");
        //        this.workingCursor = 0;
        for (AbstractOrder order : orders) {
            if (order instanceof OrderSetBufferAddress) {
                processSBA((OrderSetBufferAddress) order);
            } else if (order instanceof OrderRepeatToAddress) {
                processRA((OrderRepeatToAddress) order);
            } else if (order instanceof OrderText) {
                processText((OrderText) order);
            } else if (order instanceof OrderStartField) {
                processSF((OrderStartField) order);
            } else if (order instanceof OrderStartFieldExtended) {
                processSFE((OrderStartFieldExtended) order);
            } else if (order instanceof OrderSetAttribute) {
                processSA((OrderSetAttribute) order);
            } else if (order instanceof OrderInsertCursor) {
                this.screenCursor = this.workingCursor;
            } else {
                throw new DatastreamException("Unsupported Order - " + order.getClass().getName());
            }
        }

        synchronized (updateListeners) {
            for (IScreenUpdateListener listener : updateListeners) {
                listener.screenUpdated(Direction.RECEIVED, null);
            }
        }
    }

    public synchronized void erase() {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = null;
        }

        this.screenCursor  = 0;
        this.workingCursor = 0;
    }

    /**
     * Process a Set Buffer Address order
     * 
     * @param order - the order to process
     */
    private synchronized void processSBA(OrderSetBufferAddress order) {
        this.workingCursor = order.getBufferAddress();
        if (this.workingCursor >= this.screenSize) {
            this.workingCursor = this.workingCursor - this.screenSize;
        }
    }

    /**
     * Process the Report to Address order
     * 
     * @param order - the order to process
     * @throws DatastreamException
     */
    private synchronized void processRA(OrderRepeatToAddress order) throws DatastreamException {
        int endOfRepeat = order.getBufferAddress();

        if (endOfRepeat > this.screenSize || endOfRepeat < 0) {
            throw new DatastreamException(
                    "Impossible RA end address " + endOfRepeat + ", screen size is " + screenSize);
        }

        while (this.workingCursor != endOfRepeat) {
            this.buffer[this.workingCursor] = new BufferChar(order.getChar());
            if (endOfRepeat == this.screenSize && this.workingCursor == (this.screenSize - 1)) {
                endOfRepeat = 0;
                break;
            }
            incrementWorkingCursor();
        }

        this.workingCursor = endOfRepeat;
    }

    private void incrementWorkingCursor() {
        this.workingCursor++;
        if (this.workingCursor >= this.screenSize) {
            this.workingCursor = 0;
        }
    }

    private void processSF(OrderStartField order) {
        this.buffer[this.workingCursor] = new BufferStartOfField(this.workingCursor, order.isFieldProtected(),
                order.isFieldNumeric(), order.isFieldDisplay(), order.isFieldIntenseDisplay(),
                order.isFieldSelectorPen(), order.isFieldModifed());
        incrementWorkingCursor();
    }

    private void processSFE(OrderStartFieldExtended order) {
        List<IAttribute> attributes = order.getAttributes();

        BufferStartOfField bsf = null;
        for (IAttribute attr : attributes) {
            if (attr instanceof OrderStartField) {
                OrderStartField sf = (OrderStartField) attr;
                bsf = new BufferStartOfField(this.workingCursor, sf.isFieldProtected(), sf.isFieldNumeric(),
                        sf.isFieldDisplay(), sf.isFieldIntenseDisplay(), sf.isFieldSelectorPen(), sf.isFieldModifed());
            }
            // TODO add processing for character attributes
        }

        if (bsf == null) {
            bsf = new BufferStartOfField(this.workingCursor, false, false, true, false, false, false);
        }

        this.buffer[this.workingCursor] = bsf;
        incrementWorkingCursor();
    }

    private void processSA(OrderSetAttribute order) {
        // TODO add processing for character attributes
    }

    private void processText(OrderText order) {
        String text = order.getText();
        for (int i = 0; i < text.length(); i++) {
            this.buffer[this.workingCursor] = new BufferChar(text.charAt(i));
            incrementWorkingCursor();
        }

    }

    public String printScreen() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.buffer.length; i++) {
            if (this.buffer[i] == null) {
                sb.append(" ");
            } else {
                sb.append(this.buffer[i].getStringWithoutNulls());
            }
        }

        StringBuilder screenSB = new StringBuilder();
        String screenString = sb.toString();
        for (int i = 0; i < this.screenSize; i += this.columns) {
            screenSB.append(screenString.substring(i, i + this.columns));
            screenSB.append('\n');
        }
        return screenSB.toString();
    }

    public String printScreenTextWithCursor() {
        int cursorRow = screenCursor / columns;
        int cursorCol = screenCursor % columns;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.buffer.length; i++) {
            if (this.buffer[i] == null) {
                sb.append(" ");
            } else {
                sb.append(this.buffer[i].getStringWithoutNulls());
            }
        }

        StringBuilder screenSB = new StringBuilder();
        String screenString = sb.toString();
        int row = 0;
        for (int i = 0; i < this.screenSize; i += this.columns) {
            screenSB.append("=|");
            screenSB.append(screenString.substring(i, i + this.columns));
            screenSB.append("|");
            screenSB.append('\n');
            if (row == cursorRow) {
                screenSB.append("^|");
                for (int j = 0; j < cursorCol; j++) {
                    screenSB.append(" ");
                }
                screenSB.append("^");
                screenSB.append('\n');
            }

            row++;
        }
        return screenSB.toString();
    }

    public String retrieveFlatScreen() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.buffer.length; i++) {
            if (this.buffer[i] == null) {
                sb.append(" ");
            } else {
                sb.append(this.buffer[i].getStringWithoutNulls());
            }
        }
        return sb.toString();
    }



    public synchronized @NotNull Field[] calculateFields() {
        ArrayList<Field> fields = new ArrayList<>();

        Field currentField = null;

        // *** Check to see if the screen is wrapped or unformatted
        if (!(this.buffer[0] instanceof BufferStartOfField)) {
            BufferStartOfField wrapSoField = null;
            for (int i = this.buffer.length - 1; i >= 0; i--) {
                IBufferHolder bh = this.buffer[i];
                if (bh instanceof BufferStartOfField) {
                    wrapSoField = (BufferStartOfField) bh;
                    break;
                }
            }

            if (wrapSoField == null) {
                currentField = new Field();
            } else {
                currentField = new Field(-1, wrapSoField);
            }
        }

        for (int i = 0; i < this.buffer.length; i++) {
            IBufferHolder bh = this.buffer[i];
            if (bh == null) {
                currentField.appendChar((char) 0x00); // NOSONAR, can't be null
            } else if (bh instanceof BufferStartOfField) {
                if (currentField != null) {
                    fields.add(currentField);
                }
                currentField = new Field(i, (BufferStartOfField) bh);
            } else if (bh instanceof BufferChar) {
                currentField.appendChar(((BufferChar) bh).getChar());// NOSONAR, can't be null
            } else {
                throw new UnsupportedOperationException("Unrecognised buffer type " + bh.getClass().getName());
            }
        }
        if (currentField != null) {
            fields.add(currentField);
        }

        // *** If the SBA were not in order, possibility that the safeguard first field
        // was left there
        if (fields.size() >= 2) {
            if (fields.get(0).getStart() == 0 && fields.get(1).getStart() == 0) {
                fields.remove(0);
            }
        }

        return fields.toArray(new Field[fields.size()]);
    }

    public void searchFieldContaining(String text) throws TextNotFoundException {
        for (Field field : calculateFields()) {
            if (field.containsText(text)) {
                return;
            }
        }

        throw new TextNotFoundException(CANT_FIND_TEXT + text + "'");
    }

    public boolean isTextInField(String text) {
        for (Field field : calculateFields()) {
            if (field.containsText(text)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Wait on the keyboard being free
     * 
     * @param maxWait - time in milliseconds
     * @throws KeyboardLockedException
     * @throws InterruptedException
     */
    public void waitForKeyboard(int maxWait) throws TimeoutException, TerminalInterruptedException {
        try {
            if (!keyboardLock.tryAcquire(maxWait, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException("Wait for keyboard took longer than " + maxWait + "ms");
            }
        } catch(InterruptedException e) {
            throw new TerminalInterruptedException("Wait for keyboard was interrupted", e);
        }
        keyboardLock.release();
    }

    public void waitForTextInField(String text, int maxWait) throws TerminalInterruptedException, Zos3270Exception {
        try {
            if (!ScreenUpdateTextListener.waitForText(this, text, maxWait)) {
                throw new TextNotFoundException(CANT_FIND_TEXT + text + "'");
            }
        } catch(InterruptedException e) {
            throw new TerminalInterruptedException("Wait for text was interrupted", e);
        }
    }

    public synchronized void positionCursorToFieldContaining(@NotNull String text)
            throws KeyboardLockedException, TextNotFoundException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        for (Field field : calculateFields()) {
            if (field.containsText(text)) {
                this.screenCursor = field.getStart();
                return;
            }
        }

        throw new TextNotFoundException(CANT_FIND_TEXT + text + "'");
    }

    public synchronized void eraseEof() throws KeyboardLockedException, FieldNotFoundException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        if (buffer[screenCursor] != null && !(buffer[screenCursor] instanceof BufferChar)) {
            throw new FieldNotFoundException("Unable to type where the cursor is pointing to - " + this.screenCursor);
        }

        BufferStartOfField sf = null;
        int sfPos = screenCursor - 1;
        if (sfPos < 0) {
            sfPos = buffer.length - 1;
        }
        while(sfPos != screenCursor) {
            if (buffer[sfPos] instanceof BufferStartOfField) {
                sf = (BufferStartOfField) buffer[sfPos];
                break;
            }

            sfPos--;
            if (sfPos < 0) {
                sfPos = buffer.length - 1;
            }
        }

        // *** if no field found, assume unprotected
        if (sf != null && sf.isProtected()) {
            throw new FieldNotFoundException("Unable to type where the cursor is pointing to - " + screenCursor);
        }

        //*** Set this and following characters to null
        int pos = this.screenCursor;
        while(true) {
            if (!(buffer[pos] instanceof BufferChar)) {
                break;
            }

            buffer[pos] = new BufferChar((char) 0);
            pos++;
            if (pos >= this.screenSize) {
                pos = 0;
            }

            if (pos == this.screenCursor) {
                break;
            }
        }


    }


    public synchronized void tab() throws KeyboardLockedException, FieldNotFoundException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        Field[] fields = calculateFields();

        int fieldPosition = 0;
        Field startField = null;
        for (; fieldPosition < fields.length; fieldPosition++) {
            Field field = fields[fieldPosition];
            if (field.containsPosition(this.screenCursor)) {
                startField = field;
                break;
            }
        }

        if (startField == null) {
            throw new FieldNotFoundException("Unable to locate field to tab from, should not have happened");
        }

        if (screenCursor == startField.getStart() && !startField.isProtected()) {
            this.screenCursor = startField.getStart() + 1;
            return;
        }

        while (true) {
            fieldPosition++;
            if (fieldPosition >= fields.length) {
                fieldPosition = 0;
            }

            Field field = fields[fieldPosition];
            if (!field.isProtected()) {
                this.screenCursor = field.getStart() + 1;
                return;
            }

            if (field == startField) {
                throw new FieldNotFoundException("Unable to locate an unprotected field to tab to");
            }
        }

    }

    public synchronized void cursorUp() throws KeyboardLockedException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        this.screenCursor = this.screenCursor - this.columns;
        if (this.screenCursor < 0) {
            this.screenCursor = this.screenSize - this.screenCursor;
        }
    }

    public synchronized void cursorDown() throws KeyboardLockedException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        this.screenCursor = this.screenCursor + this.columns;
        if (this.screenCursor >= this.screenSize) {
            this.screenCursor = this.screenCursor - this.screenSize;
        }
    }

    public synchronized void cursorLeft() throws KeyboardLockedException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        this.screenCursor--;
        if (this.screenCursor < 0) {
            this.screenCursor = this.screenSize - this.screenCursor;
        }
    }

    public synchronized void cursorRight() throws KeyboardLockedException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        this.screenCursor++;
        if (this.screenCursor >= this.screenSize) {
            this.screenCursor = this.screenCursor - this.screenSize;
        }

    }

    public synchronized void home() throws KeyboardLockedException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to move cursor as keyboard is locked");
        }

        Field[] fields = calculateFields();

        if (fields == null || fields.length == 0) {
            this.screenCursor = 0;
            return;
        }

        //*** find first unprotected field
        for(Field field : fields) {
            if (!field.isProtected()) {
                if (field.isUnformatted()) {
                    this.screenCursor = 0;
                    return;
                }

                this.screenCursor = field.getStart() + 1;
                return;
            }
        }

        this.screenCursor = 0;
        return;
    }


    public int getNoOfColumns() {
        return this.columns;
    }

    public int getNoOfRows() {
        return this.rows;
    }

    public synchronized void registerScreenUpdateListener(IScreenUpdateListener listener) {
        synchronized (updateListeners) {
            updateListeners.add(listener);
        }
    }

    public synchronized void unregisterScreenUpdateListener(IScreenUpdateListener listener) {
        synchronized (updateListeners) {
            updateListeners.remove(listener);
        }
    }

    public int getCursor() {
        return this.screenCursor;
    }

    public Field locateFieldAt(int cursorPos) {
        Field[] fields = calculateFields();

        int fieldPosition = 0;
        for (; fieldPosition < fields.length; fieldPosition++) {
            Field field = fields[fieldPosition];
            if (field.containsPosition(cursorPos)) {
                return field;
            }
        }
        return null;
    }

    public String getValueFromFieldContaining(String text) throws TextNotFoundException {
        Boolean foundHeader = false;
        for (Field field : calculateFields()) {
            if (!foundHeader) {
                if (field.containsText(text)) {
                    foundHeader = true;
                }
            } else {
                String foundText = field.getFieldWithoutNulls();
                if (foundText.length() > 0) {
                    return foundText;
                }
            }
        }

        throw new TextNotFoundException(CANT_FIND_TEXT + text + "'");
    }

    public synchronized void type(String text) throws KeyboardLockedException, FieldNotFoundException {
        this.screenCursor = type(text, this.screenCursor);
    }

    public synchronized int type(String text, int column, int row) throws KeyboardLockedException, FieldNotFoundException {
        int position = column + (row * column);

        return type(text, position);
    }

    public synchronized int type(String text, int position) throws KeyboardLockedException, FieldNotFoundException {
        if (keyboardLockSet) {
            throw new KeyboardLockedException("Unable to type as keyboard is locked");
        }

        if (buffer[position] != null && !(buffer[position] instanceof BufferChar)) {
            throw new FieldNotFoundException("Unable to type where the cursor is pointing to - " + this.screenCursor);
        }

        BufferStartOfField sf = null;
        int sfPos = position - 1;
        if (sfPos < 0) {
            sfPos = buffer.length - 1;
        }
        while(sfPos != position) {
            if (buffer[sfPos] instanceof BufferStartOfField) {
                sf = (BufferStartOfField) buffer[sfPos];
                break;
            }

            sfPos--;
            if (sfPos < 0) {
                sfPos = buffer.length - 1;
            }
        }

        // *** if no field found, assume unprotected
        if (sf != null && sf.isProtected()) {
            throw new FieldNotFoundException("Unable to type where the cursor is pointing to - " + position);
        }

        if (text.length() == 0) {
            return position;
        }

        for (int i = 0; i < text.length(); i++) {
            IBufferHolder bh = buffer[position];
            if (bh != null && !(bh instanceof BufferChar)) {
                throw new FieldNotFoundException(
                        "Unable to type where the cursor is pointing to - " + position);
            }

            buffer[position] = new BufferChar(text.charAt(i));
            position++;
            if (position >= screenSize) {
                position = 0;
            }
            
            this.screenCursor = position;
        }

        if (sf != null) {
            sf.setFieldModified();
        }

        return position;
    }

    public synchronized byte[] aid(AttentionIdentification aid) throws DatastreamException, TerminalInterruptedException {
        lockKeyboard();

        try {
            ByteArrayOutputStream outboundBuffer = new ByteArrayOutputStream();

            outboundBuffer.write(aid.getKeyValue());

            BufferAddress cursor = new BufferAddress(this.screenCursor);
            outboundBuffer.write(cursor.getCharRepresentation());

            if (aid == AttentionIdentification.CLEAR) {
                erase();
            } else {
                boolean fieldModified = false;
                boolean fieldProtected = false;

                // *** Locate the first StartOfField in the buffer, if absent, then unformatted,
                // send everything back.

                int start = 0;
                int end = 0;
                for (; start < buffer.length; start++) {
                    if (buffer[start] instanceof BufferStartOfField) {
                        break;
                    }
                }

                if (start >= buffer.length) { // indicates unfromatted, send it all
                    start = 0;
                    end = buffer.length - 1;

                    // OrderSetBufferAddress sba = new OrderSetBufferAddress(new BufferAddress(0));
                    // outboundBuffer.write(sba.getCharRepresentation());
                    fieldModified = true;
                } else { // formatted
                    end = start - 1;
                    if (end < 0) {
                        end = buffer.length - 1;
                    }
                }

                int pos = start;
                while (true) {
                    IBufferHolder bh = buffer[pos];
                    if (bh instanceof BufferStartOfField) {
                        BufferStartOfField bsf = (BufferStartOfField) bh;
                        fieldModified = bsf.isFieldModifed();
                        fieldProtected = bsf.isProtected();

                        if (fieldModified && !fieldProtected) {
                            OrderSetBufferAddress sba = new OrderSetBufferAddress(new BufferAddress(pos + 1));
                            outboundBuffer.write(sba.getCharRepresentation());
                        }
                    } else if (bh instanceof BufferChar) {
                        BufferChar bc = (BufferChar) bh;
                        if (fieldModified && !fieldProtected) {
                            byte value = bc.getFieldEbcdic();
                            if (value != 0) {
                                outboundBuffer.write(value);
                            }
                        }
                    }

                    if (pos == end) {
                        break;
                    }

                    pos++;
                    if (pos >= buffer.length) {
                        pos = 0;
                    }
                }

                String hex = new String(Hex.encodeHex(outboundBuffer.toByteArray()));
                logger.trace("outbound=" + hex);
            }

            String hex = new String(Hex.encodeHex(outboundBuffer.toByteArray()));
            logger.trace("outbound=" + hex);

            for (IScreenUpdateListener listener : updateListeners) {
                listener.screenUpdated(Direction.SENDING, aid);
            }

            return outboundBuffer.toByteArray();
        } catch (IOException e) {
            throw new DatastreamException("Unable to generate outbound datastream", e);
        }
    }

    public int getScreenSize() {
        return this.screenSize;
    }

    public String printFields() {
        Field[] fields = calculateFields();

        StringBuilder sb = new StringBuilder();
        for (Field field : fields) {
            sb.append(field.toString());
            sb.append("\n");
        }

        return sb.toString();
    }

    public void setBuffer(IBufferHolder[] newBuffer) {
        for (int i = 0; i < this.buffer.length && i < newBuffer.length; i++) {
            this.buffer[i] = newBuffer[i];
        }
    }

    public void setBuffer(int col, int row, String text) {
        int pos = (row * 80) + col;
        for (int i = 0; i < text.length(); i++) {
            buffer[pos] = new BufferChar(text.charAt(i));
            pos++;
        }
    }

    public void nullify(int col, int row, int len) {
        int pos = (row * 80) + col;
        for (int i = 0; i < len; i++) {
            buffer[pos] = null;
            pos++;
        }
    }

    public Field getFieldAt(int col, int row) {
        int pos = (row * 80) + col;

        Field[] fields = calculateFields();
        Field currentField = fields[0];
        for (int i = 1; i < fields.length; i++) {
            if (fields[i].getStart() > pos) {
                return currentField;
            }
            currentField = fields[i];
        }

        return currentField;
    }

    public void setCursorPosition(int newPosition) {
        this.screenCursor = newPosition;
    }

}
