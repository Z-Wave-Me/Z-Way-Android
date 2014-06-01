/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 31.05.14 12:51.
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

package me.z_wave.android.network;

import me.z_wave.android.app.Constants;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.DevicesStatus;
import me.z_wave.android.network.devices.DevicesStateRequest;
import me.z_wave.android.network.devices.DevicesStateResponse;
import me.z_wave.android.network.devices.UpdateDeviceRequest;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;


public class ApiClient {

    private static final String TAG = ApiClient.class.getSimpleName();


    public static interface ApiCallback<T, K> {
        public void onSuccess(T result);

        public void onFailure(K request, boolean isNetworkError);
    }

    public static interface EmptyApiCallback<T> {
        public void onSuccess();

        public void onFailure(T request, boolean isNetworkError);
    }

    private static RestAdapter sAdaptor = new RestAdapter.Builder()
            .setEndpoint(Constants.BASE_API_URL)
            .setLogLevel(Constants.API_LOG_LEVEL)
            .build();


    public static void getDevicesState(final long lastUpdateTime, final ApiCallback<DevicesStatus, Long> callback) {
        sAdaptor.create(DevicesStateRequest.class).getDevices(lastUpdateTime, new Callback<DevicesStateResponse>() {
            @Override
            public void success(DevicesStateResponse devicesDataResponse, Response response) {
                Timber.v(devicesDataResponse.toString());
                if (devicesDataResponse.code != 200) {
                    callback.onFailure(lastUpdateTime, false);
                } else {
                    callback.onSuccess(devicesDataResponse.data);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                boolean networkUnreachable = isNetworkUnreachableError(error);
                callback.onFailure(lastUpdateTime, networkUnreachable);
            }
        });
    }

    public static void updateDevicesState(final Device updatedDevice, final EmptyApiCallback<Device> callback) {
        sAdaptor.create(UpdateDeviceRequest.class).updateDeviceExact(updatedDevice.id, updatedDevice.metrics.level,
         new  Callback<Device>() {
            @Override
            public void success(Device objects, Response response) {
                Timber.v(objects.toString());
            }

            @Override
            public void failure(RetrofitError error) {
                boolean networkUnreachable = isNetworkUnreachableError(error);
                callback.onFailure(updatedDevice, networkUnreachable);
            }
        });
    }

    private static boolean isNetworkUnreachableError(RetrofitError retrofitError) {
        return retrofitError.isNetworkError() && (retrofitError.getResponse() == null
                || retrofitError.getResponse().getStatus() != 404);
    }

}
