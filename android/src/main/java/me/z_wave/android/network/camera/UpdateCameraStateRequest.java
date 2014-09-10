/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 10.09.14 22:37.
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

package me.z_wave.android.network.camera;

import me.z_wave.android.dataModel.Device;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by Ivan PL on 10.09.2014.
 */
public interface UpdateCameraStateRequest {

    @GET("/ZAutomation/api/v1/devices/{id}/command/zoomIn")
    void zoomIn(@Path("id") String id, Callback<Device> callback);

    @GET("/ZAutomation/api/v1/devices/{id}/command/zoomOut")
    void zoomOut(@Path("id") String id, Callback<Device> callback);

    @GET("/ZAutomation/api/v1/devices/{id}/command/left")
    void moveLeft(@Path("id") String id, Callback<Device> callback);

    @GET("/ZAutomation/api/v1/devices/{id}/command/right")
    void moveRight(@Path("id") String id, Callback<Device> callback);

    @GET("/ZAutomation/api/v1/devices/{id}/command/up")
    void moveUp(@Path("id") String id, Callback<Device> callback);

    @GET("/ZAutomation/api/v1/devices/{id}/command/down")
    void moveDown(@Path("id") String id, Callback<Device> callback);

    @GET("/ZAutomation/api/v1/devices/{id}/command/open")
    void openCamera(@Path("id") String id, Callback<Device> callback);

    @GET("/ZAutomation/api/v1/devices/{id}/command/close")
    void closeCamera(@Path("id") String id, Callback<Device> callback);

}