package dev.galasa.zos3270.common.screens;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

public class Terminal {
	
	private String id;
	private List<TerminalImage> images;
	private TerminalSize        defaultSize;
	
	public Terminal(@NotNull String id, @NotNull TerminalSize defaultSize) {
		this.id          = id;
		this.defaultSize = defaultSize;
	}
	
	public @NotNull String getId() {
		return id;
	}
	
	public @NotNull List<TerminalImage> getImages() {
		if (images == null) {
			return new ArrayList<>();
		}
		return images;
	}
	
	public void addImage(@NotNull TerminalImage image) {
		if (image == null) {
			return;
		}
		
		if (this.images == null) {
			this.images = new ArrayList<>();
		}
		
		this.images.add(image);
	}
	
	public @NotNull TerminalSize getDefaultSize() {
		return this.defaultSize;
	}
	
}
