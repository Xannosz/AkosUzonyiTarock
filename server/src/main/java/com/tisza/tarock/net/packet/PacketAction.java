package com.tisza.tarock.net.packet;

import java.io.*;
import com.tisza.tarock.proto.ActionProto.*;


public class PacketAction extends Packet
{
	private Action action;
	
	PacketAction() {}
	
	public PacketAction(Action action)
	{
		this.action = action;
	}

	public Action getAction()
	{
		return action;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		action = Action.parseDelimitedFrom(dis);
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		action.writeDelimitedTo(dos);
	}
}
