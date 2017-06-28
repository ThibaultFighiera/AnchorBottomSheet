package com.pj.tfighiera.anchorbottomsheet.bottomsheet.state;

import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Permet de gerer les états d'une bottom sheet
 *
 * created on 16/06/2017
 *
 * @author tfi
 * @version 1.0
 */
public class StateManager
{
	@IntDef({STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_ANCHOR_POINT, STATE_SETTLING, STATE_HIDDEN})
	@Retention(RetentionPolicy.SOURCE)
	public @interface State {}

	public static final int STATE_DRAGGING = 1;
	public static final int STATE_SETTLING = 2;
	public static final int STATE_ANCHOR_POINT = 3;
	public static final int STATE_EXPANDED = 4;
	public static final int STATE_COLLAPSED = 5;
	public static final int STATE_HIDDEN = 6;
	private static final double PARALLAX_FACTOR = 1.25;
	private static final double ANCHOR_TRIGGER_RATIO = 0.5;
	@State
	private int mLastStableState;
	@State
	int mState;

	public StateManager(@State int state)
	{
		mLastStableState = mState;
		mState = state;
	}

	@NonNull
	public Parcelable createSavedState(@NonNull Parcelable source)
	{
		return new SavableState(this, source);
	}

	@NonNull
	public static StateManager restoreInstance(@Nullable Parcelable savedState)
	{
		if (savedState instanceof SavableState)
		{
			return ((SavableState) savedState).createStateManager();
		}
		return new StateManager(STATE_ANCHOR_POINT);
	}

	@State
	public int getLastStableState()
	{
		return mLastStableState;
	}

	public void setLastStableState(int lastStableState)
	{
		mLastStableState = lastStableState;
	}

	public boolean isStateStable()
	{
		return mState != STATE_DRAGGING && mState != STATE_SETTLING;
	}

	public boolean validState(int state, boolean hideable)
	{
		return state == STATE_COLLAPSED || state == STATE_EXPANDED || state == STATE_ANCHOR_POINT || (hideable && state == STATE_HIDDEN);
	}

	@NonNull
	public static Parcelable getSuperState(@NonNull Parcelable savedState)
	{
		return ((SavableState) savedState).getSuperState();
	}

	/**
	 * Force stop at the anchor - do not go from collapsed to expanded in one scroll
	 */
	public boolean shouldStopAtAnchor(int newTop, int anchorPoint)
	{
		return (mLastStableState == STATE_COLLAPSED && newTop < anchorPoint) || (mLastStableState == STATE_EXPANDED
		                                                                         && newTop > anchorPoint);
	}

	@State
	public final int getState()
	{
		return mState;
	}

	public int setNextStableState(int minOffset, int anchorPoint, int maxOffset, boolean isFlingingUp)
	{
		int top;
		if (isFlingingUp)
		{
			if (mLastStableState == STATE_COLLAPSED)
			{
				top = anchorPoint;
				mLastStableState = STATE_ANCHOR_POINT;
			}
			else
			{
				top = minOffset;
				mLastStableState = STATE_EXPANDED;
			}
		}
		// va vers le bas
		else
		{
			if (mLastStableState == STATE_EXPANDED)
			{
				top = anchorPoint;
				mLastStableState = STATE_ANCHOR_POINT;
			}
			else
			{
				top = maxOffset;
				mLastStableState = STATE_COLLAPSED;
			}
		}
		return top;
	}

	/**
	 * Set le nouvel état
	 *
	 * @param state le nouvel état
	 *
	 * @return true si le nouvel état est identique à l'ancien
	 */
	public boolean setState(@State int state)
	{
		boolean isSame = mState != state;
		mState = state;
		return isSame;
	}

	public int settleNearestNextStableState(int minOffset, int anchorPoint, int maxOffset, int currentTop)
	{
		int top;
		// Se ferme
		if (currentTop > anchorPoint * PARALLAX_FACTOR)
		{
			top = maxOffset;
			mLastStableState = STATE_COLLAPSED;
		}
		// s'ouvre
		else if (currentTop < anchorPoint * ANCHOR_TRIGGER_RATIO)
		{
			top = minOffset;
			mLastStableState = STATE_EXPANDED;
		}
		// retourne à l'ancre
		else
		{
			top = anchorPoint;
			mLastStableState = STATE_ANCHOR_POINT;
		}
		return top;
	}
}


