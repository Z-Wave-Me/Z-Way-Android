/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 22.06.14 11:00.
 * Copyright (c) 2014 Z-Wave.Me
 *
 * All rights reserved
 * info@z-wave.me
 * Z-Way for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Z-Way for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Z-Way for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.z_wave.android.ui.views;


import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

public class SwipeGridViewTouchHandler implements View.OnTouchListener {

    private static final int DISPLACE_CHOICE = 80;

    private boolean swipeOpenOnLongPress = true;
    private boolean swipeClosesAllItemsWhenListMoves = true;

    private int swipeFrontView = 0;
    private int swipeBackView = 0;

    private Rect rect = new Rect();

    private int slop;
    private int minFlingVelocity;
    private int maxFlingVelocity;
    private long configShortAnimationTime;
    private long animationTime;

    private float offset = 0;
    private int swipeDrawableChecked = 0;
    private int swipeDrawableUnchecked = 0;

    private SwipeGridView swipeListView;
    private int viewWidth = 1;
    private List<PendingDismissData> pendingDismisses = new ArrayList<PendingDismissData>();
    private int dismissAnimationRefCount = 0;

    private float downX;
    private boolean swiping;
    private boolean swipingRight;
    private VelocityTracker velocityTracker;
    private int downPosition;
    private View parentView;
    private View frontView;
    private View backView;
    private boolean paused;

    private int swipeCurrentAction = SwipeGridView.SWIPE_ACTION_NONE;

    private int swipeActionLeft = SwipeGridView.SWIPE_ACTION_REVEAL;
    private int swipeActionRight = SwipeGridView.SWIPE_ACTION_REVEAL;

    private List<Boolean> opened = new ArrayList<Boolean>();
    private List<Boolean> openedRight = new ArrayList<Boolean>();
    private List<Boolean> checked = new ArrayList<Boolean>();
    private int oldSwipeActionRight;
    private int oldSwipeActionLeft;

    public SwipeGridViewTouchHandler(SwipeGridView swipeListView, int swipeFrontView, int swipeBackView) {
        this.swipeFrontView = swipeFrontView;
        this.swipeBackView = swipeBackView;
        ViewConfiguration vc = ViewConfiguration.get(swipeListView.getContext());
        slop = vc.getScaledTouchSlop();
        minFlingVelocity = vc.getScaledMinimumFlingVelocity();
        maxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        configShortAnimationTime = swipeListView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
        animationTime = configShortAnimationTime;
        this.swipeListView = swipeListView;
    }

    private void setParentView(View parentView) {
        this.parentView = parentView;
    }

