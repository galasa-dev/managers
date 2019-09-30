package dev.galasa.common.zos3270.internal.datastream;

public class CommandEraseWrite extends CommandCode {

	public byte[] getBytes() {
		return new byte[] {ERASE_WRITE};
	}
	
}
