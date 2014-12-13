/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 07.12.14 21:02.
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

package me.z_wave.android.dataModel;

import android.content.Context;

import me.z_wave.android.R;

/**
 * Created by Ivan Pl on 07.12.2014.
 */
public enum  Theme {

    DEFAULT, THEME_1, THEME_2, THEME_3, THEME_4;

    public String getThemeTitle(Context context) {
        switch (this) {
            case DEFAULT:
                return context.getString(R.string.theme_default_name);
            case THEME_1:
                return context.getString(R.string.theme_1_name);
            case THEME_2:
                return context.getString(R.string.theme_2_name);
            case THEME_3:
                return context.getString(R.string.theme_3_name);
            case THEME_4:
                return context.getString(R.string.theme_4_name);
            default:
                return "N/A";
        }
    }

    public int getThemeId() {
        switch (this) {
            case DEFAULT:
                return R.style.ZWayAppBaseTheme;
            case THEME_1:
                return R.style.ZWayAppTheme1;
            case THEME_2:
                return R.style.ZWayAppTheme2;
            case THEME_3:
                return R.style.ZWayAppTheme3;
            case THEME_4:
                return R.style.ZWayAppTheme4;
            default:
                return R.style.ZWayAppBaseTheme;
        }
    }

    public int getThemeColorId() {
        switch (this) {
            case DEFAULT:
                return R.color.main_app_color;
            case THEME_1:
                return R.color.theme_1_app_color;
            case THEME_2:
                return R.color.theme_2_app_color;
            case THEME_3:
                return R.color.theme_3_app_color;
            case THEME_4:
                return R.color.theme_4_app_color;
            default:
                return R.color.main_app_color;
        }
    }

}
