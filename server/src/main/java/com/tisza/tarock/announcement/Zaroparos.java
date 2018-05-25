package com.tisza.tarock.announcement;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.TarockCard;
import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Round;
import com.tisza.tarock.game.Team;

public class Zaroparos extends AnnouncementBase
{
	Zaroparos(){}

	public String getName()
	{
		return "zaroparos";
	}

	public Result isSuccessful(GameState gameState, Team team)
	{
		if (!isRoundOK(gameState, team, 8, new TarockCard(1)))
			return Result.FAILED;
		
		if (!isRoundOK(gameState, team, 7, new TarockCard(2)))
			return Result.FAILED;
		
		return Result.SUCCESSFUL;
	}
	
	private boolean isRoundOK(GameState gameState, Team team, int roundIndex, Card cardToTakeWith)
	{
		Round round = gameState.getRound(roundIndex);
		int theCardPlayer = round.getPlayerOfCard(cardToTakeWith);
		if (theCardPlayer < 0) return false;
		
		if (gameState.getPlayerPairs().getTeam(theCardPlayer) != team)
		{
			return false;
		}
		else
		{
			return round.getWinner() == theCardPlayer;
		}
	}
	
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Announcement a0 = Announcements.ultimok.get(new TarockCard(1)).get(6);
		Announcement a1 = Announcements.ultimok.get(new TarockCard(2)).get(6);
		if (!a0.canBeAnnounced(announcing))
			return false;
		if  (!a1.canBeAnnounced(announcing))
			return false;
		
		return super.canBeAnnounced(announcing);
	}
	
	public void onAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		for (int t = 1; t <= 2; t++)
		{
			for (int ri = 8; ri > 6; ri--)
			{
				Announcement ultimo = Announcements.ultimok.get(new TarockCard(t)).get(ri);
				announcing.clearAnnouncement(team, ultimo);
			}
		}
	}

	protected int getPoints()
	{
		return 40;
	}
}