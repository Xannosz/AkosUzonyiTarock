package com.tisza.tarock.game;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.card.TarockCard;
import com.tisza.tarock.card.filter.CallableCardFilter;
import com.tisza.tarock.card.filter.CardFilter;
import com.tisza.tarock.game.Bidding.Invitation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Calling extends Phase
{
	private int callerPlayer;
	private boolean canCallAnyTarock;
	
	public Calling(GameSession gameSession)
	{
		super(gameSession);
	}
	
	public PhaseEnum asEnum()
	{
		return PhaseEnum.CALLING;
	}

	public void onStart()
	{
		callerPlayer = currentGame.getBidWinnerPlayer();
		
		canCallAnyTarock = false;
		for (Card c : currentGame.getSkartForTeam(Team.OPPONENT))
		{
			if (c instanceof TarockCard)
			{
				canCallAnyTarock = true;
				break;
			}
		}
		
		gameSession.getPlayerEventQueue(callerPlayer).availabeCalls(getCallableCards());
		gameSession.getBroadcastEventSender().turn(callerPlayer);
	}
	
	public void call(int player, Card card)
	{
		if (player != callerPlayer)
			return;
		
		if (!getCallableCards().contains(card))
			return;
		
		int calledPlayer = -1;
		for (int i = 0; i < 4; i++)
		{
			PlayerCards pc = currentGame.getPlayerCards(i);
			if (pc.hasCard(card))
			{
				calledPlayer = i;
			}
		}
		
		currentGame.setSoloIntentional(calledPlayer == callerPlayer);
		
		//if the player called a card that had been skarted
		if (calledPlayer < 0)
		{
			calledPlayer = callerPlayer;
			
			if (card.equals(new TarockCard(20)) && currentGame.getPlayerSkarted20() != callerPlayer)
			{
				if (currentGame.getPlayerSkarted20() < 0)
					throw new RuntimeException();
				currentGame.setPlayerToAnnounceSolo(currentGame.getPlayerSkarted20());
			}
		}

		currentGame.setPlayerPairs(new PlayerPairs(callerPlayer, calledPlayer));
		
		if (currentGame.getInvitSent() == Invitation.XVIII && card.equals(new TarockCard(18)) || currentGame.getInvitSent() == Invitation.XIX && card.equals(new TarockCard(19)))
		{
			currentGame.invitAccepted();
		}

		gameSession.getBroadcastEventSender().call(player, card);
		gameSession.changePhase(new Announcing(gameSession));
	}
	
	private List<Card> getCallableCards()
	{
		Set<Card> callOptions = new LinkedHashSet<Card>();
		
		if (currentGame.getInvitSent() == Invitation.XIX)
		{
			Card c = new TarockCard(19);
			callOptions.add(c);
		}
		
		if (currentGame.getInvitSent() == Invitation.XVIII)
		{
			Card c = new TarockCard(18);
			callOptions.add(c);
		}
		
		PlayerCards pc = currentGame.getPlayerCards(callerPlayer);
		for (int t = 20; t >= 1; t--)
		{
			TarockCard c = new TarockCard(t);
			if (!pc.hasCard(c))
			{
				callOptions.add(c);
				break;
			}
		}
		
		if (canCallAnyTarock)
		{
			CardFilter cf = new CallableCardFilter();
			for (Card c : Card.all)
			{
				if (cf.match(c))
				{
					callOptions.add(c);
				}
			}
		}
		else
		{
			callOptions.addAll(pc.filter(new CallableCardFilter()));
		}
		
		return new ArrayList<Card>(callOptions);
	}
}