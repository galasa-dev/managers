/*
 * Copyright contributors to the Galasa project
 */
package test.zos3270.datastream.extended;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.zos3270.TerminalInterruptedException;
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
import dev.galasa.zos3270.Color;
import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.spi.Highlight;
import dev.galasa.zos3270.spi.Screen;

public class ExtendedDatastreamTest {
    
    private final String RED   = "42f2";
    private final String GREEN = "42f4";
    private final String BLUE  = "42f1";

    private final String REVERSE    = "41f2";
    private final String BLINK      = "41f1";
    private final String NORMAL     = "41f0";
    private final String UNDERSCORE = "41f4";

    private final String CHARS = "c0f8";

    private Screen screen;

    private ByteBuffer getBuffer(String data) throws DecoderException {
        byte[] stream = Hex.decodeHex(data);
        return ByteBuffer.wrap(stream);
    }

    @Before
    public void setUp() throws TerminalInterruptedException {
        Network network = new Network("here", 1, "a");
        screen = new Screen(10, 2, network);
        screen.erase();
    }

    // Note: Colour was deprecated in v0.28.0, replaced by Color in the public package.
    @Deprecated(since = "0.28.0", forRemoval = true)
    @Test
    public void testColourAndHighlighting() throws DecoderException, DatastreamException {
        String redReverse = "03" + REVERSE + RED + CHARS;
        String greenBlink = "03" + BLINK + GREEN + CHARS;
        String blueNormal = "03" + NORMAL + BLUE + CHARS;
        ByteBuffer redReverseBuffer = getBuffer(redReverse);
        ByteBuffer greenBlinkBuffer = getBuffer(greenBlink);
        ByteBuffer blueNormalBuffer = getBuffer(blueNormal);

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
        
        assertThat(screen.getColourAtPosition(7)).as("Field 2 should be green").isEqualTo(Colour.GREEN);
        assertThat(screen.getHighlightAtPosition(7)).as("Field 2 should be blinking").isEqualTo(Highlight.BLINK);

        assertThat(screen.getColourAtPosition(12)).as("Field 3 should be blue").isEqualTo(Colour.BLUE);
        assertThat(screen.getHighlightAtPosition(12)).as("Field 3 should be normal").isEqualTo(Highlight.NORMAL);
        
        assertThat(screen.getColourAtPosition(17)).as("Field 4 should be standard field").isNull();;
        assertThat(screen.getHighlightAtPosition(17)).as("Field 4 should be standard field").isNull();
    }

    @Test
    public void testColorAndHighlighting() throws DecoderException, DatastreamException {
        String redReverse = "03" + REVERSE + RED + CHARS;
        String greenBlink = "03" + BLINK + GREEN + CHARS;
        String blueNormal = "03" + NORMAL + BLUE + CHARS;
        ByteBuffer redReverseBuffer = getBuffer(redReverse);
        ByteBuffer greenBlinkBuffer = getBuffer(greenBlink);
        ByteBuffer blueNormalBuffer = getBuffer(blueNormal);

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
        
        assertThat(screen.getColorAtPosition(2)).as("Field 1 should be red").isEqualTo(Color.RED);
        assertThat(screen.getHighlightAtPosition(2)).as("Field 1 should be reverse").isEqualTo(Highlight.REVERSE);
        
        assertThat(screen.getColorAtPosition(7)).as("Field 2 should be green").isEqualTo(Color.GREEN);
        assertThat(screen.getHighlightAtPosition(7)).as("Field 2 should be blinking").isEqualTo(Highlight.BLINK);

        assertThat(screen.getColorAtPosition(12)).as("Field 3 should be blue").isEqualTo(Color.BLUE);
        assertThat(screen.getHighlightAtPosition(12)).as("Field 3 should be normal").isEqualTo(Highlight.NORMAL);
        
        assertThat(screen.getColorAtPosition(17)).as("Field 4 should be standard field").isNull();;
        assertThat(screen.getHighlightAtPosition(17)).as("Field 4 should be standard field").isNull();
    }

