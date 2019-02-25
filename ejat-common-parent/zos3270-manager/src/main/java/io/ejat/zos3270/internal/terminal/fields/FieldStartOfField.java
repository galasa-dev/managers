package io.ejat.zos3270.internal.terminal.fields;

import java.util.List;

/**
 * Creat a Start of Field position, represents to SF order
 * 
 * @author Michael Baylis
 *
 */
public class FieldStartOfField extends Field {
	
	/**
	 * Create the start of a field
	 * 
	 * @param position - The buffer position the field is to start
	 */
	public FieldStartOfField(int position) {
		super(position, position);
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

}
