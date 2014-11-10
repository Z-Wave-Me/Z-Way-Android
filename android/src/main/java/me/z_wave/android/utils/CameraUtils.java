/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 03.11.14 13:09.
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

package me.z_wave.android.utils;

import android.text.TextUtils;
import android.webkit.URLUtil;

import me.z_wave.android.app.Constants;
import me.z_wave.android.dataModel.LocalProfile;

/**
 * Created by Ivan Pl on 03.11.2014.
 */
public class CameraUtils {

    public static String getCameraUrl(LocalProfile profile, String baseUrl) {
        if(TextUtils.isEmpty(baseUrl))
            return null;

        if(URLUtil.isValidUrl(baseUrl))
            return baseUrl;

        final String serverUrl = TextUtils.isEmpty(profile.indoorServer) ? Constants.DEFAULT_URL
                : profile.indoorServer;

        return String.format("%s%s", serverUrl, baseUrl);
    }

}
