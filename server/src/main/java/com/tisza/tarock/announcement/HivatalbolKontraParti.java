package com.tisza.tarock.announcement;

import com.tisza.tarock.game.AnnouncementContra;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Team;

public class HivatalbolKontraParti extends AnnouncementWrapper
{
	public HivatalbolKontraParti()
	{
		super(new AnnouncementContra(Announcements.jatek, 1));
	}

	@Override
	public String getName()
	{
		return "hkp";
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		if (announcing.isAnnounced(announcing.getCurrentTeam(), Announcements.hkp))
			return false;
		
		if (announcing.getCurrentPlayer() != announcing.getPlayerToAnnounceSolo())
			return false;
		
		return announcing.getContraLevel(Team.CALLER, Announcements.jatek) == 0;
	}

	@Override
	public boolean requireIdentification()
	{
		return false;
	}
}
