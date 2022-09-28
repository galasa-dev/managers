package test.zos3270.datastream.extended;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.BufferAddress;
import dev.galasa.zos3270.internal.datastream.CommandEraseWrite;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.internal.datastream.OrderStartFieldExtended;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.spi.Colour;
import dev.galasa.zos3270.spi.Highlight;
import dev.galasa.zos3270.spi.Screen;

public class ExtendedDatastreamTest {
    
    @Test
    public void testColour1() throws Exception {
        
        String redReverse = "0341f242f2c0f8";
        String greenBlink = "0341f142f4c0f8";
        String blueNormal = "0341f042f1c0f8";
        byte[] redReverseStream = Hex.decodeHex(redReverse);
        byte[] greenBlinkStream = Hex.decodeHex(greenBlink);
        byte[] blueNormalStream = Hex.decodeHex(blueNormal);
        ByteBuffer redReverseBuffer = ByteBuffer.wrap(redReverseStream);
        ByteBuffer greenBlinkBuffer = ByteBuffer.wrap(greenBlinkStream);
        ByteBuffer blueNormalBuffer = ByteBuffer.wrap(blueNormalStream);
        
        Network network = new Network("here", 1, "a");
        Screen screen = new Screen(10, 2, network);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartFieldExtended(redReverseBuffer));
        orders.add(new OrderText("1234"));
        orders.add(new OrderSetBufferAddress(new BufferAddress(5)));
        orders.add(new OrderStartFieldExtended(greenBlinkBuffer));
        orders.add(new OrderText("5678"));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartFieldExtended(blueNormalBuffer));
        orders.add(new OrderText("ABCD"));
        orders.add(new OrderSetBufferAddress(new BufferAddress(15)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("EFGH"));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        assertThat(screen.getColourAtPosition(2)).as("Field 1 should be red").isEqualTo(Colour.RED);
        assertThat(screen.getHighlightAtPosition(2)).as("Field 1 should be reverse").isEqualTo(Highlight.REVERSE);
        
        assertThat(screen.getColourAtPosition(7)).as("Field 2 should be red").isEqualTo(Colour.GREEN);
        assertThat(screen.getHighlightAtPosition(7)).as("Field 2 should be reverse").isEqualTo(Highlight.BLINK);

        assertThat(screen.getColourAtPosition(12)).as("Field 3 should be red").isEqualTo(Colour.BLUE);
        assertThat(screen.getHighlightAtPosition(12)).as("Field 3 should be reverse").isEqualTo(Highlight.NORMAL);
        
        assertThat(screen.getColourAtPosition(17)).as("Field 4 should be standard field").isNull();;
        assertThat(screen.getHighlightAtPosition(17)).as("Field 4 should be standard field").isNull();
        
    }

}
