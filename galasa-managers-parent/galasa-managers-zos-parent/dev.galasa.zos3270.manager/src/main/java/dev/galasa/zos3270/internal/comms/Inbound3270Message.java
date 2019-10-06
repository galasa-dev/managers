/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270.internal.comms;

import java.util.List;

import dev.galasa.zos3270.internal.datastream.CommandCode;
import dev.galasa.zos3270.internal.datastream.Order;
import dev.galasa.zos3270.internal.datastream.StructuredField;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;

public class Inbound3270Message {
	
	private final CommandCode commandCode;
	private final WriteControlCharacter writeControlCharacter;
	private final List<Order> orders;
	private final List<StructuredField> structuredFields;
	
	public Inbound3270Message(CommandCode commandCode,
			WriteControlCharacter writeControlCharacter,
			List<Order> orders) {
		this.commandCode           = commandCode;
		this.writeControlCharacter = writeControlCharacter;
		this.orders                = orders;
		this.structuredFields      = null;
	}

	public Inbound3270Message(CommandCode commandCode,
			List<StructuredField> structuredFields) {
		this.commandCode           = commandCode;
		this.writeControlCharacter = null;
		this.orders                = null;
		this.structuredFields      = structuredFields;
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

	public List<StructuredField> getStructuredFields() {
		return structuredFields;
	}
}
