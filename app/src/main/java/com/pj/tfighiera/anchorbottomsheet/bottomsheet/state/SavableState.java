package com.pj.tfighiera.anchorbottomsheet.bottomsheet.state;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;

import static com.pj.tfighiera.anchorbottomsheet.bottomsheet.state.StateManager.STATE_COLLAPSED;
import static com.pj.tfighiera.anchorbottomsheet.bottomsheet.state.StateManager.STATE_DRAGGING;
import static com.pj.tfighiera.anchorbottomsheet.bottomsheet.state.StateManager.STATE_SETTLING;

/**
 * Permet d'enregistrer l'état la bottomsheet
 *
 * created on 21/06/2017
 *
 * @author tfi
 * @version 1.0
 */
class SavableState extends View.BaseSavedState
{
	@StateManager.State
	private final int mState;

	SavableState(Parcel source)
	{
		super(source);
		// noinspection ResourceType
		mState = source.readInt();
	}

	SavableState(@NonNull StateManager stateManager, @NonNull Parcelable superState)
	{
		super(superState);
		mState = stateManager.mState;
	}

	@Override
	public void writeToParcel(@NonNull Parcel out, int flags)
	{
		super.writeToParcel(out, flags);
		out.writeInt(mState == STATE_DRAGGING || mState == STATE_SETTLING
		             ? STATE_COLLAPSED
		             : mState);
	}

	public static final Creator<SavableState> CREATOR = new Creator<SavableState>()
	{
		@Override
		public SavableState createFromParcel(@NonNull Parcel source)
		{
			return new SavableState(source);
		}

		@Override
		@NonNull
		public SavableState[] newArray(int size)
		{
			return new SavableState[size];
		}
	};

	@NonNull
	StateManager createStateManager()
	{
		return new StateManager(mState);
	}
}
