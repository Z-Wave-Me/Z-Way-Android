/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 22.06.14 10:57.
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

/**
 * Created by Ivan PL on 22.06.2014.
 */
public interface SwipeListViewListener {

    /**
     * Called when open animation finishes
     * @param position list item
     * @param toRight Open to right
     */
    void onOpened(int position, boolean toRight);

    /**
     * Called when close animation finishes
     * @param position list item
     * @param fromRight Close from right
     */
    void onClosed(int position, boolean fromRight);

    /**
     * Called when the list changed
     */
    void onListChanged();

    /**
     * Called when user is moving an item
     * @param position list item
     * @param x Current position X
     */
    void onMove(int position, float x);

    /**
     * Start open item
     * @param position list item
     * @param action current action
     * @param right to right
     */
    void onStartOpen(int position, int action, boolean right);

    /**
     * Start close item
     * @param position list item
     * @param right
     */
    void onStartClose(int position, boolean right);

    /**
     * Called when user clicks on the front view
     * @param position list item
     */
    void onClickFrontView(int position);

    /**
     * Called when user clicks on the back view
     * @param position list item
     */
    void onClickBackView(int position);

    /**
     * Called when user dismisses items
     * @param reverseSortedPositions Items dismissed
     */
    void onDismiss(int[] reverseSortedPositions);

    /**
     * Used when user want to change swipe list mode on some rows. Return SWIPE_MODE_DEFAULT
     * if you don't want to change swipe list mode
     * @param position position that you want to change
     * @return type
     */
    int onChangeSwipeMode(int position);

    /**
     * Called when user choice item
     * @param position position that choice
     * @param selected if item is selected or not
     */
    void onChoiceChanged(int position, boolean selected);

    /**
     * User start choice items
     */
    void onChoiceStarted();

    /**
     * User end choice items
     */
    void onChoiceEnded();

    /**
     * User is in first item of list
     */
    void onFirstListItem();

    /**
     * User is in last item of list
     */
    void onLastListItem();

}