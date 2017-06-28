package com.pj.tfighiera.anchorbottomsheet.bottomsheet.callback;

import com.pj.tfighiera.anchorbottomsheet.bottomsheet.state.StateManager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Callback sur l'etat de la BottomSheet
 *
 * created on 27/06/2017
 *
 * @author tfi
 * @version 1.0
 */
public class BottomSheetCallbackManager
{
	@NonNull
	private List<BottomSheetCallback> mCallback;

	public interface BottomSheetCallback
	{
		void onStateChanged(@NonNull View bottomSheet, @StateManager.State int newState);

		void onSlide(@NonNull View bottomSheet, double slideOffset);
	}

	public BottomSheetCallbackManager()
	{
		mCallback = new ArrayList<>();
	}

	public void addCallBack(BottomSheetCallback callback)
	{
		mCallback.add(callback);
	}

	public void notifyOnSlide(@Nullable View bottomSheet, double slideOffset)
	{
		if (bottomSheet == null)
		{
			return;
		}
		for (BottomSheetCallback bottomSheetCallback : mCallback)
		{
			bottomSheetCallback.onSlide(bottomSheet, slideOffset);
		}
	}

	public void notifyStateChanged(@Nullable View bottomSheet, @StateManager.State int newState)
	{
		if (bottomSheet == null)
		{
			return;
		}
		for (BottomSheetCallback bottomSheetCallback : mCallback)
		{
			bottomSheetCallback.onStateChanged(bottomSheet, newState);
		}
	}
}