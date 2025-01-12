package com.tisza.tarock.server.database;

import com.tisza.tarock.server.player.*;
import com.tisza.tarock.server.player.bot.*;
import io.reactivex.*;

import java.util.*;

public class User
{
	private final int id;
	private final TarockDatabase database;

	public User(int id, TarockDatabase database)
	{
		this.id = id;
		this.database = database;
	}

	public int getID()
	{
		return id;
	}

	public Single<String> getName()
	{
		return database.getUserName(id);
	}

	public Single<Optional<String>> getImageURL()
	{
		return database.getUserImgURL(id);
	}

	public void setImageURL(String imgURL)
	{
		database.setUserImgURL(id, imgURL);
	}

	public Single<Boolean> isFriendWith(User user)
	{
		return database.areUserFriends(id, user.id);
	}

	public Flowable<String> getFCMTokens()
	{
		return database.getFCMTokensForUser(id);
	}

	public boolean isBot()
	{
		return id < 4;
	}

	public Player createPlayer()
	{
		return isBot() ? new BotPlayer(this, 500, 3000) : new ProtoPlayer(this);
	}

	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof User))
			return false;

		return id == ((User)obj).id;
	}
}