    @Deprecated(since = "0.28.0", forRemoval = true)
    @Test
    public void testColourNoHighlighting() throws DecoderException, DatastreamException {
        String red = "02" + RED + CHARS;
        String green = "02" + GREEN + CHARS;
        String blue = "02" + BLUE + CHARS;
        ByteBuffer redBuffer = getBuffer(red);
        ByteBuffer greenBuffer = getBuffer(green);
        ByteBuffer blueBuffer = getBuffer(blue);

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartFieldExtended(redBuffer));
        orders.add(new OrderText("1234"));
        orders.add(new OrderSetBufferAddress(new BufferAddress(5)));
        orders.add(new OrderStartFieldExtended(greenBuffer));
        orders.add(new OrderText("5678"));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartFieldExtended(blueBuffer));
        orders.add(new OrderText("ABCD"));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        assertThat(screen.getColourAtPosition(2)).as("Field 1 should be red").isEqualTo(Colour.RED);
        assertThat(screen.getHighlightAtPosition(2)).as("Field 1 should have no highlighting").isNull();
        
        assertThat(screen.getColourAtPosition(7)).as("Field 2 should be green").isEqualTo(Colour.GREEN);
        assertThat(screen.getHighlightAtPosition(7)).as("Field 2 should have no highlighting").isNull();

        assertThat(screen.getColourAtPosition(12)).as("Field 3 should be blue").isEqualTo(Colour.BLUE);
        assertThat(screen.getHighlightAtPosition(12)).as("Field 3 should have no highlighting").isNull();
    }

    @Test
    public void testColorNoHighlighting() throws DecoderException, DatastreamException {
        String red = "02" + RED + CHARS;
        String green = "02" + GREEN + CHARS;
        String blue = "02" + BLUE + CHARS;
        ByteBuffer redBuffer = getBuffer(red);
        ByteBuffer greenBuffer = getBuffer(green);
        ByteBuffer blueBuffer = getBuffer(blue);

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartFieldExtended(redBuffer));
        orders.add(new OrderText("1234"));
        orders.add(new OrderSetBufferAddress(new BufferAddress(5)));
        orders.add(new OrderStartFieldExtended(greenBuffer));
        orders.add(new OrderText("5678"));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartFieldExtended(blueBuffer));
        orders.add(new OrderText("ABCD"));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        assertThat(screen.getColorAtPosition(2)).as("Field 1 should be red").isEqualTo(Color.RED);
        assertThat(screen.getHighlightAtPosition(2)).as("Field 1 should have no highlighting").isNull();
        
        assertThat(screen.getColorAtPosition(7)).as("Field 2 should be green").isEqualTo(Color.GREEN);
        assertThat(screen.getHighlightAtPosition(7)).as("Field 2 should have no highlighting").isNull();

        assertThat(screen.getColorAtPosition(12)).as("Field 3 should be blue").isEqualTo(Color.BLUE);
        assertThat(screen.getHighlightAtPosition(12)).as("Field 3 should have no highlighting").isNull();
    }

    @Deprecated(since = "0.28.0", forRemoval = true)
    @Test
    public void testHighlightingNoColour() throws DecoderException, DatastreamException {
        String normal = "02" + NORMAL + CHARS;
        String blink = "02" + BLINK + CHARS;
        String underscore = "02" + UNDERSCORE + CHARS;
        ByteBuffer blinkBuffer = getBuffer(blink);
        ByteBuffer underscoreBuffer = getBuffer(underscore);
        ByteBuffer normalBuffer = getBuffer(normal);

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartFieldExtended(blinkBuffer));
        orders.add(new OrderText("1234"));
        orders.add(new OrderSetBufferAddress(new BufferAddress(5)));
        orders.add(new OrderStartFieldExtended(underscoreBuffer));
        orders.add(new OrderText("5678"));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartFieldExtended(normalBuffer));
        orders.add(new OrderText("ABCD"));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        assertThat(screen.getHighlightAtPosition(2)).as("Field 1 should be blinking").isEqualTo(Highlight.BLINK);
        assertThat(screen.getColourAtPosition(2)).as("Field 1 should have no colour").isNull();
        
        assertThat(screen.getHighlightAtPosition(7)).as("Field 2 should be underscore").isEqualTo(Highlight.UNDERSCORE);
        assertThat(screen.getColourAtPosition(7)).as("Field 2 should have no colour").isNull();

        assertThat(screen.getHighlightAtPosition(12)).as("Field 3 should be normal").isEqualTo(Highlight.NORMAL);
        assertThat(screen.getColourAtPosition(12)).as("Field 3 should have no colour").isNull();
    }

    @Test
    public void testHighlightingNoColor() throws DecoderException, DatastreamException {
        String normal = "02" + NORMAL + CHARS;
        String blink = "02" + BLINK + CHARS;
        String underscore = "02" + UNDERSCORE + CHARS;
        ByteBuffer blinkBuffer = getBuffer(blink);
        ByteBuffer underscoreBuffer = getBuffer(underscore);
        ByteBuffer normalBuffer = getBuffer(normal);

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartFieldExtended(blinkBuffer));
        orders.add(new OrderText("1234"));
        orders.add(new OrderSetBufferAddress(new BufferAddress(5)));
        orders.add(new OrderStartFieldExtended(underscoreBuffer));
        orders.add(new OrderText("5678"));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartFieldExtended(normalBuffer));
        orders.add(new OrderText("ABCD"));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        assertThat(screen.getHighlightAtPosition(2)).as("Field 1 should be blinking").isEqualTo(Highlight.BLINK);
        assertThat(screen.getColorAtPosition(2)).as("Field 1 should have no colour").isNull();
        
        assertThat(screen.getHighlightAtPosition(7)).as("Field 2 should be underscore").isEqualTo(Highlight.UNDERSCORE);
        assertThat(screen.getColorAtPosition(7)).as("Field 2 should have no colour").isNull();

        assertThat(screen.getHighlightAtPosition(12)).as("Field 3 should be normal").isEqualTo(Highlight.NORMAL);
        assertThat(screen.getColorAtPosition(12)).as("Field 3 should have no colour").isNull();
    }
}
