package com.tisza.tarock.gui;

import android.content.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.view.animation.Animation.*;
import android.widget.*;
import com.tisza.tarock.*;
import com.tisza.tarock.card.*;

import java.util.*;

public class PlayedCardView extends ImageView
{
	private int orientation;

	private Queue<Integer> imgResourcesQueue = new LinkedList<>();

	private boolean isAnimating = false;

	public PlayedCardView(Context context, int width, int height, int orientation)
	{
		super(context);
		this.orientation = orientation;
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		setLayoutParams(lp);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		if (!isAnimating)
		{
			startAnimation(createStaticPositionAnimation());
		}
	}
	
	public void addCard(Card c)
	{
		int res = getBitmapResForCard(c);

		if (imgResourcesQueue.isEmpty())
			setImageResource(res);

		imgResourcesQueue.add(res);
	}
	
	public void removeFirstCard()
	{
		if (imgResourcesQueue.isEmpty())
		{
			Log.e(GameActivtiy.LOG_TAG, "Tried to remove a card from an empty PlayedCardView");
			return;
		}

		imgResourcesQueue.remove();
		if (imgResourcesQueue.isEmpty())
		{
			setImageBitmap(null);
		}
		else
		{
			setImageResource(imgResourcesQueue.peek());
		}
	}
	
	private Animation createPositionAnimation()
	{
		int w = getWidth();
		int h = getHeight();

		Animation rotateAnim = new RotateAnimation(0, (orientation % 2) * 90, w / 2, h / 2);
		
		float tx = 0;
		float ty = 0;
		if (orientation == 0)
		{
			ty = 1;
		}
		else if (orientation == 1)
		{
			tx = 1;
		}
		else if (orientation == 2)
		{
			ty = -1;
		}
		else if (orientation == 3)
		{
			tx = -1;
		}
		tx *= h * GameActivtiy.PLAYED_CARD_DISTANCE;
		ty *= h * GameActivtiy.PLAYED_CARD_DISTANCE;
		Animation translateAnim = new TranslateAnimation(0, tx, 0, ty);

		AnimationSet animSet = new AnimationSet(true);
		animSet.addAnimation(rotateAnim);
		animSet.addAnimation(translateAnim);

		return animSet;
	}

	private Animation createStaticPositionAnimation()
	{
		Animation animation = createPositionAnimation();
		animation.setDuration(0);
		animation.setInterpolator(new EndInterpolator());
		animation.setFillAfter(true);
		return animation;
	}
	
	public void animatePlay()
	{
		startTakePlayAnimation(true, orientation);
	}
	
	public void animateTake(int dir)
	{
		startTakePlayAnimation(false, dir);
	}
	
	private void startTakePlayAnimation(boolean play, int dir)
	{
		Animation currentAnimation = getAnimation();
		if (currentAnimation != null)
		{
			currentAnimation.cancel();
		}
		
		isAnimating = true;

		View parent = (View)getParent();
		float tx = 0;
		float ty = 0;
		if (dir == 0)
		{
			ty = parent.getHeight() / 2;
		}
		else if (dir == 1)
		{
			tx = parent.getWidth() / 2;
		}
		else if (dir == 2)
		{
			ty = -parent.getHeight() / 2;
		}
		else if (dir == 3)
		{
			tx = -parent.getWidth() / 2;
		}

		Interpolator interpolator = play ? new LinearInterpolator() : new ReverseInterpolator();
		int duration = play ? GameActivtiy.PLAY_DURATION : GameActivtiy.TAKE_DURATION;

		Animation moveAnim = new TranslateAnimation(tx, 0, ty, 0);
		Animation positionAnim = createPositionAnimation();
		moveAnim.setInterpolator(interpolator);
		positionAnim.setInterpolator(interpolator);

		AnimationSet animSet = new AnimationSet(false);
		animSet.addAnimation(positionAnim);
		animSet.addAnimation(moveAnim);
		animSet.setDuration(duration);
		startAnimation(animSet);
		
		animSet.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
			}
			
			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
			
			@Override
			public void onAnimationEnd(Animation animation)
			{
				if (!play)
				{
					removeFirstCard();
				}
				startAnimation(createStaticPositionAnimation());
				isAnimating = false;
			}
		});
	}
	
	public boolean isAnimating()
	{
		return isAnimating;
	}
	
	public static int getBitmapResForCard(Card card)
	{
		if (ResourceMappings.cardToImageResource.containsKey(card))
		{
			return ResourceMappings.cardToImageResource.get(card);
		}
		else
		{
			Log.e(GameActivtiy.LOG_TAG, card + " has no image");
			return R.drawable.a1;
		}
	}
}
