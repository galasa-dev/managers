package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class OrderText extends Order {
	
	private static final Charset ebcdic = Charset.forName("Cp037");
	
	private StringBuilder text = new StringBuilder();
	
	public OrderText() {
	}
	
	public OrderText(String newText) {
		this.text.append(newText);
	}

	public void append(byte data) {
		if (data == -1) {
			data = 0x00;
		}
		
		byte[] charByte = new byte[] {data};
		text.append(ebcdic.decode(ByteBuffer.wrap(charByte)).array()[0]);
	}
	
	@Override
	public String toString() {
		return "TEXT(" + text.toString() + ")";
	}

	public String getText() {
		return text.toString();
	}

	@Override
	public byte[] getBytes() {
		throw new UnsupportedOperationException("Needs to be written");
	}
	
}
