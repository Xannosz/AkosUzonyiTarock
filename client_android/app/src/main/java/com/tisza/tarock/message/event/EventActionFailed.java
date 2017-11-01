package com.tisza.tarock.message.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.tisza.tarock.message.EventHandler;

public class EventActionFailed extends Event
{
	private Reason type;
	
	public EventActionFailed(){}
	
	public EventActionFailed(Reason type)
	{
		this.type = type;
	}
	
	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(type.ordinal());
	}

	public void readData(DataInputStream dis) throws IOException
	{
		type = Reason.values()[dis.readByte()];
	}

	public void handle(EventHandler handler)
	{
		handler.wrongAction(type);
	}
	
	public static enum Reason
	{
		OTHERS_TURN, CONTRAJATEK_REQUIRED, WRONG_SKART_COUNT, INVALID_SKART, INVALID_CARD;
	}
}