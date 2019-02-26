package io.ejat.zos3270.internal.comms;

import java.util.List;

import io.ejat.zos3270.internal.datastream.CommandCode;
import io.ejat.zos3270.internal.datastream.Order;
import io.ejat.zos3270.internal.datastream.WriteControlCharacter;

public class Inbound3270Message {
	
	private CommandCode commandCode;
	private WriteControlCharacter writeControlCharacter;
	private List<Order> orders;
	
	public Inbound3270Message(CommandCode commandCode,
			WriteControlCharacter writeControlCharacter,
			List<Order> orders) {
		this.commandCode = commandCode;
		this.writeControlCharacter = writeControlCharacter;
		this.orders = orders;
	}

	public CommandCode getCommandCode() {
		return commandCode;
	}

	public WriteControlCharacter getWriteControlCharacter() {
		return writeControlCharacter;
	}

	public List<Order> getOrders() {
		return orders;
	}
}
