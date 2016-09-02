package com.tisza.tarock.gui;

import java.io.*;
import java.net.*;
import java.util.*;

import android.app.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.*;
import android.widget.*;

import com.tisza.tarock.*;
import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.net.packet.*;

public class GameActivtiy extends Activity implements PacketHandler
{
	private int cardWidth;
	
	private TextView[] playerNameViews;
	private LinearLayout myCardsView0;
	private LinearLayout myCardsView1;
	private FrameLayout center_space;
	private Button okButton;
	
	private View biddingView;
	private ScrollView biddingScrollView;
	private TextView biddingTextView;
	private LinearLayout availabeBidsView;
	
	private RelativeLayout played_cards;
	private PlacedCardView[] playedCardViews;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		ResourceMappings.init(this);
			
		cardWidth = getWindowManager().getDefaultDisplay().getWidth() / 6;
		
		View game = View.inflate(this, R.layout.game, null);
		
		center_space = (FrameLayout)game.findViewById(R.id.center_space);
		
		playerNameViews = new TextView[]
		{
				null,
				(TextView)game.findViewById(R.id.playername_1),
				(TextView)game.findViewById(R.id.playername_2),
				(TextView)game.findViewById(R.id.playername_3),
		};
		
		biddingView = View.inflate(this, R.layout.bidding, null);
		biddingScrollView = (ScrollView)biddingView.findViewById(R.id.bidding_scroll);
		biddingTextView = (TextView)biddingView.findViewById(R.id.bidding_text);
		availabeBidsView = (LinearLayout)biddingView.findViewById(R.id.available_bids);
				
		myCardsView0 = (LinearLayout)game.findViewById(R.id.my_cards_0);
		myCardsView1 = (LinearLayout)game.findViewById(R.id.my_cards_1);
		
		okButton = (Button)game.findViewById(R.id.ok_button);
		
		played_cards = new RelativeLayout(this);
		played_cards.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		playedCardViews = new PlacedCardView[4];
		for (int i = 0; i < 4; i++)
		{
			playedCardViews[i] = new PlacedCardView(this, cardWidth, i);
			played_cards.addView(playedCardViews[i]);
		}
		center_space.addView(biddingView);
		
		setContentView(game);
		
		final String host = getIntent().getStringExtra("host");
		final int port = getIntent().getIntExtra("port", 8128);
		final String name = getIntent().getStringExtra("name");
		
