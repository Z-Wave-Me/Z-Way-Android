/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 30.06.14 18:49.
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

package me.z_wave.android.network.auth;

import me.z_wave.android.utils.BooleanTypeAdapter;
import retrofit.http.Field;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Headers;
import retrofit.http.Part;


/**
 * Created by Oleg Gerasimov on 02.09.2015
 */
public interface LocalAuthRequest {

    @POST("/ZAutomation/api/v1/login")
    Object auth(@Body LocalAuth localAuth);

}
