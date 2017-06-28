package com.pj.tfighiera.anchorbottomsheet.bottomsheet.behaviors;

import com.pj.tfighiera.anchorbottomsheet.R;
import com.pj.tfighiera.anchorbottomsheet.bottomsheet.utils.ScrollVelocityTracker;
import com.pj.tfighiera.anchorbottomsheet.bottomsheet.callback.BottomSheetCallbackManager;
import com.pj.tfighiera.anchorbottomsheet.bottomsheet.state.StateManager;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import static com.pj.tfighiera.anchorbottomsheet.bottomsheet.state.StateManager.STATE_ANCHOR_POINT;
import static com.pj.tfighiera.anchorbottomsheet.bottomsheet.state.StateManager.STATE_COLLAPSED;
import static com.pj.tfighiera.anchorbottomsheet.bottomsheet.state.StateManager.STATE_DRAGGING;
import static com.pj.tfighiera.anchorbottomsheet.bottomsheet.state.StateManager.STATE_EXPANDED;
import static com.pj.tfighiera.anchorbottomsheet.bottomsheet.state.StateManager.STATE_HIDDEN;
import static com.pj.tfighiera.anchorbottomsheet.bottomsheet.state.StateManager.STATE_SETTLING;

public class AnchorBottomSheetBehavior extends AppBarLayout.ScrollingViewBehavior
{
	// layout settings
	private int mInitialY;
	private int mParentHeight;
	private int mPeekHeight;
	private int mMinOffset;
	private int mMaxOffset;
	private int mAnchorPoint;

	private WeakReference<View> mBottomSheetViewRef;
	// Helpers et Managers
	@NonNull
	private final BottomSheetCallbackManager mBottomSheetCallbackManager;
	private ViewDragHelper mViewDragHelper;
	@NonNull
	private final ViewDragHelper.Callback mViewDragCallback;
	@NonNull
	private ScrollVelocityTracker mScrollVelocityTracker;
	@NonNull
	private StateManager mStateManager;
	private boolean mHideable;
	private boolean mIgnoreEvents;
	// Nested scroll
	private boolean mNestedScrolled;
	private WeakReference<View> mNestedScrollingChildRef;
	private int mActivePointerId;

	public AnchorBottomSheetBehavior()
	{
		mScrollVelocityTracker = new ScrollVelocityTracker();
		mStateManager = new StateManager(STATE_ANCHOR_POINT);
		mBottomSheetCallbackManager = new BottomSheetCallbackManager();
		mViewDragCallback = new DragCallback();
	}

	public AnchorBottomSheetBehavior(@NonNull Context context, @Nullable AttributeSet attrs)
	{
		super(context, attrs);
		mScrollVelocityTracker = new ScrollVelocityTracker();
		mScrollVelocityTracker.setMinimumVelocity(ViewConfiguration.get(context)
		                                                           .getScaledMinimumFlingVelocity());
		TypedArray styledAttributesBottomSheet = context.obtainStyledAttributes(attrs, android.support.design.R.styleable.BottomSheetBehavior_Layout);
		setPeekHeight(styledAttributesBottomSheet.getDimensionPixelSize(android.support.design.R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight, 0));
		setHideable(styledAttributesBottomSheet.getBoolean(android.support.design.R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false));
		styledAttributesBottomSheet.recycle();
		TypedArray styledAttributesCustom = context.obtainStyledAttributes(attrs, R.styleable.CustomBottomSheetBehavior);
		if (attrs != null)
		{
			mAnchorPoint = (int) styledAttributesCustom.getDimension(R.styleable.CustomBottomSheetBehavior_anchorPoint, 0);
			mStateManager = new StateManager(STATE_ANCHOR_POINT);
		}
		else
		{
			mStateManager = new StateManager(STATE_COLLAPSED);
		}
		styledAttributesCustom.recycle();
		mBottomSheetCallbackManager = new BottomSheetCallbackManager();
		mViewDragCallback = new DragCallback();
	}

