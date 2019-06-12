package dev.voras.common.zos3270.internal.terminal.fields;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import dev.voras.common.zos3270.FieldNotFoundException;

/**
 * Represents a display field of varying text
 * 
 * @author Michael Baylis
 *
 */
public class FieldText extends Field {
	
	private static final Pattern numericPattern = Pattern.compile("\\d+");
	private static final Charset ebcdic = Charset.forName("Cp037");
	
	
	private char[] text;
	
	/**
	 * Create a new field of text characters
	 * 
	 * @param newText - Text to create the field with
	 * @param position - Start position in the buffer, inclusive, 0 based
	 * @param end - end position in the buffer, inclusive, 0 based
	 */
	public FieldText(String newText, int position, int end) {
		super(position, end);
		
		this.text = newText.toCharArray();
	}

	/**
	 * Create a new field of text characters based on a char field, ie convert it
	 * 
	 * @param fieldChars - The char field to replace
	 */
	public FieldText(FieldChars fieldChars) {
		super(fieldChars.start, fieldChars.end);
		this.text = new char[(fieldChars.end - fieldChars.start) + 1];
		char newChar = fieldChars.getCharacter();
		for(int i = 0; i < this.text.length; i++) {
			this.text[i] = newChar;
		}
		this.previousStartOfField = fieldChars.previousStartOfField;
	}

	/**
	 * Create a new field of text characters
	 * 
	 * @param text - Text to create the field with
	 * @param position - Start position in the buffer, inclusive, 0 based
	 * @param end - end position in the buffer, inclusive, 0 based
	 */
	private FieldText(char[] text, int position, int end) {
		super(position, end);
		this.text = text;
	}

	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		getFieldString(sb);
		
		return "Text(" + sb.toString() + "," + super.toString() + ")";
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
		
		//*** A split in the middle, shrink original and create one for the end
		if (start < splitStart && splitEnd < end) {
			int length1 = splitStart - start;
			int length2 = end - splitEnd;
			int offset2 = (splitEnd - start) + 1;
			
			char[] text1 = Arrays.copyOf(text, length1);
			char[] text2 = Arrays.copyOfRange(text, offset2, (offset2 + length2));
			
			int originalEnd = this.end;
			
			this.end = splitStart - 1;
			this.text = text1;
			
			int newStart = splitEnd + 1;
			Field newField = new FieldText(text2, newStart, originalEnd);
			originalFields.add(ourPosition + 1, newField);
			return;
		}

		
		//*** Check if we are chopping off the end,  if so shorten the field
		if (splitStart <= end && end <= splitEnd) {
			this.end = splitStart - 1;
			int count = (end - start) + 1;

			char[] newText = new char[count];
			for(int i = 0; i < count; i++) {
				newText[i] = this.text[i];
			}
			
			this.text = newText;
			return;
		}
		
		
		//*** Check if we are chopping off the start,  if so shift and shorten the field
		if (splitStart <= start && start <= splitEnd) {
			int oldStart = this.start;
			this.start = splitEnd + 1;
			int count = end - start + 1;

			char[] newText = new char[count];
			for(int i = 0, j = this.start - oldStart; i < count; i++, j++) {
				newText[i] = this.text[j];
			}
			
			this.text = newText;
		}
	}

	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#getFieldString(java.lang.StringBuilder)
	 */
	@Override
	public void getFieldString(StringBuilder sb) {
		for(char c : text) {
			//*** Replace nulls with spaces
			if (c == 0x00) {
				sb.append(" ");
			} else {
				sb.append(c);
			}
		}
	}

	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#merge(java.util.List, io.ejat.zos3270.internal.terminal.fields.Field)
	 */
	@Override
	public void merge(List<Field> allFields, Field nextField) {
		char[] appendText;
		int newEnd;
		if (nextField instanceof FieldText) {
			//*** If merging with a text field, simply append text
			FieldText fieldText = (FieldText) nextField;
			newEnd = fieldText.end;
			appendText = fieldText.text;
		} else if (nextField instanceof FieldChars) {
			//*** If merging with a chars field,  generate the text to merge
			FieldChars fieldChar = (FieldChars) nextField;
			newEnd = fieldChar.end;
			int extraLength = (fieldChar.end - fieldChar.start) + 1;
			appendText = new char[extraLength];
			for(int i = 0; i < extraLength; i++) {
				appendText[i] = fieldChar.getCharacter();
			}
		} else {
			throw new UnsupportedOperationException("Cannot merge with " + nextField.getClass().getName());
		}
		
		//*** Extend the array and append the new text
		int newLength = this.text.length + appendText.length;
		char[] newText = Arrays.copyOf(this.text, newLength);
		for(int j = 0, i = this.text.length; j < appendText.length; j++, i++) {
			newText[i] = appendText[j];
		}
		this.text = newText;
		this.end = newEnd;
		allFields.remove(nextField);
	}
	
	
	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#containsText(java.lang.String)
	 */
	@Override
	public boolean containsText(@NotNull String searchText) {
		StringBuilder sb = new StringBuilder();
		getFieldString(sb);
		String sbText = sb.toString();
		
		return sbText.contains(searchText);
	}
	
	
	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#isTypeable()
	 */
	@Override
	public boolean isTypeable() {
		return !this.previousStartOfField.isProtected();
	}

	public void type(String typeText) throws FieldNotFoundException {
		if (this.previousStartOfField.isNumeric()) {
			Matcher matcher = numericPattern.matcher(typeText);
			if (!matcher.matches()) {
				throw new FieldNotFoundException("Unable to type text in a numeric field");
			}
		}
		
		char[] typeTextChars = typeText.toCharArray();
		
		for(int i = 0; i < typeTextChars.length && i < this.text.length; i++) {
			this.text[i] = typeTextChars[i];
		}
		
		this.previousStartOfField.setFieldModified();
	}
	
	/* (non-Javadoc)
	 * @see io.ejat.zos3270.internal.terminal.fields.Field#isModified()
	 */
	@Override
	public boolean isModified() {
		return this.previousStartOfField.isFieldModifed();
	}
	
	
    /* (non-Javadoc)
     * @see io.ejat.zos3270.internal.terminal.fields.Field#getFieldEbcdicWithoutNulls()
     */
    @Override
    public byte[] getFieldEbcdicWithoutNulls() {
        return getFieldWithoutNulls().getBytes(ebcdic); 
    }

    /* (non-Javadoc)
     * @see io.ejat.zos3270.internal.terminal.fields.Field#getFieldEbcdicWithoutNulls()
     */
    @Override
    public String getFieldWithoutNulls() {
        StringBuilder sb = new StringBuilder();
        for(char c : text) {
            //*** Remove nulls and compress
            if (c != 0x00) {
                sb.append(c);
            }
        }

        return sb.toString();
    }

}
