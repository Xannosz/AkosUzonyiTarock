package com.tisza.tarock.announcement;

import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Team;

public class Kisszincsalad extends Szincsalad
{
	Kisszincsalad(int suit)
	{
		super(suit);
	}

	public String getName()
	{
		return "kisszincsalad";
	}

	public Result isSuccessful(GameState gameState, Team team)
	{
		for (int i = 0; i < 2; i++)
		{
			int roundIndex = 8 - i;
			if (isRoundOK(gameState, team, roundIndex))
			{
				return Result.SUCCESSFUL;
			}
		}
		return Result.FAILED;
	}
	
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		if (announcing.isAnnounced(team, Announcements.nagyszincsaladok[getSuit()]))
			return false;
		
		return super.canBeAnnounced(announcing);
	}

	protected int getPoints()
	{
		return 60;
	}
}