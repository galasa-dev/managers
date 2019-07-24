package dev.voras.common.zos3270.internal.datastream;

public enum AttentionIdentification {
	
	ENTER((byte)0x7d),
	CLEAR((byte)0x6d),
	PF1((byte)0xf1),
	PF3((byte)0xf3),
	STRUCTURED_FIELD((byte)0x88);
	
	private final byte keyValue;
	
	AttentionIdentification(byte keyValue) {
		this.keyValue = keyValue;
	}
	
	public byte getKeyValue() {
		return this.keyValue;
	}
	
	public static AttentionIdentification valueOfAid(byte aid) {
		for(AttentionIdentification a : values()) {
			if (a.keyValue == aid) {
				return a;
			}
		}
		
		return null;
	}

}
