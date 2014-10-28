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

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import me.z_wave.android.app.Constants;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.DeviceRgbColor;
import me.z_wave.android.dataModel.DeviceType;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.dataModel.Location;
import me.z_wave.android.dataModel.Notification;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.network.camera.UpdateCameraStateRequest;
import me.z_wave.android.network.devices.DevicesStateRequest;
import me.z_wave.android.network.devices.DevicesStateResponse;
import me.z_wave.android.network.devices.UpdateDeviceRequest;
import me.z_wave.android.network.locations.LocationsRequest;
import me.z_wave.android.network.locations.LocationsResponse;
import me.z_wave.android.network.notification.NotificationDataWrapper;
import me.z_wave.android.network.notification.NotificationRequest;
import me.z_wave.android.network.notification.NotificationResponse;
import me.z_wave.android.network.notification.UpdateNotificationRequest;
import me.z_wave.android.network.profiles.ProfilesRequest;
import me.z_wave.android.network.profiles.ProfilesResponse;
import me.z_wave.android.network.profiles.UpdateProfileRequest;
import me.z_wave.android.utils.BooleanTypeAdapter;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import timber.log.Timber;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;

public class ApiClient {

    public static interface ApiCallback<T, K> {
        public void onSuccess(T result);

        public void onFailure(K request, boolean isNetworkError);
    }

    public static interface EmptyApiCallback<T> {
        public void onSuccess();

        public void onFailure(T request, boolean isNetworkError);
    }

    private LocalProfile mLocalProfile;
    private RestAdapter mAdaptor;
    private Cookie mCookie;

    public void init(LocalProfile profile) {
        mLocalProfile = profile;
        final String url = TextUtils.isEmpty(mLocalProfile.indoorServer)
                ? Constants.DEFAULT_URL : mLocalProfile.indoorServer;
        mAdaptor = new RestAdapter.Builder()
                .setConverter(new GsonConverter(getGson()))
                .setLogLevel(Constants.API_LOG_LEVEL)
                .setEndpoint(url)
                .build();
    }

    public void init(LocalProfile profile, Cookie cookie) {
        mLocalProfile = profile;
        mCookie = cookie;

        final DefaultHttpClient client = HttpClientHelper.createHttpsClient();
        mAdaptor = new RestAdapter.Builder()
                .setRequestInterceptor(createCookiesInterceptor())
                .setConverter(new GsonConverter(getGson()))
                .setLogLevel(Constants.API_LOG_LEVEL)
                .setClient(new ApacheClient(client))
                .setEndpoint(Constants.DEFAULT_URL)
                .build();
    }