	@Override
	public Parcelable onSaveInstanceState(@NonNull CoordinatorLayout parent, @NonNull View child)
	{
		return mStateManager.createSavedState(super.onSaveInstanceState(parent, child));
	}

	@Override
	public void onRestoreInstanceState(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull Parcelable savedState)
	{
		mStateManager = StateManager.restoreInstance(savedState);
		super.onRestoreInstanceState(parent, child, StateManager.getSuperState(savedState));
	}

	// Management du layout //

	@Override
	public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull View child, int layoutDirection)
	{
		if (mStateManager.isStateStable())
		{
			if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child))
			{
				ViewCompat.setFitsSystemWindows(child, true);
			}
			parent.onLayoutChild(child, layoutDirection);
		}

		// Offset the bottom sheet
		mParentHeight = parent.getHeight();
		mMinOffset = Math.max(0, mParentHeight - child.getHeight());
		mMaxOffset = Math.max(mParentHeight - mPeekHeight, mMinOffset);
		switch (mStateManager.getState())
		{
			case STATE_ANCHOR_POINT:
				ViewCompat.offsetTopAndBottom(child, mAnchorPoint);
				break;
			case STATE_COLLAPSED:
				ViewCompat.offsetTopAndBottom(child, mMaxOffset);
				break;
			case STATE_DRAGGING:
				break;
			case STATE_EXPANDED:
				ViewCompat.offsetTopAndBottom(child, mMinOffset);
				break;
			case STATE_HIDDEN:
				ViewCompat.offsetTopAndBottom(child, mParentHeight);
				break;
			case STATE_SETTLING:
			default:
		}
		if (mViewDragHelper == null)
		{
			mViewDragHelper = ViewDragHelper.create(parent, mViewDragCallback);
		}
		mBottomSheetViewRef = new WeakReference<>(child);
		mNestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
		return true;
	}

	@Nullable
	private View findScrollingChild(@NonNull View view)
	{
		if (view instanceof NestedScrollingChild)
		{
			return view;
		}
		if (view instanceof ViewGroup)
		{
			ViewGroup group = (ViewGroup) view;
			for (int i = 0, count = group.getChildCount(); i < count; i++)
			{
				View scrollingChild = findScrollingChild(group.getChildAt(i));
				if (scrollingChild != null)
				{
					return scrollingChild;
				}
			}
		}
		return null;
	}

	private void setPeekHeight(int peekHeight)
	{
		mPeekHeight = Math.max(0, peekHeight);
		mMaxOffset = mParentHeight - peekHeight;
	}

	private void setHideable(boolean hideable)
	{
		mHideable = hideable;
	}

	// Evenements au touché de l'utilisateur //

	@Override
	public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull MotionEvent event)
	{
		if (!child.isShown())
		{
			return false;
		}
		int action = MotionEventCompat.getActionMasked(event);
		if (!mStateManager.isStateStable() && action == MotionEvent.ACTION_DOWN)
		{
			return true;
		}
		mViewDragHelper.processTouchEvent(event);
		if (action == MotionEvent.ACTION_DOWN)
		{
			reset();
		}

		// The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
		// to capture the bottom sheet in case it is not captured and the touch slop is passed.
		if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents)
		{
			if (Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop())
			{
				mViewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
			}
		}
		return !mIgnoreEvents;
	}

	@Override
	public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull MotionEvent event)
	{
		if (!child.isShown())
		{
			return false;
		}

		boolean isStopping = false;
		int x = (int) event.getX();
		int y = (int) event.getY();
		int action = MotionEventCompat.getActionMasked(event);
		switch (action)
		{
			case MotionEvent.ACTION_CANCEL:
				// evite d'envoyer l'évenement FLING
				mScrollVelocityTracker.clear();
				isStopping = onStopTouching();
				break;
			case MotionEvent.ACTION_UP:
				isStopping = onStopTouching();
				break;
			case MotionEvent.ACTION_DOWN:
				reset();
				mInitialY = y;
				if (mStateManager.getState() == STATE_ANCHOR_POINT)
				{
					mActivePointerId = event.getPointerId(event.getActionIndex());
				}
				else
				{
					View scroll = mNestedScrollingChildRef.get();
					if (scroll != null && parent.isPointInChildBounds(scroll, x, y))
					{
						mActivePointerId = event.getPointerId(event.getActionIndex());
					}
				}
				mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID && !parent.isPointInChildBounds(child, x, y);
				break;
			case MotionEvent.ACTION_MOVE:
			default:
		}
		if (isStopping)
		{
			return false;
		}
		if (!mIgnoreEvents && mViewDragHelper.shouldInterceptTouchEvent(event))
		{
			return true;
		}
		// We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
		// it is not the top most view of its parent. This is not necessary when the touch event is
		// happening over the scrolling content as nested scrolling logic handles that case.
		View scroll = mNestedScrollingChildRef.get();
		return action == MotionEvent.ACTION_MOVE
		       && scroll != null
		       && !mIgnoreEvents
		       && !mStateManager.isStateStable()
		       && !parent.isPointInChildBounds(scroll, x, y)
		       && Math.abs(mInitialY - y) > mViewDragHelper.getTouchSlop();
	}

	private boolean onStopTouching()
	{
		mActivePointerId = MotionEvent.INVALID_POINTER_ID;
		// Reset le flag ignore
		if (mIgnoreEvents)
		{
			mIgnoreEvents = false;
			return true;
		}
		return false;
	}

	@Override
	public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int nestedScrollAxes)
	{
		mNestedScrolled = false;
		return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
	}

	@Override
	public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dx, int dy, int[] consumed)
	{
		@StateManager.State int state = STATE_EXPANDED;
		View scrollingChild = mNestedScrollingChildRef.get();
		if (target != scrollingChild)
		{
			return;
		}
		mScrollVelocityTracker.recordScroll(dy);
		int currentTop = child.getTop();
		int newTop = currentTop - dy;
		if (mStateManager.shouldStopAtAnchor(newTop, mAnchorPoint))
		{
			consumed[1] = dy;
			ViewCompat.offsetTopAndBottom(child, mAnchorPoint - currentTop);
			dispatchOnSlide(child.getTop());
			mNestedScrolled = true;
			return;
		}
		if (dy > 0)
		{
			if (newTop < mMinOffset)
			{
				consumed[1] = currentTop - mMinOffset;
				ViewCompat.offsetTopAndBottom(child, -consumed[1]);
				state = STATE_EXPANDED;
			}
			else
			{
				consumed[1] = dy;
				ViewCompat.offsetTopAndBottom(child, -dy);
				state = STATE_DRAGGING;
			}
		}
		else if (dy < 0 && !ViewCompat.canScrollVertically(target, -1))
		{
			if (newTop <= mMaxOffset || mHideable)
			{
				consumed[1] = dy;
				ViewCompat.offsetTopAndBottom(child, -dy);
				state = STATE_DRAGGING;
			}
			else
			{
				consumed[1] = currentTop - mMaxOffset;
				ViewCompat.offsetTopAndBottom(child, -consumed[1]);
				state = STATE_COLLAPSED;
			}
		}
		setStateInternal(state);
		dispatchOnSlide(child.getTop());
		mNestedScrolled = true;
	}

	@Override
	public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target)
	{
		if (target != mNestedScrollingChildRef.get() || !mNestedScrolled)
		{
			return;
		}
		int top;
		if (mScrollVelocityTracker.isFlinging() && !mStateManager.isStateStable())
		{
			top = mStateManager.setNextStableState(mMinOffset, mAnchorPoint, mMaxOffset, mScrollVelocityTracker.isFlingingUp());
		}
		else
		{
			top = mStateManager.settleNearestNextStableState(mMinOffset, mAnchorPoint, mMaxOffset, child.getTop());

		}
		if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top))
		{
			setStateInternal(STATE_SETTLING);
			ViewCompat.postOnAnimation(child, new SettleRunnable(child, mStateManager.getLastStableState()));
		}
		else
		{
			setStateInternal(mStateManager.getLastStableState());
		}
		mNestedScrolled = false;
	}

	@Override
	public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, float velocityX, float velocityY)
	{
		return mStateManager.getLastStableState() != STATE_EXPANDED;
	}

	// Changement d'état //

	/**
	 * Permet de changer l'état d'ouverture de la bottom sheet
	 *
	 * @param state le nouvel état attendu
	 */
	public final void setState(@StateManager.State int state)
	{
		if (state == mStateManager.getState())
		{
			return;
		}
		if (mBottomSheetViewRef == null)
		{
			if (mStateManager.validState(state, mHideable))
			{
				mStateManager = new StateManager(state);
			}
			return;
		}
		View child = mBottomSheetViewRef.get();
		if (child == null)
		{
			return;
		}
		int top;
		switch (state)
		{
			case STATE_ANCHOR_POINT:
				top = mAnchorPoint;
				break;
			case STATE_COLLAPSED:
				top = mMaxOffset;
				break;
			case STATE_EXPANDED:
				top = mMinOffset;
				break;
			case STATE_HIDDEN:
				top = mHideable
				      ? mParentHeight
				      : 0;
				break;
			case STATE_DRAGGING:
			case STATE_SETTLING:
			default:
				throw new IllegalArgumentException("Illegal state argument: " + state);
		}
		setStateInternal(STATE_SETTLING);
		if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top))
		{
			ViewCompat.postOnAnimation(child, new SettleRunnable(child, state));
		}
	}

	private void setStateInternal(@StateManager.State int state)
	{
		if (!mStateManager.setState(state))
		{
			notifyStateChangedToListeners(mBottomSheetViewRef.get(), state);
		}
	}

	// Callback //

	public void addBehaviorCallback(@NonNull BottomSheetCallbackManager.BottomSheetCallback callback)
	{
		mBottomSheetCallbackManager.addCallBack(callback);
	}

	private void notifyStateChangedToListeners(@Nullable View bottomSheet, @StateManager.State int newState)
	{
		mBottomSheetCallbackManager.notifyStateChanged(bottomSheet, newState);
	}

	private void notifyOnSlideToListeners(@NonNull View bottomSheet, double slideOffset)
	{
		mBottomSheetCallbackManager.notifyOnSlide(bottomSheet, slideOffset);
	}

	private void reset()
	{
		mActivePointerId = ViewDragHelper.INVALID_POINTER;
	}

	private void dispatchOnSlide(int top)
	{
		double divider = top > mMaxOffset
		                 ? mPeekHeight
		                 : mMaxOffset - mMinOffset;
		notifyOnSlideToListeners(mBottomSheetViewRef.get(), (double) (mMaxOffset - top) / divider);
	}

	private class DragCallback extends ViewDragHelper.Callback
	{
		private static final double HIDE_THRESHOLD = 0.5f;
		private static final double HIDE_FRICTION = 0.1f;

		@Override
		public boolean tryCaptureView(@NonNull View child, int pointerId)
		{
			if (mStateManager.isStateStable())
			{
				return false;
			}
			if (mActivePointerId != MotionEvent.INVALID_POINTER_ID)
			{
				return false;
			}
			if (mStateManager.getState() == STATE_EXPANDED && mActivePointerId == pointerId)
			{
				View scroll = mNestedScrollingChildRef.get();
				if (scroll != null && ViewCompat.canScrollVertically(scroll, -1))
				{
					// Permet au contenu de scroll up
					return false;
				}
			}
			return mBottomSheetViewRef != null && mBottomSheetViewRef.get() == child;
		}

		@Override
		public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy)
		{
			dispatchOnSlide(top);
		}

		@Override
		public void onViewDragStateChanged(int state)
		{
			if (state == ViewDragHelper.STATE_DRAGGING)
			{
				setStateInternal(STATE_DRAGGING);
			}
		}

		@Override
		public void onViewReleased(@NonNull View releasedChild, float xvel, float yVelocity)
		{
			int top;
			@StateManager.State int targetState;
			// tire vers le haut
			if (yVelocity < 0)
			{
				top = mMinOffset;
				targetState = STATE_EXPANDED;
			}
			else if (mHideable && shouldHide(releasedChild, yVelocity))
			{
				top = mParentHeight;
				targetState = STATE_HIDDEN;
			}
			else if (yVelocity > 0)
			{
				top = mMaxOffset;
				targetState = STATE_COLLAPSED;
			}
			// si la bottom sheet est immobile
			else
			{
				int currentTop = releasedChild.getTop();
				if (Math.abs(currentTop - mMinOffset) < Math.abs(currentTop - mMaxOffset))
				{
					top = mMinOffset;
					targetState = STATE_EXPANDED;
				}
				else
				{
					top = mMaxOffset;
					targetState = STATE_COLLAPSED;
				}
			}
			if (mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top))
			{
				setStateInternal(STATE_SETTLING);
				ViewCompat.postOnAnimation(releasedChild, new SettleRunnable(releasedChild, targetState));
			}
			else
			{
				setStateInternal(targetState);
			}
		}

		@Override
		public int clampViewPositionVertical(@NonNull View child, int top, int dy)
		{
			int maxPosition = mHideable
			                  ? mParentHeight
			                  : mMaxOffset;
			int constrainValue;
			if (top > mMinOffset)
			{
				constrainValue = top > maxPosition
				                 ? maxPosition
				                 : top;
			}
			else
			{
				constrainValue = mMinOffset;
			}
			return constrainValue;
		}

		@Override
		public int clampViewPositionHorizontal(@NonNull View child, int left, int dx)
		{
			return child.getLeft();
		}

		@Override
		public int getViewVerticalDragRange(@NonNull View child)
		{
			return mHideable
			       ? mParentHeight - mMinOffset
			       : mMaxOffset - mMinOffset;
		}

		private boolean shouldHide(@NonNull View child, float yVelocity)
		{
			if (child.getTop() < mMaxOffset)
			{
				// hide != collapse.
				return false;
			}
			final double newTop = child.getTop() + yVelocity * HIDE_FRICTION;
			return Math.abs(newTop - mMaxOffset) / (double) mPeekHeight > HIDE_THRESHOLD;
		}
	}

	private class SettleRunnable implements Runnable
	{
		private final View mView;
		@StateManager.State
		private final int mTargetState;

		SettleRunnable(@NonNull View view, @StateManager.State int targetState)
		{
			mView = view;
			mTargetState = targetState;
		}

		@Override
		public void run()
		{
			if (mViewDragHelper != null && mViewDragHelper.continueSettling(true))
			{
				ViewCompat.postOnAnimation(mView, this);
			}
			setStateInternal(mTargetState);
			mStateManager.setLastStableState(mTargetState);
		}
	}

	/**
	 * Permet de récuperer le {@link AnchorBottomSheetBehavior} associé à une vue {@link View}
	 *
	 * @param view la {@link View} avec {@link AnchorBottomSheetBehavior}.
	 *
	 * @return le {@link AnchorBottomSheetBehavior} associé à la {@code View}.
	 */
	@SuppressWarnings("unchecked")
	@NonNull
	public static AnchorBottomSheetBehavior from(@NonNull View view)
	{
		ViewGroup.LayoutParams params = view.getLayoutParams();
		if (!(params instanceof CoordinatorLayout.LayoutParams))
		{
			throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
		}
		CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params).getBehavior();
		if (!(behavior instanceof AnchorBottomSheetBehavior))
		{
			throw new IllegalArgumentException("The view is not associated with AnchorBottomSheetBehavior");
		}
		return (AnchorBottomSheetBehavior) behavior;
	}
}
