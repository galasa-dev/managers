package dev.galasa.common.zos3270.internal.datastream;

public class OrderInsertCursor extends Order {
	
	public static final byte ID = 0x13;
	
	@Override
	public String toString() {
		return "IC";
	}
	
	public byte[] getBytes() {
		return new byte[] {ID};
	}

}