    private Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Boolean.class, new BooleanTypeAdapter())
                .create();
    }

    public DevicesStateResponse getDevices(long lastUpdateTime) {
        return mAdaptor.create(DevicesStateRequest.class).getDevices(lastUpdateTime);
    }

    public void updateDevicesState(final Device updatedDevice) {
        final String state = updatedDevice.deviceType == DeviceType.DOORLOCK
                ? updatedDevice.metrics.mode : updatedDevice.metrics.level;

        mAdaptor.create(UpdateDeviceRequest.class).updateDeviceSwitchState(updatedDevice.id, state);
    }

    public void updateDevicesMode(final Device updatedDevice) {
        mAdaptor.create(UpdateDeviceRequest.class).updateMode(updatedDevice.id,
                updatedDevice.metrics.mode);
    }

    public void updateDevicesLevel(final Device updatedDevice) {
        mAdaptor.create(UpdateDeviceRequest.class).updateLevel(updatedDevice.id,
                updatedDevice.metrics.level);
    }

    public void updateToggle(final Device updatedDevice) {
        mAdaptor.create(UpdateDeviceRequest.class).updateToggle(updatedDevice.id);
    }

    public void updateRGBColor(final Device updatedDevice) {
        final DeviceRgbColor color = updatedDevice.metrics.color;
        mAdaptor.create(UpdateDeviceRequest.class).updateRGB(updatedDevice.id,
                color.r, color.g, color.b);
    }


    public void moveCameraRight(final Device updatedDevice) {
        mAdaptor.create(UpdateDeviceRequest.class).moveCameraRight(updatedDevice.id);
    }

    public void moveCameraLeft(final Device updatedDevice) {
        mAdaptor.create(UpdateDeviceRequest.class).moveCameraLeft(updatedDevice.id);
    }

    public void moveCameraUp(final Device updatedDevice) {
        mAdaptor.create(UpdateDeviceRequest.class).movevCameraUp(updatedDevice.id);
    }

    public void moveCameraDown(final Device updatedDevice) {
            mAdaptor.create(UpdateDeviceRequest.class).moveCameraDown(updatedDevice.id);
    }

    public void zoomCameraIn(final Device updatedDevice) {
        mAdaptor.create(UpdateDeviceRequest.class).zoomCameraIn(updatedDevice.id);
    }

    public void zoomCameraOut(final Device updatedDevice) {
        mAdaptor.create(UpdateDeviceRequest.class).zoomCameraOut(updatedDevice.id);
    }

    public void openCamera(final Device updatedDevice) {
        mAdaptor.create(UpdateDeviceRequest.class).openCamera(updatedDevice.id);
    }

    public void closeCamera(final Device updatedDevice) {
        mAdaptor.create(UpdateDeviceRequest.class).closeCamera(updatedDevice.id);
    }

    public LocationsResponse getLocations() {
        return mAdaptor.create(LocationsRequest.class).getLocations();
    }

    public NotificationResponse getNotifications(final long lastUpdateTime) {
        return mAdaptor.create(NotificationRequest.class).getNotifications(Constants.NOTIFICATIONS_LIMIT, lastUpdateTime);
    }

    public void updateNotifications(final Notification notification,
                                    final EmptyApiCallback<String> callback) {
        mAdaptor.create(UpdateNotificationRequest.class).updateNotification(notification.id, notification
                , new Callback<NotificationResponse>() {
                    @Override
                    public void success(NotificationResponse notificationResponse, Response response) {
                        Timber.v(notificationResponse.toString());
                        callback.onSuccess();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        callback.onFailure("", networkUnreachable);
                    }
                }
        );
    }

    public void getNotificationPage(int offset, final ApiCallback<NotificationDataWrapper, String> callback) {
        mAdaptor.create(NotificationRequest.class).getNotifications(Constants.NOTIFICATIONS_LIMIT,
                offset, true, new Callback<NotificationResponse>() {
                    @Override
                    public void success(NotificationResponse notificationResponse, Response response) {
                        Timber.v(notificationResponse.toString());
                        callback.onSuccess(notificationResponse.data);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        callback.onFailure("", networkUnreachable);
                    }
                }
        );
    }

    public ProfilesResponse getProfiles() {
        return mAdaptor.create(ProfilesRequest.class).getProfiles();
    }

    public void updateProfile(Profile profile, final ApiCallback<List<Profile>, String> callback) {
        mAdaptor.create(UpdateProfileRequest.class).updateProfile(profile.id, profile,
                new Callback<ProfilesResponse>() {
                    @Override
                    public void success(ProfilesResponse profileResponse, Response response) {
                        Timber.v(profileResponse.toString());
                        callback.onSuccess(profileResponse.data);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        callback.onFailure("", networkUnreachable);
                    }
                }
        );
    }

    private boolean isNetworkUnreachableError(RetrofitError retrofitError) {
        return retrofitError.isNetworkError() && (retrofitError.getResponse() == null
                || retrofitError.getResponse().getStatus() != 404);
    }

    public boolean isPrepared() {
        return mAdaptor != null;
    }

    private RequestInterceptor createCookiesInterceptor() {
        final CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        // Set up interceptor to include mCookie value in the header.
        return new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                if (mCookie != null && !TextUtils.isEmpty(mCookie.getValue())) {
                    Timber.tag("Cookie values");
                    Timber.v(mCookie.toString());
                    String cookieValue = mCookie.getName() + "=" + mCookie.getValue();
                    request.addHeader("Cookie", cookieValue);
                }
            }
        };
    }


}
