/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 28.05.14 18:05.
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

package me.z_wave.android.app;

import retrofit.RestAdapter;

public class Constants {

    public static final String BASE_API_URL = "/ZAutomation/api/v1";
    public static final RestAdapter.LogLevel API_LOG_LEVEL = RestAdapter.LogLevel.BASIC;

    public static final String Z_WAY_PREFERENCES = "z_way_preferences";

    //TODO remove stub
    public static final String URL_KEY = "url_key";
    public static final String DEFAULT_URL = "https://find.z-wave.me";//"http://mskoff.z-wave.me:10483";

}
