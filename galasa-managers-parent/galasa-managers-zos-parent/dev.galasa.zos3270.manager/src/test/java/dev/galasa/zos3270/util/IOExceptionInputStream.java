/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.util;

import java.io.IOException;
import java.io.InputStream;

public class IOExceptionInputStream extends InputStream {

    @Override
    public int read() throws IOException {
        throw new IOException("Dummy exception");
    }

}