		Thread connThread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					Socket socket = new Socket();
					socket.connect(new InetSocketAddress(host, port), 1000);
					conncection = new Connection(socket);
					conncection.sendPacket(new PacketLogin(name));
					conncection.addPacketHandler(new PacketHandler()
					{
						public void handlePacket(final Packet p)
						{
							runOnUiThread(new Runnable()
							{
								public void run()
								{
									GameActivtiy.this.handlePacket(p);
								}
							});
						}
						

						public void connectionClosed()
						{
							GameActivtiy.this.connectionClosed();
						}
					});
				}
				catch (IOException e)
				{
					e.printStackTrace();
					finish();
				}
			}
		});
		connThread.start();
		try
		{
			connThread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private Connection conncection;
	private List<String> playerNames;
	private PlayerCards myCards;
	private int myID = -1;
	private int cardsPlayed = 0;
	private Map<Card, View> cardToViewMapping = new HashMap<Card, View>();
	
	private List<Card> cardsToSkart = new ArrayList<Card>();
	private int numSkart;
	private boolean skarting = false;

	public void handlePacket(Packet p)
	{
		Handler handler = new Handler();
		if (p instanceof PacketStartGame)
		{
			PacketStartGame packet = ((PacketStartGame)p);
			myID = packet.getPlayerID();
			playerNames = packet.getNames();
			for (int i = 0; i < 4; i++)
			{
				int pos = getPositionFromPlayerID(i);
				if (pos != 0)
				{
					playerNameViews[pos].setText(playerNames.get(i));
					playerNameViews[pos].invalidate();
				}
			}
		}
		if (p instanceof PacketPlayerCards)
		{
			PacketPlayerCards packet = ((PacketPlayerCards)p);
			myCards = packet.getPlayerCards();
			arrangeCards();
		}
		if (p instanceof PacketAvailableBids)
		{
			PacketAvailableBids packet = ((PacketAvailableBids)p);
			List<Integer> bids = packet.getAvailableBids();
			availabeBidsView.removeAllViews();
			for (final int bid : bids)
			{
				Button bidButton = new Button(this);
				bidButton.setText(bid + "");
				bidButton.setOnClickListener(new OnClickListener()
				{
					public void onClick(View v)
					{
						conncection.sendPacket(new PacketBid(bid, myID));
					}
				});
				availabeBidsView.addView(bidButton);
			}
		}
		if (p instanceof PacketBid)
		{
			PacketBid packet = ((PacketBid)p);
			String bidText = biddingTextView.getText().toString();
			if (bidText == null) bidText = "";
			bidText += playerNames.get(packet.getPlayer()) + " licitalt: " + packet.getBid() + "\n";
			biddingTextView.setText(bidText);
		}
		if (p instanceof PacketChange)
		{
			PacketChange packet = ((PacketChange)p);
			myCards.getCards().addAll(packet.getCards());
			skarting = true;
			numSkart = packet.getCards().size();
			arrangeCards();
			
			okButton.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					conncection.sendPacket(new PacketChange(cardsToSkart, myID));
				}
			});
		}
		if (p instanceof PacketChangeDone)
		{
			PacketChangeDone packet = ((PacketChangeDone)p);
			if (packet.getPlayer() == myID)
			{
				okButton.setOnClickListener(null);
				myCards.getCards().removeAll(cardsToSkart);
				skarting = false;
				arrangeCards();
			}
		}
		if (p instanceof PacketAvailableCalls)
		{
			PacketAvailableCalls packet = ((PacketAvailableCalls)p);
			List<Card> calls = packet.getAvailableCalls();
			availabeBidsView.removeAllViews();
			for (final Card card : calls)
			{
				Button callButton = new Button(this);
				callButton.setText(card.toString());
				callButton.setOnClickListener(new OnClickListener()
				{
					public void onClick(View v)
					{
						conncection.sendPacket(new PacketCall(card, myID));
						availabeBidsView.removeAllViews();
						availabeBidsView.invalidate();
					}
				});
				availabeBidsView.addView(callButton);
			}
		}
		if (p instanceof PacketCall)
		{
			PacketCall packet = ((PacketCall)p);
			String bidText = biddingTextView.getText().toString();
			bidText += playerNames.get(packet.getPlayer()) + " hivott: " + packet.getCalledCard() + "\n";
			biddingTextView.setText(bidText);
		}
		if (p instanceof PacketAnnounce)
		{
			PacketAnnounce packet = ((PacketAnnounce)p);
			String bidText = biddingTextView.getText().toString();
			String annName = packet.getAnnouncement() == null ? "passz" : packet.getAnnouncement().getClass().getSimpleName();
			bidText += playerNames.get(packet.getPlayer()) + " bemondta: " + annName + "\n";
			biddingTextView.setText(bidText);
		}
		if (p instanceof PacketTurn)
		{
			PacketTurn packet = ((PacketTurn)p);
			if (packet.getPlayer() == myID)
			{
				if (packet.getType() == PacketTurn.Type.BID)
				{
				}
				if (packet.getType() == PacketTurn.Type.CHANGE)
				{
				}
				if (packet.getType() == PacketTurn.Type.CALL)
				{
				}
				if (packet.getType() == PacketTurn.Type.ANNOUNCE)
				{
					availabeBidsView.removeAllViews();
					for (final Announcement a : Announcements.getAll())
					{
						Button announceButton = new Button(this);
						announceButton.setText(a.getClass().getSimpleName());
						announceButton.setOnClickListener(new OnClickListener()
						{
							public void onClick(View v)
							{
								conncection.sendPacket(new PacketAnnounce(a, myID));
							}
						});
						availabeBidsView.addView(announceButton);
					}
					
					okButton.setOnClickListener(new OnClickListener()
					{
						public void onClick(View v)
						{
							conncection.sendPacket(new PacketAnnounce(null, myID));
						}
					});
				}
				if (packet.getType() == PacketTurn.Type.PLAY_CARD)
				{
					if (center_space.getChildAt(0) != played_cards)
					{
						center_space.removeAllViews();
						center_space.addView(played_cards);
					}
				}
			}
			
			int pos = getPositionFromPlayerID(packet.getPlayer());
			
			myCardsView0.setBackgroundColor(Color.TRANSPARENT);
			for (TextView nameView : playerNameViews)
			{
				if (nameView == null) continue;
				nameView.setBackgroundColor(Color.TRANSPARENT);
			}
			
			if (pos == 0)
			{
				myCardsView0.setBackgroundColor(Color.MAGENTA);
			}
			else
			{
				for (TextView nameView : playerNameViews)
				{
					if (nameView == null) continue;
					nameView.setBackgroundColor(Color.TRANSPARENT);
				}
				playerNameViews[pos].setBackgroundColor(Color.MAGENTA);
			}
		}
		
		if (p instanceof PacketPlayCard)
		{
			PacketPlayCard packet = ((PacketPlayCard)p);
			
			if (center_space.getChildAt(0) == biddingView)
			{
				center_space.removeAllViews();
				center_space.addView(played_cards);
			}
			
			int pos = getPositionFromPlayerID(packet.getPlayer());
			
			if (packet.getPlayer() == myID)
			{
				myCards.removeCard(packet.getCard());
				View cardView = cardToViewMapping.remove(packet.getCard());
				myCardsView0.removeView(cardView);
				myCardsView1.removeView(cardView);
			}
			
			playedCardViews[pos].setImageBitmap(getBitmapForCard(packet.getCard()));
			
			cardsPlayed++;
			if (cardsPlayed % 4 == 0)
			{
				handler.postDelayed(new Runnable()
				{
					public void run()
					{
						for (ImageView cardView : playedCardViews)
						{
							cardView.setImageBitmap(getBitmapForCard(null));
						}
					}
				}, 1500);
			}
		}
		if (p instanceof PacketAnnouncementStatistics)
		{
			PacketAnnouncementStatistics packet = ((PacketAnnouncementStatistics)p);
		}
		if (p instanceof PacketPoints)
		{
			PacketPoints packet = ((PacketPoints)p);
		}
		if (p instanceof PacketSkartTarock)
		{
			PacketSkartTarock packet = ((PacketSkartTarock)p);
		}
		if (p instanceof PacketReadyForNewGame)
		{
		}
	}
	
	private void arrangeCards()
	{
		cardToViewMapping.clear();
		myCardsView0.removeAllViews();
		myCardsView1.removeAllViews();
		for (int i = 0; i < myCards.getCards().size(); i++)
		{
			final Card card = myCards.getCards().get(i);
			
			ImageView cardView = new ImageView(GameActivtiy.this);
			cardView.setAdjustViewBounds(true);
			cardView.setImageBitmap(getBitmapForCard(card));
			
			int padding = 10;
			cardView.setPadding(padding, padding, padding, padding);
			cardView.setLayoutParams(new LinearLayout.LayoutParams(cardWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
			final LinearLayout parentView = i < 6 ? myCardsView0 : myCardsView1;
			parentView.addView(cardView);
			cardToViewMapping.put(card, cardView);
			
			cardView.setOnClickListener(new OnClickListener()
			{
				private boolean selectedForSkart = false;
				public void onClick(View v)
				{
					if (skarting)
					{
						if (!selectedForSkart)
						{
							cardsToSkart.add(card);
							selectedForSkart = true;
							Animation a = new TranslateAnimation(0, 0, 0, -30);
							a.setDuration(300);
							a.setFillAfter(true);
							v.startAnimation(a);
						}
						else
						{
							cardsToSkart.remove(card);
							selectedForSkart = false;
							Animation a = new TranslateAnimation(0, 0, -30, 0);
							a.setDuration(300);
							a.setFillAfter(true);
							v.startAnimation(a);
						}
					}
					else
					{
						conncection.sendPacket(new PacketPlayCard(card, myID));
					}
				}
			});
		}
	}
	
	private Bitmap getBitmapForCard(Card card)
	{
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bmp = Bitmap.createBitmap(174, 289, conf);
		Canvas canvas = new Canvas(bmp);
		Paint paint = new Paint();
		paint.setTextSize(10);
		paint.setColor(Color.GREEN);
		if (card != null) canvas.drawText(card.toString(), 0, 0, paint);
		
		if (card == null) return null;
		
		int id = R.drawable.testcard;
		if (ResourceMappings.cardToImageResource.containsKey(card))
		{
			id = ResourceMappings.cardToImageResource.get(card);
		}
		bmp = BitmapFactory.decodeResource(getResources(), id);
		return bmp;
	}
	
	private int getPositionFromPlayerID(int id)
	{
		return (id - myID + 4) % 4;
	}

	public void connectionClosed()
	{
		finish();
	}
	
	protected void onDestroy()
	{
		super.onDestroy();
		if (conncection != null)
		{
			conncection.closeRequest();
		}
	}
}
