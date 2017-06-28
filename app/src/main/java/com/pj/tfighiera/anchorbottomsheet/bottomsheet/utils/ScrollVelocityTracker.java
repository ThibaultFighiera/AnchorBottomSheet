package com.pj.tfighiera.anchorbottomsheet.bottomsheet.utils;

/**
 * Classe utilitaire permetant de monitorer la vitesse du scroll horizontal
 *
 * created on 16/06/2017
 *
 * @author thibaultfighiera
 * @version 1.0
 */
public class ScrollVelocityTracker
{
	public static final int SECONDE = 1000;
	private long mPreviousScrollTime;
	private double mScrollVelocity;
	private int mMinimumVelocity;

	public ScrollVelocityTracker()
	{
		mPreviousScrollTime = 0;
		mScrollVelocity = 0;
	}

	public void recordScroll(int dy)
	{
		long now = System.currentTimeMillis();
		if (mPreviousScrollTime != 0)
		{
			long elapsed = now - mPreviousScrollTime;
			mScrollVelocity = (double) dy / elapsed * SECONDE; // pixels per sec
		}
		mPreviousScrollTime = now;
	}

	public void clear()
	{
		mPreviousScrollTime = 0;
		mScrollVelocity = 0;
	}

	public double getScrollVelocity()
	{
		return mScrollVelocity;
	}

	public void setMinimumVelocity(int minimumVelocity)
	{
		mMinimumVelocity = minimumVelocity;
	}

	public boolean isFlingingUp()
	{
		return mScrollVelocity > mMinimumVelocity;
	}

	public boolean isFlingingDown()
	{
		return mScrollVelocity < -mMinimumVelocity;
	}

	public boolean isFlinging()
	{
		return isFlingingDown() || isFlingingUp();
	}
}
