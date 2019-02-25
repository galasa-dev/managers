package io.ejat.zos3270.internal.terminal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.ejat.zos3270.internal.datastream.Order;
import io.ejat.zos3270.internal.datastream.OrderInsertCursor;
import io.ejat.zos3270.internal.datastream.OrderRepeatToAddress;
import io.ejat.zos3270.internal.datastream.OrderSetBufferAddress;
import io.ejat.zos3270.internal.datastream.OrderStartField;
import io.ejat.zos3270.internal.datastream.OrderText;
import io.ejat.zos3270.internal.terminal.fields.Field;
import io.ejat.zos3270.internal.terminal.fields.FieldChars;
import io.ejat.zos3270.internal.terminal.fields.FieldStartOfField;
import io.ejat.zos3270.internal.terminal.fields.FieldText;
import io.ejat.zos3270.spi.DatastreamException;

/**
 * Screen representation of the 3270 terminal
 * 
 * @author Michael Baylis
 *
 */
public class Screen {

	private LinkedList<Field> fields = new LinkedList<>();

	private final int columns;
	private final int rows;
	private final int screenSize;

	private int workingCursor = 0;
	
	private int screenCursor = 0;
	
	/**
	 * Create a default screen
	 */
	public Screen() {
		this(80, 24);
	}

	/**
	 * Create a default screen
	 * 
	 * @param columns - Number of columns
	 * @param rows - Number of rows 
	 */
	public Screen(int columns, int rows) {
		this.columns    = columns;
		this.rows       = rows;
		this.screenSize = this.columns * this.rows; 
	}
	
	/**
	 * Get the screen size, ie the buffer length
	 * 
	 * @return
	 */
	public int getScreenSize() {
		return this.screenSize;
	}

	/**
	 * Clear the screen and fill with nulls
	 */
	public void erase() {
		fields.clear();
		fields.add(new FieldChars((char) 0, 0, screenSize - 1));
		this.screenCursor = 0;
	}
	
	/**
	 * Return the buffer address of the cursor
	 * 
	 * @return - Address of the cursor
	 */
	public int getCursor() {
		return this.screenCursor;
	}


	/**
	 * Process the 3270 datastream orders to build up the screen
	 * 
	 * @param orders - List of orders
	 * @throws DatastreamException - If we discover a unknown order
	 */
	public void processOrders(Iterable<Order> orders) throws DatastreamException {
		this.workingCursor = 0;
		for(Order order : orders) {
			if (order instanceof OrderSetBufferAddress) {
				processSBA((OrderSetBufferAddress) order);
			} else if (order instanceof OrderRepeatToAddress) {
				processRA((OrderRepeatToAddress) order);
			} else if (order instanceof OrderText) {
				processText((OrderText) order);
			} else if (order instanceof OrderStartField) {
				processSF((OrderStartField) order);
			} else if (order instanceof OrderInsertCursor) {
				this.screenCursor = this.workingCursor;
			} else {
				throw new DatastreamException("Unsupported Order - " + order.getClass().getName());
			}
		}
		
		int pos = 0;
		//*** Merge suitable fields,  text and chars
		while(pos < this.fields.size()) {
			int nextPos = pos + 1;
			if (nextPos >= this.fields.size()) {
				break;
			}
			
			Field thisField = this.fields.get(pos);
			Field nextField = this.fields.get(nextPos);
			
			if ((thisField instanceof FieldText) 
					|| (thisField instanceof FieldChars)) {
				if ((nextField instanceof FieldText) 
						|| (nextField instanceof FieldChars)) {
					thisField.merge(this.fields, nextField);
					continue; // Go round again without incrementing position
				}
			}
			pos++;
		}
	}

	/**
	 * Process a Set Buffer Address order
	 * 
	 * @param order - the order to process
	 */
	private void processSBA(OrderSetBufferAddress order) {
		this.workingCursor = order.getBufferAddress();
	}

