package dev.galasa.common.zos3270.spi;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.common.zos3270.AttentionIdentification;
import dev.galasa.common.zos3270.FieldNotFoundException;
import dev.galasa.common.zos3270.ITerminal;
import dev.galasa.common.zos3270.KeyboardLockedException;
import dev.galasa.common.zos3270.TextNotFoundException;
import dev.galasa.common.zos3270.TimeoutException;
import dev.galasa.common.zos3270.internal.comms.Network;
import dev.galasa.common.zos3270.internal.comms.NetworkThread;

public class Terminal implements ITerminal {

    private final Screen screen;
    private final Network network;
    private NetworkThread networkThread;

    private int defaultWaitTime = 1_200_000;

    private Log logger = LogFactory.getLog(getClass());

    public Terminal(String host, int port) {
        this(host, port, false);
    }

    public Terminal(String host, int port, boolean ssl) {
        network = new Network(host, port, ssl);
        screen = new Screen(80, 24, this.network);
    }

    public synchronized void connect() throws NetworkException {
        network.connectClient();
        networkThread = new NetworkThread(screen, network, network.getInputStream());
        networkThread.start();
    }

    public void disconnect() {
        network.close();
        try {
            networkThread.join();
        } catch (InterruptedException e) { //NOSONAR
            logger.error("Problem joining network thread",e);
        }
        networkThread = null;
    }


    @Override
    public ITerminal waitForKeyboard() throws TimeoutException, KeyboardLockedException {
        logger.trace("Waiting for keyboard");
        screen.waitForKeyboard(defaultWaitTime);
        logger.trace("Wait for keyboard complete");
        return this;
    }


    public Screen getScreen() {
        return this.screen;
    }

    @Override
    public ITerminal positionCursorToFieldContaining(@NotNull String text) throws TextNotFoundException, KeyboardLockedException {
        screen.positionCursorToFieldContaining(text);
        return this;
    }

    @Override
    public Terminal verifyTextInField(String text) throws TextNotFoundException {
        screen.searchFieldContaining(text);
        return this;
    }

    @Override
    public boolean isTextInField(String text) {
        return screen.isTextInField(text);
    }

    @Override
    public Terminal waitForTextInField(String text) throws TextNotFoundException {
        screen.waitForTextInField(text, defaultWaitTime);
        return this;
    }

    @Override
    public ITerminal type(String text) throws KeyboardLockedException, FieldNotFoundException {
        screen.type(text);
        return this;
    }

    @Override
    public ITerminal tab() throws KeyboardLockedException, FieldNotFoundException {
        screen.tab();
        return this;
    }

    @Override
    public ITerminal enter() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.ENTER));
        return this;
    }

    @Override
    public ITerminal clear() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.CLEAR));
        return this;
    }

    @Override
    public ITerminal pf1() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF1));
        return this;
    }

    @Override
    public ITerminal pf2() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF2));
        return this;
    }

    @Override
    public ITerminal pf3() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF3));
        return this;
    }

    @Override
    public ITerminal pf4() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF4));
        return this;
    }

    @Override
    public ITerminal pf5() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF5));
        return this;
    }

    @Override
    public ITerminal pf6() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF6));
        return this;
    }

    @Override
    public ITerminal pf7() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF7));
        return this;
    }

    @Override
    public ITerminal pf8() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF8));
        return this;
    }

    @Override
    public ITerminal pf9() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF9));
        return this;
    }

    @Override
    public ITerminal pf10() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF10));
        return this;
    }

    @Override
    public ITerminal pf11() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF11));
        return this;
    }

    @Override
    public ITerminal pf12() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF12));
        return this;
    }

    @Override
    public ITerminal pf13() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF13));
        return this;
    }

    @Override
    public ITerminal pf14() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF14));
        return this;
    }

    @Override
    public ITerminal pf15() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF15));
        return this;
    }

    @Override
    public ITerminal pf16() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF16));
        return this;
    }

    @Override
    public ITerminal pf17() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF17));
        return this;
    }

    @Override
    public ITerminal pf18() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF18));
        return this;
    }

    @Override
    public ITerminal pf19() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF19));
        return this;
    }

    @Override
    public ITerminal pf20() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF20));
        return this;
    }

    @Override
    public ITerminal pf21() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF21));
        return this;
    }

    @Override
    public ITerminal pf22() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF22));
        return this;
    }

    @Override
    public ITerminal pf23() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF23));
        return this;
    }

    @Override
    public ITerminal pf24() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF24));
        return this;
    }

    @Override
    public ITerminal pa1() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PA1));
        return this;
    }

    @Override
    public ITerminal pa2() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PA2));
        return this;
    }

    @Override
    public ITerminal pa3() throws KeyboardLockedException, NetworkException {
        network.sendDatastream(screen.aid(AttentionIdentification.PA3));
        return this;
    }

    @Override
    public ITerminal reportScreen() {
        logger.info("\n" + screen.printScreen());
        return this;
    }

    @Override
    public ITerminal reportScreenWithCursor() {
        logger.info("\n" + screen.printScreenTextWithCursor());
        return this;
    }

    /**
     * Returns the screen print out as a String. 
     * For use in edge testing cases. 
     * 
     * @return Screen as String
     */
    public String retrieveScreen() {
        return screen.printScreen();
    }

    /**
     * Return a String of the contents of the current Field. 
     * Current field is the one which the cursor is at. 
     * 
     * @return Current Field as String
     */
    public String retrieveFieldAtCursor() {
        int cursorPos = screen.getCursor();
        Field cursorField = screen.locateFieldAt(cursorPos);
        return cursorField.getFieldWithoutNulls();
    }

    /**
     * Returns a String of text in a Field with a given name.
     * 
     * @param fieldName Name of the field to be extracted from
     * @return String which has been extracted from the field
     * @throws TextNotFoundException
     */
    public String retrieveFieldTextAfterFieldWithString(String fieldName) throws TextNotFoundException {
        String text = screen.getValueFromFieldContaining(fieldName);
        return text;
    }

	public String getHostPort() {
		return this.network.getHostPort();
	}


}
