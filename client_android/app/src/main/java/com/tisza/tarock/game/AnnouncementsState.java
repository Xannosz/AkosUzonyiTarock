package com.tisza.tarock.game;

import java.util.HashMap;
import java.util.Map;

import com.tisza.tarock.announcement.Announcement;

public class AnnouncementsState
{
	private Map<Team, Map<Announcement, Integer>> announcementContraLevels = new HashMap<Team, Map<Announcement, Integer>>();

	public AnnouncementsState()
	{
		for (Team team : Team.values())
		{
			Map<Announcement, Integer> map = new HashMap<Announcement, Integer>();
			announcementContraLevels.put(team, map);
		}
	}
	
	public boolean isAnnounced(Team team, Announcement a)
	{
		return announcementContraLevels.get(team).containsKey(a);
	}
	
	public void setContraLevel(Team team, Announcement a, int level)
	{
		announcementContraLevels.get(team).put(a, level);
	}
	
	public int getContraLevel(Team team, Announcement a)
	{
		Integer contraLevel = announcementContraLevels.get(team).get(a);
		if (contraLevel == null)
			throw new IllegalStateException();
		return contraLevel;
	}
	
	public void clearAnnouncement(Team team, Announcement announcement)
	{
		announcementContraLevels.get(team).remove(announcement);
	}
}