package io.ejat.zos3270.internal.datastream;

import io.ejat.zos3270.spi.DatastreamException;

public abstract class CommandCode {
	
	public static final byte WRITE                 = (byte) 0xf1; 
	public static final byte ERASE_WRITE           = (byte) 0xf5; 
	public static final byte ERASE_WRITE_ALTERNATE = (byte) 0x7e;
	public static final byte READ_BUFFER           = (byte) 0xf2; 
	public static final byte READ_MODIFIED         = (byte) 0xf6; 
	public static final byte READ_MODIFIED_ALL     = (byte) 0x6e; 
	public static final byte ERASE_ALL_UNPROTECTED = (byte) 0x6f; 
	public static final byte WRITE_STRUCTURED      = (byte) 0xf3; 
	
	protected CommandCode() {}
	
	public static CommandCode getCommandCode(byte commandCode) throws DatastreamException {
		switch (commandCode) {
		case ERASE_WRITE:
			return new CommandEraseWrite();
		case WRITE:
			return new CommandWrite();
		case WRITE_STRUCTURED:
			return new CommandWriteStructured();
		case ERASE_WRITE_ALTERNATE:
		case READ_BUFFER:
		case READ_MODIFIED:
		case READ_MODIFIED_ALL:
		case ERASE_ALL_UNPROTECTED:
			throw new DatastreamException("Unsupported command code=" + commandCode);
		default:
			throw new DatastreamException("Unrecognised command code=" + commandCode);
		}
	}

}
