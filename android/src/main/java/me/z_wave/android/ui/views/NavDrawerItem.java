/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 07.12.14 14:43.
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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import me.z_wave.android.R;

/**
 * Created by Ivan Pl on 07.12.2014.
 */
public class NavDrawerItem extends TextView {
    public NavDrawerItem(Context context) {
        super(context);
    }

    public NavDrawerItem(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.navDrawerItemStyle);
    }

    public NavDrawerItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, R.attr.navDrawerItemStyle);
    }
}
