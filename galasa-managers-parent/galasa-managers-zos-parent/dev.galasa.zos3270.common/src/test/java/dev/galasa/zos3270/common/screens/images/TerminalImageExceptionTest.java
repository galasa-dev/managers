/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.common.screens.images;


import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class TerminalImageExceptionTest {
    
    @Test
    public void testCanCreateTerminalImageExceptionOk() throws Exception {
        TerminalImageException ex = new TerminalImageException();
        assertThat(ex).isNotNull();
    }

    @Test
    public void testCanCreateTerminalImageExceptionWithMessageOk() throws Exception {
        TerminalImageException ex = new TerminalImageException("a message");
        assertThat(ex).isNotNull().hasMessageContaining("a message");
    }

}
