package dev.voras.common.zos3270;

import javax.validation.constraints.NotNull;

public interface IScreenUpdateListener {
	
	public enum Direction {
		Received,
		Sending
	}
    
    void screenUpdated(@NotNull Direction direction, AttentionIdentification aid);

}
