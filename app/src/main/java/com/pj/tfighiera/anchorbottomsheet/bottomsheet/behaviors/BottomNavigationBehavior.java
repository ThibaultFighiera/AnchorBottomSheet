package com.pj.tfighiera.anchorbottomsheet.bottomsheet.behaviors;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Behavior de Bottomsheet à trois états
 *
 * created on 27/06/2017
 *
 * @author tfi
 * @version 1.0
 */
public class BottomNavigationBehavior<V extends View> extends CoordinatorLayout.Behavior<V>
{

	public BottomNavigationBehavior()
	{
		super();
	}

	public BottomNavigationBehavior(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency)
	{
		return dependency instanceof NestedScrollView;
	}

	@Override
	public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes)
	{
		return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
	}

	@Override
	public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed)
	{
		if (dy > 0)
		{
			hideBottomNavigationView(child);
		}
		else if (dy < 0)
		{
			showBottomNavigationView(child);
		}
	}

	private void hideBottomNavigationView(V view)
	{
		view.animate()
		    .translationY(view.getHeight());
	}

	private void showBottomNavigationView(V view)
	{
		view.animate()
		    .translationY(0);
	}
}