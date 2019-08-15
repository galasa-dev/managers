package dev.galasa.common.zos3270.internal.terminal.fields;

import java.util.List;

/**
 * Create a Start of Field position, represents to SF order
 * 
 * @author Michael Baylis
 *
 */
public class FieldStartOfField extends Field {
	
	private final boolean fieldProtected; 
	private final boolean fieldNumeric; 
	private final boolean fieldDisplay; 
	private final boolean fieldIntenseDisplay; 
	private final boolean fieldSelectorPen; 
	private boolean fieldModifed; 
	
	/**
	 * Create the start of a field
	 * 
	 * @param position - The buffer position the field is to start
	 */
	public FieldStartOfField(int position,
			boolean fieldProtected, 
			boolean fieldNumeric,
			boolean fieldDisplay, 
			boolean fieldIntenseDisplay, 
			boolean fieldSelectorPen,
			boolean fieldModifed) {
		super(position, position);
		this.fieldProtected = fieldProtected;
		this.fieldNumeric = fieldNumeric;
		this.fieldDisplay = fieldDisplay;
		this.fieldIntenseDisplay = fieldIntenseDisplay;
		this.fieldSelectorPen = fieldSelectorPen;
		this.fieldModifed = fieldModifed;		
	}

	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#toString()
	 */
	@Override
	public String toString() {
		return "StartOfField(" + super.toString() + ")";
	}

	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#split(java.util.List, int, int)
	 */
	@Override
	public void split(List<Field> originalFields, int splitStart, int splitEnd) {
		int ourPosition = originalFields.indexOf(this);		
		
		//*** Check if we need to remove the whole field
		if (splitStart <= this.start && this.end <= splitEnd) {
			originalFields.remove(ourPosition);
		}
	}

	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#getFieldString(java.lang.StringBuilder)
	 */
	@Override
	public void getFieldString(StringBuilder sb) {
		sb.append(" ");
	}

	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#merge(java.util.List, io.ejat.zos3270.internal.terminal.fields.Field)
	 */
	@Override
	public void merge(List<Field> allFields, Field nextField) {
		//*** Cant merge with anything
		throw new UnsupportedOperationException("Should not have entered this method");
	}

	public boolean isProtected() {
		return fieldProtected;
	}

	public boolean isNumeric() {
		return fieldNumeric;
	}

	public boolean isDisplay() {
		return fieldDisplay;
	}

	public boolean isIntenseDisplay() {
		return fieldIntenseDisplay;
	}

	public boolean isSelectorPen() {
		return fieldSelectorPen;
	}

	public boolean isFieldModifed() {
		return fieldModifed;
	}

	public void setFieldModified() {
		this.fieldModifed = true;
	}

	
}
