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

import android.content.Context;
import android.text.TextUtils;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import me.z_wave.android.app.Constants;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.DeviceType;
import me.z_wave.android.dataModel.DevicesStatus;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.dataModel.Location;
import me.z_wave.android.dataModel.Notification;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.dataModel.ServerStatus;
import me.z_wave.android.network.auth.AuthRequest;
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
import me.z_wave.android.network.server.ServerStatusRequest;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;
import retrofit.client.Response;
import timber.log.Timber;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ApiClient {

    public static interface ApiCallback<T, K> {
        public void onSuccess(T result);

        public void onFailure(K request, boolean isNetworkError);
    }

    public static interface EmptyApiCallback<T> {
        public void onSuccess();

        public void onFailure(T request, boolean isNetworkError);
    }

    public static interface SimpleApiCallback<T> {
        public void onSuccess(T response);

        public void onFailure( boolean isNetworkError);
    }


    public interface OnAuthCompleteListener {
        public void onAuthComplete();

        public void onAuthFiled();
    }

    private LocalProfile mLocalProfile;
    private DefaultHttpClient mClient;
    private RestAdapter mAdaptor;
    private Cookie mCookie;
    private boolean mIgnoreRequestResults = false;


    private boolean mAuthInProgress;
    private int mAuthTriesCounter;

    public void init(LocalProfile localProfile) {
        init(localProfile, false);
    }

    public void init(LocalProfile localProfile, boolean useDefaultUrl) {
        if(localProfile != null) {
            Timber.v("init ApiClient for " + localProfile.toString());
            mLocalProfile = localProfile;
            String url;
            if(useDefaultUrl) {
                url = Constants.DEFAULT_URL;
            } else {
                url = TextUtils.isEmpty(mLocalProfile.indoorServer)
                        ? Constants.DEFAULT_URL : mLocalProfile.indoorServer;
            }
            mClient = getHttpsClient();
            mAdaptor = new RestAdapter.Builder()
                    .setEndpoint(url)
                    .setLogLevel(Constants.API_LOG_LEVEL)
                    .setClient(new ApacheClient(mClient))
                    .setRequestInterceptor(createCookiesInterceptor())
                    .build();
        }
    }

    public void getServerState() {

    }

    public void getServerState(String login, String password) {

    }

    public void getDevicesState(final long lastUpdateTime, final ApiCallback<DevicesStatus, Long> callback) {
        mAdaptor.create(DevicesStateRequest.class).getDevices(lastUpdateTime, new Callback<DevicesStateResponse>() {
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
                if (!networkUnreachable && !mAuthInProgress) {
                    getDevicesState(lastUpdateTime, callback);
                } else {
                    callback.onFailure(lastUpdateTime, networkUnreachable);
                }
            }
        });
    }

    public DevicesStateResponse getDevices() {
        return mAdaptor.create(DevicesStateRequest.class).getDevices();
    }

    public void updateDevicesState(final Device updatedDevice, final EmptyApiCallback<Device> callback) {
        final String state = updatedDevice.deviceType == DeviceType.DOORLOCK
                ? updatedDevice.metrics.mode : updatedDevice.metrics.level;

        mAdaptor.create(UpdateDeviceRequest.class).updateDeviceSwitchState(updatedDevice.id, state,
                new Callback<Device>() {
                    @Override
                    public void success(Device objects, Response response) {
                        Timber.v(objects.toString());
                        callback.onSuccess();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        if (!networkUnreachable && !mAuthInProgress) {
                            updateDevicesState(updatedDevice, callback);
                        } else {
                            callback.onFailure(updatedDevice, networkUnreachable);
                        }

                    }
                }
        );
    }

    public void updateDevicesMode(final Device updatedDevice, final EmptyApiCallback<Device> callback) {
        mAdaptor.create(UpdateDeviceRequest.class).updateMode(updatedDevice.id,
                updatedDevice.metrics.mode, new Callback<Device>() {
                    @Override
                    public void success(Device objects, Response response) {
                        Timber.v(objects.toString());
                        callback.onSuccess();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        if (!networkUnreachable && !mAuthInProgress) {
                            updateDevicesState(updatedDevice, callback);
                        } else {
                            callback.onFailure(updatedDevice, networkUnreachable);
                        }

                    }
                }
        );
    }

    public void updateDevicesLevel(final Device updatedDevice, final EmptyApiCallback<Device> callback) {
        mAdaptor.create(UpdateDeviceRequest.class).updateLevel(updatedDevice.id,
                updatedDevice.metrics.level, new Callback<Device>() {
                    @Override
                    public void success(Device objects, Response response) {
                        Timber.v(objects.toString());
                        callback.onSuccess();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        if (!networkUnreachable && !mAuthInProgress) {
                            updateDevicesState(updatedDevice, callback);
                        } else {
                            callback.onFailure(updatedDevice, networkUnreachable);
                        }

                    }
                }
        );
    }

    public void updateTogle(final Device updatedDevice, final EmptyApiCallback<Device> callback) {
        mAdaptor.create(UpdateDeviceRequest.class).updateTogle(updatedDevice.id, new Callback<Device>() {
                    @Override
                    public void success(Device objects, Response response) {
                        Timber.v(objects.toString());
                        callback.onSuccess();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        if (!networkUnreachable && !mAuthInProgress) {
                            updateDevicesState(updatedDevice, callback);
                        } else {
                            callback.onFailure(updatedDevice, networkUnreachable);
                        }

                    }
                }
        );
    }

    public void getLocations(final ApiCallback<List<Location>, String> callback) {
        mAdaptor.create(LocationsRequest.class).getLocations(new Callback<LocationsResponse>() {
            @Override
            public void success(LocationsResponse locationsResponse, Response response) {
                Timber.v(locationsResponse.toString());
                if (locationsResponse.code != 200) {
                    callback.onFailure("", false);
                } else {
                    callback.onSuccess(locationsResponse.data);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                boolean networkUnreachable = isNetworkUnreachableError(error);
                callback.onFailure("", networkUnreachable);
            }
        });
    }

    public LocationsResponse getLocations() {
        return mAdaptor.create(LocationsRequest.class).getLocations();
    }

    public void auth(final OnAuthCompleteListener listener) {
        mAuthInProgress = false;
        mAdaptor.create(AuthRequest.class).auth("login", mLocalProfile.login, mLocalProfile.password,
                new Callback<Object>() {
                    @Override
                    public void success(Object obj, Response response) {
                        onAuthResult(listener);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onAuthResult(listener);
                    }
                }
        );
    }

    public void checkServerStatus(final SimpleApiCallback<ServerStatus> callback) {
        mAdaptor.create(ServerStatusRequest.class).getServerStatus(
                new Callback<ServerStatus>() {
                    @Override
                    public void success(ServerStatus profileResponse, Response response) {
                        if(mIgnoreRequestResults) {
                            mIgnoreRequestResults = false;
                        } else {
                            Timber.v(profileResponse.toString());
                            callback.onSuccess(profileResponse);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if(mIgnoreRequestResults) {
                            mIgnoreRequestResults = false;
                        } else {
                            boolean networkUnreachable = isNetworkUnreachableError(error);
                            callback.onFailure(networkUnreachable);
                        }
                    }
                }
        );
    }

    public void cancelConnection(){
        mIgnoreRequestResults = true;
    }

    private void onAuthResult(OnAuthCompleteListener listener) {
        if(mIgnoreRequestResults) {
            mAuthTriesCounter = 0;
            mAuthInProgress = false;
            mIgnoreRequestResults = false;
        } else {
            mAuthTriesCounter++;
            if (mClient.getCookieStore() == null || mClient.getCookieStore().getCookies().size() == 0 ||
                    TextUtils.isEmpty(mClient.getCookieStore().getCookies().get(0).getValue())) {
                if (mAuthTriesCounter >= Constants.AUTH_TRIES_COUNT) {
                    mAuthTriesCounter = 0;
                    listener.onAuthFiled();
                } else {
                    auth(listener);
                }
            } else {
                mCookie = mClient.getCookieStore().getCookies().get(0);
                listener.onAuthComplete();
                mAuthInProgress = false;
            }
        }
    }

    public void getNotifications(final long lastUpdateTime,
                                 final ApiCallback<NotificationDataWrapper, Long> callback) {
        mAdaptor.create(NotificationRequest.class).getNotifications(
                lastUpdateTime, new Callback<NotificationResponse>() {
                    @Override
                    public void success(NotificationResponse notificationResponse, Response response) {
                        Timber.v(notificationResponse.toString());
                        callback.onSuccess(notificationResponse.data);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        if (!networkUnreachable && !mAuthInProgress) {
                            getNotifications(lastUpdateTime, callback);
                        } else {
                            callback.onFailure(lastUpdateTime, networkUnreachable);
                        }
                    }
                }
        );
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

    public void getProfiles(final ApiCallback<List<Profile>, String> callback) {
        mAdaptor.create(ProfilesRequest.class).getProfiles(
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

    public ProfilesResponse getProfiles() {
        return mAdaptor.create(ProfilesRequest.class).getProfiles();
    }

    public void updateProfiles(Profile profile, final ApiCallback<List<Profile>, String> callback) {
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

    private DefaultHttpClient getHttpsClient() {
        try {
            X509TrustManager x509TrustManager = new X509TrustManager() {

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {

                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {

                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            HttpClient client = createDefaultHttpClient();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
            SSLSocketFactory sslSocketFactory = new ExSSLSocketFactory(sslContext);
            sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            ClientConnectionManager cm = new ThreadSafeClientConnManager(client.getParams(), schemeRegistry);
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schemeRegistry.register(new Scheme("https", sslSocketFactory, 80));
            schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
            return new DefaultHttpClient(cm, client.getParams());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private HttpClient createDefaultHttpClient(){
        final HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
        HttpConnectionParams.setSoTimeout(httpParameters, 5000);
       return new DefaultHttpClient(httpParameters);
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
