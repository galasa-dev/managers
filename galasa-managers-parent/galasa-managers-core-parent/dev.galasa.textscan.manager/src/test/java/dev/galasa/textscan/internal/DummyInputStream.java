/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.textscan.internal;

import java.io.IOException;
import java.io.InputStream;

class DummyInputStream extends InputStream {
	@Override
	public int read() throws IOException {
		throw new IOException("EXCEPTION");
	}
}