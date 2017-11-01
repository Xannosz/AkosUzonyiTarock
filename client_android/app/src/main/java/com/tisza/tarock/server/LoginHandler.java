package com.tisza.tarock.server;

import com.tisza.tarock.net.PacketHandler;
import com.tisza.tarock.net.packet.Packet;
import com.tisza.tarock.net.packet.PacketLogin;
import com.tisza.tarock.net.packet.PacketLoginFailed;

public class LoginHandler implements PacketHandler
{
	private LoginManager loginManager;
	private String name = null;
	
	public LoginHandler(LoginManager lm)
	{
		loginManager = lm;
	}
	
	public void handlePacket(Packet p)
	{
		if (p instanceof PacketLogin)
		{
			PacketLogin pl = (PacketLogin)p;
			name = pl.getName();
			loginManager.loginAuthorized(this, name);
		}
	}

	public void connectionClosed()
	{
		if (name != null)
		{
			loginManager.loginFailed(this, PacketLoginFailed.Reason.CONNECTION_ERROR);
		}
	}
}