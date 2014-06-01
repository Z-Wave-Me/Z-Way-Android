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

    @Headers("Content-type: application/json")
    @PUT("/devices/{id}")
    void updateDeviceState(@Path("id") String id, @Body() String updatedDeviceJson,
                           Callback<Device> callback);

    //TODO refactor this!
    @GET("/devices/{id}/command/{state}")
    void updateDeviceExact(@Path("id") String id, @Path("state") String state,
                           Callback<Device> callback);

//    switch:
//
//    devices/:deviceId/command/on
//    devices/:deviceId/command/off
//
//            thermostat
//    devices/:deviceId/command/etMode/:integer
//    devices/:deviceId/command/setTempo/:integer
//
//    multilevel:
//    devices/:deviceId/command/exact/:integer
//
//    fan:
//    devices/:deviceId/command/setMode/:mode
//    devices/:deviceId/command/setMode/off (возможно deprecated)
//
//    door:
//    devices/:deviceId/command/exact/open
//    devices/:deviceId/command/exact/close
//    [1:07:31] Stanislav Morozov: вот так получается
//    [1:07:34] Stanislav Morozov: видимо
//    [1:07:41] Stanislav Morozov: devices/:deviceId/command/on
//    devices/:deviceId/command/off

}
