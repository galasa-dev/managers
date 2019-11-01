/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package test.zos3270.terminal;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.datastream.BufferAddress;
import dev.galasa.zos3270.internal.datastream.CommandEraseWrite;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.OrderInsertCursor;
import dev.galasa.zos3270.internal.datastream.OrderRepeatToAddress;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.spi.Screen;

public class ScreenTest {

    @Test
    public void testScreenSize() throws InterruptedException {
        Assert.assertEquals("default screen size incorrect", 1920, new Screen().getScreenSize());
        Assert.assertEquals("small screen size incorrect", 20, new Screen(10, 2, null).getScreenSize());
    }

    @Test
    public void testErase() throws InterruptedException {
        Screen screen = new Screen(10, 2, null);
        screen.erase();

        Assert.assertEquals("Erase fields are incorrect",
                "Field(pos=0,p=false,n=false,d=true,i=false,s=false,m=false,                    )\n",
                screen.printFields());

    }

    @Test
    public void testEraseUsingRA() throws DatastreamException, InterruptedException {
        Screen screen = new Screen(10, 2, null);
        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderRepeatToAddress((char) 0x00, new BufferAddress(0)));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        Assert.assertEquals("Clear fields are incorrect",
                "Field(pos=0,p=false,n=false,d=true,i=false,s=false,m=false,                    )\n",
                screen.printFields());
    }

    @Test
    public void testOrders() throws DatastreamException, InterruptedException {
        Screen screen = new Screen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("Hello"));
        orders.add(new OrderStartField(false, false, false, false, false, false));
        orders.add(new OrderInsertCursor());
        orders.add(new OrderRepeatToAddress('X', new BufferAddress(10)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderRepeatToAddress('y', new BufferAddress(14)));
        orders.add(new OrderRepeatToAddress('z', new BufferAddress(17)));
        orders.add(new OrderStartField(true, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        String fields = screen.printFields();
        Assert.assertEquals("Screen layout is incorrect",
                "Field(pos=0,p=true,n=false,d=true,i=false,s=false,m=false,Hello)\n"
                        + "Field(pos=6,p=false,n=false,d=false,i=false,s=false,m=false,XXX)\n"
                        + "Field(pos=10,p=true,n=false,d=true,i=false,s=false,m=false,yyyzzz)\n"
                        + "Field(pos=17,p=true,n=false,d=true,i=false,s=false,m=false,  )\n",
                fields);
    }

    @Test
    public void testOrdersInsertAndTail() throws DatastreamException, InterruptedException {
        Screen screen = new Screen(10, 2, null);

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderRepeatToAddress('x', new BufferAddress(19)));
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(19)));
        orders.add(new OrderStartField(true, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        String fields = screen.printFields();
        Assert.assertEquals("Screen layout is incorrect",
                "Field(pos=0,p=true,n=false,d=true,i=false,s=false,m=false,xxxxxxxxxxxxxxxxxx)\n"
                        + "Field(pos=19,p=true,n=false,d=true,i=false,s=false,m=false,)\n",
                fields);
    }

    @Test
    public void testOrdersJumbled() throws DatastreamException, InterruptedException {
        Screen screen = new Screen(10, 2, null);

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, false, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, false, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(11)));
        orders.add(new OrderText("123456789"));
        orders.add(new OrderSetBufferAddress(new BufferAddress(1)));
        orders.add(new OrderText("abcdefghi"));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        String fields = screen.printFields();
        Assert.assertEquals("Screen layout is incorrect",
                "Field(pos=0,p=false,n=false,d=false,i=false,s=false,m=false,abcdefghi)\n"
                        + "Field(pos=10,p=false,n=false,d=false,i=false,s=false,m=false,123456789)\n",
                fields);
    }

    @Test
    public void testOrdersReplacedAll() throws DatastreamException, InterruptedException {
        Screen screen = new Screen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, false, false, false, false));
        orders.add(new OrderRepeatToAddress('X', new BufferAddress(20)));
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderRepeatToAddress('X', new BufferAddress(20)));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        String fields = screen.printFields();
        Assert.assertEquals("Screen layout is incorrect",
                "Field(pos=0,p=false,n=false,d=true,i=false,s=false,m=false,XXXXXXXXXXXXXXXXXXXX)\n", fields);
    }

    @Test
    public void testOrderReplaceMiddle() throws DatastreamException, InterruptedException {
        Screen screen = new Screen(10, 2, null);

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderRepeatToAddress('X', new BufferAddress(10)));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderRepeatToAddress('Y', new BufferAddress(20)));
        orders.add(new OrderSetBufferAddress(new BufferAddress(5)));
        orders.add(new OrderRepeatToAddress('Z', new BufferAddress(10)));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        String fields = screen.printFields();
        Assert.assertEquals("Screen layout is incorrect",
                "Field(pos=0,p=false,n=false,d=true,i=false,s=false,m=false,XXXXXZZZZZYYYYYYYYYY)\n", fields);
    }

}
