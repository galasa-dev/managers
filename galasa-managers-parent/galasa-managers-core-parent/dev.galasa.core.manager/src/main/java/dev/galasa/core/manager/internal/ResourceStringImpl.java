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
	
	public ResourceStringImpl(String actualString) {
		this.actualString = actualString;
	}

	@Override
	public String getString() {
		return this.actualString;
	}

	@Override
	public String toString() {
		return this.actualString;
	}
}
