/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zos3270.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import dev.galasa.zos3270.AttentionIdentification;
import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.BufferAddress;
import dev.galasa.zos3270.internal.datastream.CommandEraseWrite;
import dev.galasa.zos3270.internal.datastream.OrderGraphicsEscape;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.spi.Screen;

/**
 * Test the Graphics Escape order 
 * 
 * @author Michael Baylis
 *
 */
public class GraphicsEscapeTest {
    
    /**
     * Test with two fields
     * 
     * @throws Exception
     */
    @Test 
    public void testGeGoldenPath() throws Exception {
        
        Screen screen = new Screen(10, 2, null);
        screen.erase();
        
        ByteBuffer geBuffer = ByteBuffer.wrap(new byte[] {0x50});

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("1234"));
        orders.add(new OrderGraphicsEscape(geBuffer));
        orders.add(new OrderText("6789ABCDEFGHIJ"));
        
        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("1234 6789ABCDEFGHIJ should be in the first field before test").isEqualTo("1234 6789ABCDEFGHIJ");

    }
    
    /**
     * Ensure that when the GE buffer is converted back to a datastream, it includes the order
     * 
     * @throws Exception
     */
    @Test
    public void testGeConvertToDatastream() throws Exception {
        Screen screen = new Screen(6, 1, null);
        screen.erase();
        
        ByteBuffer geBuffer = ByteBuffer.wrap(new byte[] {0x50});

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, true)); // needs modified to ensure field written outbound
        orders.add(new OrderText("12"));
        orders.add(new OrderGraphicsEscape(geBuffer));
        orders.add(new OrderText("34"));
        
        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("Unexpected screen").isEqualTo("12 34");

        byte[] outboundDatastream = screen.aid(AttentionIdentification.ENTER);
        String hexDatastream = Hex.encodeHexString(outboundDatastream, false);
        
        System.out.println(hexDatastream);
        
        assertThat(hexDatastream).as("Expected outbound datastream").isEqualTo("7D40401140C1F1F20850F3F4");

    }
    
}
