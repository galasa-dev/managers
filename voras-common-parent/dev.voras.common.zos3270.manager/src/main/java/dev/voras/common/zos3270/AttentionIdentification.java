package dev.voras.common.zos3270;

public enum AttentionIdentification {
	
	ENTER((byte)0x7d),
	CLEAR((byte)0x6d),
	PF1((byte)0xf1),
	PF2((byte)0xf2),
	PF3((byte)0xf3),
	PF4((byte)0xf4),
	PF5((byte)0xf5),
	PF6((byte)0xf6),
	PF7((byte)0xf7),
	PF8((byte)0xf8),
	PF9((byte)0xf9),
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