    private void setFrontView(View frontView) {
        this.frontView = frontView;
        if (swipeOpenOnLongPress) {
            frontView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    openAnimate(downPosition);
                    return false;
                }
            });
        }
    }

    private void setBackView(View backView) {
        this.backView = backView;
    }

    public void setAnimationTime(long animationTime) {
        if (animationTime > 0) {
            this.animationTime = animationTime;
        } else {
            this.animationTime = configShortAnimationTime;
        }
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public void setSwipeClosesAllItemsWhenListMoves(boolean swipeClosesAllItemsWhenListMoves) {
        this.swipeClosesAllItemsWhenListMoves = swipeClosesAllItemsWhenListMoves;
    }

    public void setSwipeOpenOnLongPress(boolean swipeOpenOnLongPress) {
        this.swipeOpenOnLongPress = swipeOpenOnLongPress;
    }

    public void setSwipeActionRight(int swipeActionRight) {
        this.swipeActionRight = swipeActionRight;
    }

    protected void setSwipeDrawableChecked(int swipeDrawableChecked) {
        this.swipeDrawableChecked = swipeDrawableChecked;
    }

    protected void setSwipeDrawableUnchecked(int swipeDrawableUnchecked) {
        this.swipeDrawableUnchecked = swipeDrawableUnchecked;
    }

    public void resetItems() {
        if (swipeListView.getAdapter() != null) {
            int count = swipeListView.getAdapter().getCount();
            for (int i = opened.size(); i <= count; i++) {
                opened.add(false);
                openedRight.add(false);
                checked.add(false);
            }
        }
    }

    protected void openAnimate(int position) {
        openAnimate(swipeListView.getChildAt(position - swipeListView.getFirstVisiblePosition()).findViewById(swipeFrontView), position);
    }

    protected void closeAnimate(int position) {
        closeAnimate(swipeListView.getChildAt(position - swipeListView.getFirstVisiblePosition()).findViewById(swipeFrontView), position);
    }

    private void swapChoiceState(int position) {
        int lastCount = getCountSelected();
        boolean lastChecked = checked.get(position);
        checked.set(position, !lastChecked);
        int count = lastChecked ? lastCount - 1 : lastCount + 1;
        if (lastCount == 0 && count == 1) {
            closeOpenedItems();
            setActionsTo(SwipeGridView.SWIPE_ACTION_CHOICE);
        }
        if (lastCount == 1 && count == 0) {
            returnOldActions();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            swipeListView.setItemChecked(position, !lastChecked);
        }
        reloadChoiceStateInView(frontView, position);
    }

    protected int dismiss(int position) {
        int start = swipeListView.getFirstVisiblePosition();
        int end = swipeListView.getLastVisiblePosition();
        View view = swipeListView.getChildAt(position - start);
        ++dismissAnimationRefCount;
        if (position >= start && position <= end) {
            performDismiss(view, position, false);
            return view.getHeight();
        } else {
            pendingDismisses.add(new PendingDismissData(position, null));
            return 0;
        }
    }

    protected void reloadChoiceStateInView(View frontView, int position) {
        if (isChecked(position)) {
            if (swipeDrawableChecked > 0) frontView.setBackgroundResource(swipeDrawableChecked);
        } else {
            if (swipeDrawableUnchecked > 0) frontView.setBackgroundResource(swipeDrawableUnchecked);
        }
    }

    protected void reloadSwipeStateInView(View frontView) {
        if(this.swipeClosesAllItemsWhenListMoves){
            frontView.setTranslationX(0f);
        }
    }

    protected boolean isChecked(int position) {
        return position < checked.size() && checked.get(position);
    }

    protected int getCountSelected() {
        int count = 0;
        for (int i = 0; i < checked.size(); i++) {
            if (checked.get(i)) {
                count++;
            }
        }
        return count;
    }

    private void openAnimate(View view, int position) {
        if (!opened.get(position)) {
            generateRevealAnimate(view, true, false, position);
        }
    }

    private void closeAnimate(View view, int position) {
        if (opened.get(position)) {
            generateRevealAnimate(view, true, false, position);
        }
    }

    private void generateAnimate(final View view, final boolean swap, final boolean swapRight, final int position) {
        if (swipeCurrentAction == SwipeGridView.SWIPE_ACTION_REVEAL) {
            generateRevealAnimate(view, swap, swapRight, position);
        }
        if (swipeCurrentAction == SwipeGridView.SWIPE_ACTION_DISMISS) {
            generateDismissAnimate(parentView, swap, swapRight, position);
        }
        if (swipeCurrentAction == SwipeGridView.SWIPE_ACTION_CHOICE) {
            generateChoiceAnimate(view, position);
        }
    }

    private void generateChoiceAnimate(final View view, final int position) {
        animate(view)
                .translationX(0)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        swipeListView.resetScrolling();
                        resetCell();
                    }
                });
    }

    private void generateDismissAnimate(final View view, final boolean swap, final boolean swapRight, final int position) {
        int moveTo = 0;
        if (opened.get(position)) {
            if (!swap) {
                moveTo = viewWidth;
            }
        } else {
            if (swap) {
                moveTo = viewWidth;
            }
        }

        int alpha = 1;
        if (swap) {
            ++dismissAnimationRefCount;
            alpha = 0;
        }

        animate(view)
                .translationX(moveTo)
                .alpha(alpha)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (swap) {
                            closeOpenedItems();
                            performDismiss(view, position, true);
                        }
                        resetCell();
                    }
                });

    }

    private void generateRevealAnimate(final View view, final boolean swap, final boolean swapRight, final int position) {
        int moveTo = 0;
        if (opened.get(position)) {
            if (!swap) {
                moveTo = (int) (-offset);
            }
        } else {
            if (swap) {
                moveTo = (int) (-offset);
            }
        }

        animate(view)
                .translationX(moveTo)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        swipeListView.resetScrolling();
                        if (swap) {
                            boolean aux = !opened.get(position);
                            opened.set(position, aux);
                            if (aux) {
                                openedRight.set(position, swapRight);
                            }
                        }
                        resetCell();
                    }
                });
    }

    private void resetCell() {
        if (downPosition != ListView.INVALID_POSITION) {
            if (swipeCurrentAction == SwipeGridView.SWIPE_ACTION_CHOICE) {
                backView.setVisibility(View.VISIBLE);
            }
            frontView.setClickable(opened.get(downPosition));
            frontView.setLongClickable(opened.get(downPosition));
            frontView = null;
            backView = null;
            downPosition = ListView.INVALID_POSITION;
        }
    }

    public void setEnabled(boolean enabled) {
        paused = !enabled;
    }

    public AbsListView.OnScrollListener makeScrollListener() {
        return new AbsListView.OnScrollListener() {

            private boolean isFirstItem = false;
            private boolean isLastItem = false;

            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                if (swipeClosesAllItemsWhenListMoves && scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    closeOpenedItems();
                }
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {

                    setEnabled(false);
                }
                if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_FLING && scrollState != SCROLL_STATE_TOUCH_SCROLL) {
                    downPosition = ListView.INVALID_POSITION;
                    swipeListView.resetScrolling();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            setEnabled(true);
                        }
                    }, 500);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (isFirstItem) {
                    boolean onSecondItemList = firstVisibleItem == 1;
                    if (onSecondItemList) {
                        isFirstItem = false;
                    }
                } else {
                    boolean onFirstItemList = firstVisibleItem == 0;
                    if (onFirstItemList) {
                        isFirstItem = true;
                    }
                }
                if (isLastItem) {
                    boolean onBeforeLastItemList = firstVisibleItem + visibleItemCount == totalItemCount - 1;
                    if (onBeforeLastItemList) {
                        isLastItem = false;
                    }
                } else {
                    boolean onLastItemList = firstVisibleItem + visibleItemCount >= totalItemCount;
                    if (onLastItemList) {
                        isLastItem = true;
                    }
                }
            }
        };
    }

    void closeOpenedItems() {
        if (opened != null) {
            int start = swipeListView.getFirstVisiblePosition();
            int end = swipeListView.getLastVisiblePosition();
            for (int i = start; i <= end; i++) {
                if (opened.get(i)) {
                    closeAnimate(swipeListView.getChildAt(i - start).findViewById(swipeFrontView), i);
                }
            }
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (viewWidth < 2) {
            viewWidth = swipeListView.getWidth();
        }

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (paused && downPosition != ListView.INVALID_POSITION) {
                    return false;
                }
                swipeCurrentAction = SwipeGridView.SWIPE_ACTION_NONE;

                int childCount = swipeListView.getChildCount();
                int[] listViewCoords = new int[2];
                swipeListView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = swipeListView.getChildAt(i);
                    child.getHitRect(rect);

                    int childPosition = swipeListView.getPositionForView(child);

                    boolean allowSwipe = swipeListView.getAdapter().isEnabled(childPosition) && swipeListView.getAdapter().getItemViewType(childPosition) >= 0;

                    if (allowSwipe && rect.contains(x, y)) {
                        setParentView(child);
                        setFrontView(child.findViewById(swipeFrontView));

                        downX = motionEvent.getRawX();
                        downPosition = childPosition;

                        frontView.setClickable(!opened.get(downPosition));
                        frontView.setLongClickable(!opened.get(downPosition));

                        velocityTracker = VelocityTracker.obtain();
                        velocityTracker.addMovement(motionEvent);
                        if (swipeBackView > 0) {
                            setBackView(child.findViewById(swipeBackView));
                        }
                        break;
                    }
                }
                view.onTouchEvent(motionEvent);
                return true;
            }

            case MotionEvent.ACTION_UP: {
                if (velocityTracker == null || !swiping || downPosition == ListView.INVALID_POSITION) {
                    break;
                }

                float deltaX = motionEvent.getRawX() - downX;
                velocityTracker.addMovement(motionEvent);
                velocityTracker.computeCurrentVelocity(1000);
                float velocityX = Math.abs(velocityTracker.getXVelocity());
                if (!opened.get(downPosition)) {
                    if (velocityTracker.getXVelocity() > 0) {
                        velocityX = 0;
                    }
                }
                float velocityY = Math.abs(velocityTracker.getYVelocity());
                boolean swap = false;
                boolean swapRight = false;
                if (minFlingVelocity <= velocityX && velocityX <= maxFlingVelocity && velocityY * 2 < velocityX) {
                    swapRight = velocityTracker.getXVelocity() > 0;
                    if (swapRight != swipingRight && swipeActionLeft != swipeActionRight) {
                        swap = false;
                    } else if (opened.get(downPosition) && openedRight.get(downPosition) && swapRight) {
                        swap = false;
                    } else if (opened.get(downPosition) && !openedRight.get(downPosition) && !swapRight) {
                        swap = false;
                    } else {
                        swap = true;
                    }
                } else if (Math.abs(deltaX) > viewWidth / 2) {
                    swap = true;
                    swapRight = deltaX > 0;
                }
                generateAnimate(frontView, swap, swapRight, downPosition);
                if (swipeCurrentAction == SwipeGridView.SWIPE_ACTION_CHOICE) {
                    swapChoiceState(downPosition);
                }

                velocityTracker.recycle();
                velocityTracker = null;
                downX = 0;
                swiping = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (velocityTracker == null || paused || downPosition == ListView.INVALID_POSITION) {
                    break;
                }

                velocityTracker.addMovement(motionEvent);
                velocityTracker.computeCurrentVelocity(1000);
                float velocityX = Math.abs(velocityTracker.getXVelocity());
                float velocityY = Math.abs(velocityTracker.getYVelocity());

                float deltaX = motionEvent.getRawX() - downX;
                float deltaMode = Math.abs(deltaX);


                    if (opened.get(downPosition)) {
                        if (deltaX < 0) {
                            deltaMode = 0;
                        }
                    } else if (deltaX > 0) {
                            deltaMode = 0;
                    }
                if (deltaMode > slop && swipeCurrentAction == SwipeGridView.SWIPE_ACTION_NONE && velocityY < velocityX) {
                    swiping = true;
                    swipingRight = (deltaX > 0);
                    if (opened.get(downPosition)) {
                        swipeCurrentAction = SwipeGridView.SWIPE_ACTION_REVEAL;
                    } else {
                        if (swipingRight && swipeActionRight == SwipeGridView.SWIPE_ACTION_DISMISS) {
                            swipeCurrentAction = SwipeGridView.SWIPE_ACTION_DISMISS;
                        } else if (!swipingRight && swipeActionLeft == SwipeGridView.SWIPE_ACTION_DISMISS) {
                            swipeCurrentAction = SwipeGridView.SWIPE_ACTION_DISMISS;
                        } else if (swipingRight && swipeActionRight == SwipeGridView.SWIPE_ACTION_CHOICE) {
                            swipeCurrentAction = SwipeGridView.SWIPE_ACTION_CHOICE;
                        } else if (!swipingRight && swipeActionLeft == SwipeGridView.SWIPE_ACTION_CHOICE) {
                            swipeCurrentAction = SwipeGridView.SWIPE_ACTION_CHOICE;
                        } else {
                            swipeCurrentAction = SwipeGridView.SWIPE_ACTION_REVEAL;
                        }
                    }
                    swipeListView.requestDisallowInterceptTouchEvent(true);
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    swipeListView.onTouchEvent(cancelEvent);
                    if (swipeCurrentAction == SwipeGridView.SWIPE_ACTION_CHOICE) {
                        backView.setVisibility(View.GONE);
                    }
                }

                if (swiping && downPosition != ListView.INVALID_POSITION) {
                    if (opened.get(downPosition)) {
                        deltaX += -offset;
                    }
                    move(deltaX);
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private void setActionsTo(int action) {
        oldSwipeActionRight = swipeActionRight;
        oldSwipeActionLeft = swipeActionLeft;
        swipeActionRight = action;
        swipeActionLeft = action;
    }

    protected void returnOldActions() {
        swipeActionRight = oldSwipeActionRight;
        swipeActionLeft = oldSwipeActionLeft;
    }

    public void move(float deltaX) {
        float posX = ViewHelper.getX(frontView);
        if (opened.get(downPosition)) {
            posX +=  0;
        }
        if (posX > 0 && !swipingRight) {
            swipingRight = !swipingRight;
            swipeCurrentAction = swipeActionRight;
            if (swipeCurrentAction == SwipeGridView.SWIPE_ACTION_CHOICE) {
                backView.setVisibility(View.GONE);
            } else {
                backView.setVisibility(View.VISIBLE);
            }
        }
        if (posX < 0 && swipingRight) {
            swipingRight = !swipingRight;
            swipeCurrentAction = swipeActionLeft;
            if (swipeCurrentAction == SwipeGridView.SWIPE_ACTION_CHOICE) {
                backView.setVisibility(View.GONE);
            } else {
                backView.setVisibility(View.VISIBLE);
            }
        }
        if (swipeCurrentAction == SwipeGridView.SWIPE_ACTION_DISMISS) {
            setTranslationX(parentView, deltaX);
            setAlpha(parentView, Math.max(0f, Math.min(1f,
                    1f - 2f * Math.abs(deltaX) / viewWidth)));
        } else if (swipeCurrentAction == SwipeGridView.SWIPE_ACTION_CHOICE) {
            if ((swipingRight && deltaX > 0 && posX < DISPLACE_CHOICE)
                    || (!swipingRight && deltaX < 0 && posX > -DISPLACE_CHOICE)
                    || (swipingRight && deltaX < DISPLACE_CHOICE)
                    || (!swipingRight && deltaX > -DISPLACE_CHOICE)) {
                setTranslationX(frontView, deltaX);
            }
        } else {
            setTranslationX(frontView, deltaX);
        }
    }

    class PendingDismissData implements Comparable<PendingDismissData> {
        public int position;
        public View view;

        public PendingDismissData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(PendingDismissData other) {
            return other.position - position;
        }
    }

    protected void performDismiss(final View dismissView, final int dismissPosition, boolean doPendingDismiss) {
        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(animationTime);

        if (doPendingDismiss) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    --dismissAnimationRefCount;
                    if (dismissAnimationRefCount == 0) {
                        removePendingDismisses(originalHeight);
                    }
                }
            });
        }

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

        pendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
        animator.start();
    }

    protected void resetPendingDismisses() {
        pendingDismisses.clear();
    }

    protected void handlerPendingDismisses(final int originalHeight) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removePendingDismisses(originalHeight);
            }
        }, animationTime + 100);
    }

    private void removePendingDismisses(int originalHeight) {

        Collections.sort(pendingDismisses);

        int[] dismissPositions = new int[pendingDismisses.size()];
        for (int i = pendingDismisses.size() - 1; i >= 0; i--) {
            dismissPositions[i] = pendingDismisses.get(i).position;
        }

        ViewGroup.LayoutParams lp;
        for (PendingDismissData pendingDismiss : pendingDismisses) {
            if (pendingDismiss.view != null) {
                setAlpha(pendingDismiss.view, 1f);
                setTranslationX(pendingDismiss.view, 0);
                lp = pendingDismiss.view.getLayoutParams();
                lp.height = originalHeight;
                pendingDismiss.view.setLayoutParams(lp);
            }
        }

        resetPendingDismisses();

    }

}
