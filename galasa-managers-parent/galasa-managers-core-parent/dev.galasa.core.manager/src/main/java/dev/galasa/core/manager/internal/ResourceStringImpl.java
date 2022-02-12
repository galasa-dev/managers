/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.core.manager.internal;

import dev.galasa.core.manager.IResourceString;

/**
 * Basic holder of a Resource String.
 * Not strictly necessary, but future proofing
 * 
 * @author Michael Baylis
 *
 */
public class ResourceStringImpl implements IResourceString {
	
	private final String actualString;
	private final int    length;
	
	public ResourceStringImpl(String actualString, int length) {
		this.actualString = actualString;
		this.length       = length;
	}

	@Override
	public String getString() {
		return this.actualString;
	}

	@Override
	public String toString() {
		return this.actualString;
	}
	
	public int getLength() {
		return this.length;
	}
}
