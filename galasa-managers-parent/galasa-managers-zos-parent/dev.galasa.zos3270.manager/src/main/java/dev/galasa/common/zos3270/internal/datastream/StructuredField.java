package dev.galasa.common.zos3270.internal.datastream;

import dev.galasa.common.zos3270.spi.NetworkException;

public class StructuredField {
	
    public static final byte SF_READ_PARTITION = 0x01;
    public static final byte SF_3270_DS        = 0x40;
    
    protected StructuredField() {}

	public static StructuredField getStructuredField(byte[] sfData) throws NetworkException {
		switch(sfData[0]) {
	        case SF_READ_PARTITION:
	            return new StructuredFieldReadPartition(sfData);
	        case SF_3270_DS:
	            return new StructuredField3270DS(sfData);
		default:
			throw new NetworkException("Unknown Structured Field = " + sfData[0]);
		}
	}

}
