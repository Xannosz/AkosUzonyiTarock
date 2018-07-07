package com.tisza.tarock.server;

import com.tisza.tarock.message.*;

import java.util.*;

public class User
{
	private final String id;
	private final String name;
	private final String imgURL;

	private boolean loggedIn = false;
	private Map<Integer, ProtoPlayer> gameIDToPlayer = new HashMap<>();

	public User(String id, String name)
	{
		this(id, name, null);
	}

	public User(String id, String name, String imgURL)
	{
		this.id = id;
		this.name = name;
		this.imgURL = imgURL;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getImageURL()
	{
		return imgURL;
	}

	public boolean isLoggedIn()
	{
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn)
	{
		this.loggedIn = loggedIn;
	}

	public ProtoPlayer createPlayerForGame(int gameID)
	{
		ProtoPlayer player = new ProtoPlayer(name);
		Player prev = gameIDToPlayer.put(gameID, player);
		if (prev != null)
			System.err.println("WARNING: player is overridden for a game");
		return player;
	}

	public ProtoPlayer getPlayerForGame(int gameID)
	{
		return gameIDToPlayer.get(gameID);
	}

	public void removePlayerForGame(int gameID)
	{
		gameIDToPlayer.remove(gameID);
	}
}
