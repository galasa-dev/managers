/*
 * Copyright contributors to the Galasa project
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
