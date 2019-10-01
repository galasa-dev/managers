package dev.galasa.zos3270.internal.datastream;

public abstract class Order {
	
	@Override
	public String toString() {
		return "Ignore SQ";
	}

	public abstract byte[] getBytes();

}
