package dev.voras.common.zos3270.internal.terminal.fields;

import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * Represents a field of repeating characters, normally spaces or nulls.
 * 
 * @author Michael Baylis
 *
 */
public class FieldChars extends Field {
	
	private final char character;
	
	/**
	 * Create a field full of a repeating character.
	 * 
	 * @param character - the character to repeat
	 * @param position - the start position of the field
	 * @param end - the last position of the field
	 */
	public FieldChars(char character, int position, int end) {
		super(position, end);
		
		this.character = character;
	}

	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#toString()
	 */
	@Override
	public String toString() {
		String c;
		//*** Convert nulls to spaces
		if (this.character == 0x00) {
			c = " ";
		} else {
			c = new String(new char[] {this.character});
		}
		
		return "Chars(" + c + "," + super.toString() + ")";
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
			return;
		}
		
		//*** If the split is in the middle, chop off the end and create a new field
		if (start < splitStart && splitEnd < end) {
			int originalEnd = this.end;
			
			this.end = splitStart - 1;
			
			int newStart = splitEnd + 1;
			Field newField = new FieldChars(character, newStart, originalEnd);
			originalFields.add(ourPosition + 1, newField);
			return;
		}
		
		//*** Check if we are chopping off the end,  if so shorten the field
		if (splitStart <= end && end <= splitEnd) {
			this.end = splitStart - 1;
			return;
		}
		
		
		//*** Check if we are chopping off the start,  if so shift and shorten the field
		if (splitStart <= start && start <= splitEnd) {
			this.start = splitEnd + 1;
		}
	}

	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#getFieldString(java.lang.StringBuilder)
	 */
	@Override
	public void getFieldString(StringBuilder sb) {
		for(int i = this.start; i <= this.end; i++) {
			sb.append(this.character);
		}
	}

	/**
	 * @return - the repeating character
	 */
	public char getCharacter() {
		return this.character;
	}
	
	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#merge(java.util.List, io.ejat.zos3270.internal.terminal.fields.Field)
	 */
	@Override
	public void merge(List<Field> allFields, Field nextField) {
		//*** If we are merging with another char and the character is the same, simply extend the field
		//*** and remove the merged field
		if (nextField instanceof FieldChars) {
			FieldChars fieldChar = (FieldChars) nextField;
			if (this.character == fieldChar.character) {
				this.end = fieldChar.end;
				allFields.remove(fieldChar);
				return;
			}
		}
		
		if (!(nextField instanceof FieldText) && !(nextField instanceof FieldChars)) {
			throw new UnsupportedOperationException("Cannot merge with " + nextField.getClass().getName());
		}
		
		//*** If merging with a text field, create a new text field and merge with the next one
		//*** and remove from list
		FieldText replacementField = new FieldText(this);
		int pos = allFields.indexOf(this); 
		allFields.add(pos, replacementField);  // TODO this does not appear to be working
		allFields.remove(this);
		replacementField.merge(allFields, nextField);
	}

	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#containsText(java.lang.String)
	 */
	@Override
	public boolean containsText(@NotNull String searchText) {
		char[] searchChars = searchText.toCharArray();
		
		for(char c : searchChars) {
			if (c != this.character) {
				return false;
			}
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#isTypeable()
	 */
	@Override
	public boolean isTypeable() {
		return !this.previousStartOfField.isProtected();
	}

}
