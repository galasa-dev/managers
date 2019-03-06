package test.zos3270.terminal;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import io.ejat.zos3270.internal.datastream.BufferAddress;
import io.ejat.zos3270.internal.datastream.Order;
import io.ejat.zos3270.internal.datastream.OrderInsertCursor;
import io.ejat.zos3270.internal.datastream.OrderRepeatToAddress;
import io.ejat.zos3270.internal.datastream.OrderSetBufferAddress;
import io.ejat.zos3270.internal.datastream.OrderStartField;
import io.ejat.zos3270.internal.datastream.OrderText;
import io.ejat.zos3270.spi.DatastreamException;
import io.ejat.zos3270.spi.Screen;

public class ScreenTest {
	
	@Test
	public void testScreenSize() {
		Assert.assertEquals("default screen size incorrect", 1920, new Screen().getScreenSize());
		Assert.assertEquals("small screen size incorrect", 20, new Screen(10,2).getScreenSize());
	}

	
	@Test
	public void testErase() {
		Screen screen = new Screen(10, 2);
		screen.erase();
		
		Assert.assertEquals("Erase fields are incorrect", "Chars( ,0-19)", screen.printFields());
	}
	
	
	@Test
	public void testEraseUsingRA() throws DatastreamException {
		Screen screen = new Screen(10, 2);
		ArrayList<Order> orders = new ArrayList<>();
		orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
		orders.add(new OrderRepeatToAddress((char) 0x00, new BufferAddress(0)));
		
		screen.processOrders(orders);
		
		Assert.assertEquals("Clear fields are incorrect", "Chars( ,0-19)", screen.printFields());
	}
	
	
	@Test
	public void testOrders() throws DatastreamException {
		Screen screen = new Screen(10, 2);
		screen.erase();
		
		ArrayList<Order> orders = new ArrayList<>();
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
		
		screen.processOrders(orders);
		
		String fields = screen.printFields();
		Assert.assertEquals("Screen layout is incorrect", 
				"StartOfField(0)\n" + 
		        "Text(Hello,1-5)\n" + 
				"StartOfField(6)\n" + 
		        "Chars(X,7-9)\n" +
				"StartOfField(10)\n" +
		        "Text(yyyzzz,11-16)\n" + 
				"StartOfField(17)\n" +
		        "Chars( ,18-19)",
		        fields);
	}
	
	@Test
	public void testOrdersInsertAndTail() throws DatastreamException {
		Screen screen = new Screen(10, 2);
		
		ArrayList<Order> orders = new ArrayList<>();
		orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
		orders.add(new OrderRepeatToAddress('x', new BufferAddress(19)));
		orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
		orders.add(new OrderStartField(true, false, true, false, false, false));
		orders.add(new OrderSetBufferAddress(new BufferAddress(19)));
		orders.add(new OrderStartField(true, false, true, false, false, false));
		
		screen.processOrders(orders);
		
		String fields = screen.printFields();
		Assert.assertEquals("Screen layout is incorrect", 
				"StartOfField(0)\n" + 
		        "Chars(x,1-18)\n" +
				"StartOfField(19)", 
		        fields);
	}

	@Test
	public void testOrdersJumbled() throws DatastreamException {
		Screen screen = new Screen(10, 2);
		
		ArrayList<Order> orders = new ArrayList<>();
		orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
		orders.add(new OrderStartField(false, false, false, false, false, false));
		orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
		orders.add(new OrderStartField(false, false, false, false, false, false));
		orders.add(new OrderSetBufferAddress(new BufferAddress(11)));
		orders.add(new OrderText("123456789"));
		orders.add(new OrderSetBufferAddress(new BufferAddress(1)));
		orders.add(new OrderText("abcdefghi"));
		
		screen.processOrders(orders);
		
		String fields = screen.printFields();
		Assert.assertEquals("Screen layout is incorrect", 
				"StartOfField(0)\n" + 
		        "Text(abcdefghi,1-9)\n" + 
				"StartOfField(10)\n" + 
		        "Text(123456789,11-19)",  
		        fields);
	}

	@Test
	public void testOrdersReplacedAll() throws DatastreamException {
		Screen screen = new Screen(10, 2);
		screen.erase();
		
		ArrayList<Order> orders = new ArrayList<>();
		orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
		orders.add(new OrderStartField(false, false, false, false, false, false));
		orders.add(new OrderRepeatToAddress('X', new BufferAddress(20)));
		orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
		orders.add(new OrderRepeatToAddress('X', new BufferAddress(20)));
		
		screen.processOrders(orders);
		
		String fields = screen.printFields();
		Assert.assertEquals("Screen layout is incorrect", 
				"Chars(X,0-19)",  
		        fields);
	}
	
	@Test
	public void testOrderReplaceMiddle() throws DatastreamException {
		Screen screen = new Screen(10, 2);
		
		ArrayList<Order> orders = new ArrayList<>();
		orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
		orders.add(new OrderRepeatToAddress('X', new BufferAddress(10)));
		orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
		orders.add(new OrderRepeatToAddress('Y', new BufferAddress(20)));
		orders.add(new OrderSetBufferAddress(new BufferAddress(5)));
		orders.add(new OrderRepeatToAddress('Z', new BufferAddress(10)));
		
		screen.processOrders(orders);
		
		String fields = screen.printFields();
		Assert.assertEquals("Screen layout is incorrect", 
				"Text(XXXXXZZZZZYYYYYYYYYY,0-19)",  
		        fields);
	}

}
