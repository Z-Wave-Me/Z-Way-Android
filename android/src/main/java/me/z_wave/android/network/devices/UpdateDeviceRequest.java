/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 01.06.14 19:28.
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

package me.z_wave.android.network.devices;

import me.z_wave.android.dataModel.Device;
import retrofit.Callback;
import retrofit.http.*;

public interface UpdateDeviceRequest {

    //TODO refactor this!
    @GET("/ZAutomation/api/v1/devices/{id}/command/{state}")
    void updateDeviceSwitchState(@Path("id") String id, @Path("state") String state, Callback<Device> callback);

    @GET("/ZAutomation/api/v1/devices/{id}/command/setMode")
    void updateMode(@Path("id") String id, @Query("mode") String mode, Callback<Device> callback);

    @GET("/ZAutomation/api/v1/devices/{id}/command/exact")
    void updateLevel(@Path("id") String id, @Query("level") String level, Callback<Device> callback);

    @GET("/ZAutomation/api/v1/devices/{id}/command/on")
    void updateTogle(@Path("id") String id, Callback<Device> callback);

}