	/**
	 * Process a Start Field order
	 * 
	 * @param order - the order to process
	 */
	private void processSF(OrderStartField order) { //NOSONAR - will be using it soon
		Field newField = new FieldStartOfField(this.workingCursor);
		insertField(newField);
		
		this.workingCursor++;
	}
	
	/**
	 * Process Text - not really an order
	 * 
	 * @param order - the order to process
	 */
	private void processText(OrderText order) {		
		String text = order.getText();
		Field newField = new FieldText(text, this.workingCursor, (this.workingCursor + text.length()) - 1);
		insertField(newField);
		
		this.workingCursor += text.length();
	}
	
	/**
	 * Process the Report ot Address order 
	 * 
	 * @param order - the order to process
	 */
	private void processRA(OrderRepeatToAddress order) {
		int endOfRepeat = order.getBufferAddress();
		
		if (endOfRepeat <= this.workingCursor) {
			throw new UnsupportedOperationException("cant cope with wrapping yet");
		}
		
		Field newField = new FieldChars(order.getChar(), this.workingCursor, endOfRepeat - 1);
		insertField(newField);
		
		this.workingCursor = endOfRepeat;
	}
	
	/**
	 * Insert a new field from the process orders
	 * 
	 * @param newField - The field to insert into the buffer
	 */
	public void insertField(Field newField) {
		//*** Easy if there are no pre-existing fields
		if (this.fields.isEmpty()) {
			this.fields.add(newField);
			return;
		}
		
		//*** Locate all the current fields that span the new start and end positions
		List<Field> selectedFields = locateFieldsBetween(newField.getStart(), newField.getEnd());
		if (selectedFields.isEmpty()) {
			//*** If there are no fields,  then find the field that is after the end address
			int followingFieldPos = this.fields.size();
			for(int i = 0; i < this.fields.size(); i++) {
				if (newField.getEnd() < this.fields.get(i).getStart()) {
					followingFieldPos = i;
					break;
				}
			}
			//*** Insert the new field into the correct position
			this.fields.add(followingFieldPos, newField);
		} else {
			//*** Tell the spanned fields to split as appropriate
			int fieldPosition = fields.indexOf(selectedFields.get(0));
			for(Field field : selectedFields) {
				field.split(this.fields, newField.getStart(), newField.getEnd());
			}
			
			//*** Insert at the appropriate place
			if (this.fields.size() <= fieldPosition) {
				this.fields.add(newField);
				return;
			}
			
			if (newField.getStart() < fields.get(fieldPosition).getStart()) {
				this.fields.add(fieldPosition, newField);
			} else {
				this.fields.add(fieldPosition + 1, newField);
			}
		}
	}
	
	/**
	 * Convert the fields into a string for printing or otherwise
	 * 
	 * @return - A representation of the screen
	 */
	public String printScreen() {
		StringBuilder sb = new StringBuilder();
		for(Field field : this.fields) {
			field.getFieldString(sb);
		}
		
		StringBuilder screenSB = new StringBuilder();
		String screenString = sb.toString();
		for(int i = 0; i < this.screenSize; i += this.columns) {
			screenSB.append(screenString.substring(i, i + this.columns));
			screenSB.append('\n');
		}
		return screenSB.toString();
	}


	/**
	 * Find fields that span the start and end addresses
	 * 
	 * @param start - The start address
	 * @param end - The end address
	 * @return - a list of covered fields
	 */
	private List<Field> locateFieldsBetween(int start, int end) {
		ArrayList<Field> selectedFields = new ArrayList<>();
		for(Field field : this.fields) {
			if (field.containsPositions(start, end)) {
				selectedFields.add(field);
			}
		}
		return selectedFields;
	}
	
	/**
	 * Produce a printable list of the fields on the screen 
	 * 
	 * @return - A list of the fields
	 */
	public String printFields() {
		StringBuilder sb = new StringBuilder();
		for(Field field : fields) {
			if (sb.length() > 0) {
				sb.append('\n');
			}
			sb.append(field.toString());
		}
		return sb.toString();
	}

}
