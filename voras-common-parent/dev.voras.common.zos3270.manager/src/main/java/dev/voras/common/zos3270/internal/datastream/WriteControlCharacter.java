package dev.voras.common.zos3270.internal.datastream;

import java.util.BitSet;

public class WriteControlCharacter {
	
	private boolean nop;
	private boolean reset;
	private boolean printer1;
	private boolean printer2;
	private boolean startPrinter;
	private boolean soundAlarm;
	private boolean keyboardReset;
	private boolean resetMDT;
	
	public WriteControlCharacter(byte wcc) {
		BitSet bitSet = BitSet.valueOf(new byte[] {wcc});
		nop           = bitSet.get(7);
		reset         = bitSet.get(6);
		printer1      = bitSet.get(5);
		printer2      = bitSet.get(4);
		startPrinter  = bitSet.get(3);
		soundAlarm    = bitSet.get(2);
		keyboardReset = bitSet.get(1);
		resetMDT      = bitSet.get(0);
	}

	public boolean isNop() {
		return nop;
	}

	public boolean isReset() {
		return reset;
	}

	public boolean isPrinter1() {
		return printer1;
	}

	public boolean isPrinter2() {
		return printer2;
	}

	public boolean isStartPrinter() {
		return startPrinter;
	}

	public boolean isSoundAlarm() {
		return soundAlarm;
	}

	public boolean isKeyboardReset() {
		return keyboardReset;
	}

	public boolean isResetMDT() {
		return resetMDT;
	}

}
