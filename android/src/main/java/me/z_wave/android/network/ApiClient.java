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
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import me.z_wave.android.app.Constants;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.DeviceType;
import me.z_wave.android.dataModel.DevicesStatus;
import me.z_wave.android.dataModel.Location;
import me.z_wave.android.dataModel.Notification;
import me.z_wave.android.dataModel.Profile;
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
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;
import retrofit.client.Response;
import timber.log.Timber;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public interface OnAuthCompleteListener{
        public void onAuthComplete();
    }

    private static DefaultHttpClient client = getHttpsClient();
    private static RestAdapter adaptor = new RestAdapter.Builder()
            .setEndpoint(Constants.DEFAULT_URL)
            .setLogLevel(Constants.API_LOG_LEVEL)
            .setClient(new ApacheClient(client))
            .setRequestInterceptor(createCookiesInterceptor())
    .build();;

    private static Cookie cookie;

    private static boolean authInProgress;

    public static void getDevicesState(final long lastUpdateTime, final ApiCallback<DevicesStatus, Long> callback) {
        adaptor.create(DevicesStateRequest.class).getDevices(lastUpdateTime, new Callback<DevicesStateResponse>() {
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
                if(!networkUnreachable && !authInProgress){
                    getDevicesState(lastUpdateTime, callback);
//                    auth("10903", "newui", new OnAuthCompleteListener() {
//                        @Override
//                        public void onAuthComplete() {
//                            getDevicesState(lastUpdateTime, callback);
//                        }
//                    });
                } else {
                    callback.onFailure(lastUpdateTime, networkUnreachable);
                }
            }
        });
    }

    public static void updateDevicesState(final Device updatedDevice, final EmptyApiCallback<Device> callback) {
        final String state = updatedDevice.deviceType == DeviceType.DOORLOCK
                ? updatedDevice.metrics.mode : updatedDevice.metrics.level;

        adaptor.create(UpdateDeviceRequest.class).updateDeviceSwitchState(updatedDevice.id, state,
                new Callback<Device>() {
                    @Override
                    public void success(Device objects, Response response) {
                        Timber.v(objects.toString());
                        callback.onSuccess();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        if (!networkUnreachable && !authInProgress) {
                            updateDevicesState(updatedDevice, callback);
//                            auth("10903", "newui", new OnAuthCompleteListener() {
//                                @Override
//                                public void onAuthComplete() {
//                                    updateDevicesState(updatedDevice, callback);
//                                }
//                            });
                        } else {
                            callback.onFailure(updatedDevice, networkUnreachable);
                        }

                    }
                }
        );
    }

    public static void updateDevicesMode(final Device updatedDevice, final EmptyApiCallback<Device> callback) {
        adaptor.create(UpdateDeviceRequest.class).updateMode(updatedDevice.id,
                updatedDevice.metrics.mode, new Callback<Device>() {
                    @Override
                    public void success(Device objects, Response response) {
                        Timber.v(objects.toString());
                        callback.onSuccess();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        if (!networkUnreachable && !authInProgress) {
                            updateDevicesState(updatedDevice, callback);
//                            auth("10903", "newui", new OnAuthCompleteListener() {
//                                @Override
//                                public void onAuthComplete() {
//                                    updateDevicesState(updatedDevice, callback);
//                                }
//                            });
                        } else {
                            callback.onFailure(updatedDevice, networkUnreachable);
                        }

                    }
                }
        );
    }

    public static void updateDevicesLevel(final Device updatedDevice, final EmptyApiCallback<Device> callback) {
        adaptor.create(UpdateDeviceRequest.class).updateLevel(updatedDevice.id,
                updatedDevice.metrics.level, new Callback<Device>() {
                    @Override
                    public void success(Device objects, Response response) {
                        Timber.v(objects.toString());
                        callback.onSuccess();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        if (!networkUnreachable && !authInProgress) {
                            updateDevicesState(updatedDevice, callback);
//                            auth("10903", "newui", new OnAuthCompleteListener() {
//                                @Override
//                                public void onAuthComplete() {
//                                    updateDevicesState(updatedDevice, callback);
//                                }
//                            });
                        } else {
                            callback.onFailure(updatedDevice, networkUnreachable);
                        }

                    }
                }
        );
    }

    public static void updateTogle(final Device updatedDevice, final EmptyApiCallback<Device> callback) {
        adaptor.create(UpdateDeviceRequest.class).updateTogle(updatedDevice.id, new Callback<Device>() {
                    @Override
                    public void success(Device objects, Response response) {
                        Timber.v(objects.toString());
                        callback.onSuccess();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        if (!networkUnreachable && !authInProgress) {
                            updateDevicesState(updatedDevice, callback);
//                            auth("10903", "newui", new OnAuthCompleteListener() {
//                                @Override
//                                public void onAuthComplete() {
//                                    updateDevicesState(updatedDevice, callback);
//                                }
//                            });
                        } else {
                            callback.onFailure(updatedDevice, networkUnreachable);
                        }

                    }
                }
        );
    }

    public static void getLocations(final ApiCallback<List<Location>, String> callback) {
        adaptor.create(LocationsRequest.class).getLocations(new Callback<LocationsResponse>() {
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
                if(!networkUnreachable && !authInProgress){
                    getLocations(callback);
//                    auth("10903", "newui", new OnAuthCompleteListener() {
//                        @Override
//                        public void onAuthComplete() {
//                            getLocations(callback);
//                        }
//                    });
                } else {
                    callback.onFailure("", networkUnreachable);
                }
            }
        });
    }

    public static void auth(final String login, final String password,final OnAuthCompleteListener listener) {
        authInProgress = false;
        adaptor.create(AuthRequest.class).auth("login", login, password,
                new Callback<Object>() {
                    @Override
                    public void success(Object obj, Response response) {
                        if(client.getCookieStore() == null || client.getCookieStore().getCookies().size() == 0){
                            auth(login, password, listener);
                        } else {
                            cookie = client.getCookieStore().getCookies().get(0);
                            listener.onAuthComplete();
                            authInProgress = false;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if(client.getCookieStore() == null || client.getCookieStore().getCookies().size() == 0 ||
                                TextUtils.isEmpty(client.getCookieStore().getCookies().get(0).getValue())){
                            auth(login, password, listener);
                        } else {
                            cookie = client.getCookieStore().getCookies().get(0);
                                listener.onAuthComplete();
                                authInProgress = false;
                        }
                    }
                }
        );
    }

    public static void getNotifications(final long lastUpdateTime,
                                        final ApiCallback<NotificationDataWrapper, Long> callback) {
        adaptor.create(NotificationRequest.class).getNotifications(
                lastUpdateTime, new Callback<NotificationResponse>() {
                    @Override
                    public void success(NotificationResponse notificationResponse, Response response) {
                        Timber.v(notificationResponse.toString());
                        callback.onSuccess(notificationResponse.data);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        if(!networkUnreachable && !authInProgress){
                            getNotifications(lastUpdateTime, callback);
//                            auth("10903", "newui", new OnAuthCompleteListener() {
//                                @Override
//                                public void onAuthComplete() {
//                                    getNotifications(lastUpdateTime, callback);
//                                }
//                            });
                        } else {
                            callback.onFailure(lastUpdateTime, networkUnreachable);
                        }
                    }
                }
        );
    }

    public static void updateNotifications(final Notification notification,
                                        final EmptyApiCallback<String> callback) {
        adaptor.create(UpdateNotificationRequest.class).updateNotification(notification.id, notification
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

    public static void getProfiles(final ApiCallback<List<Profile>, String> callback) {
        adaptor.create(ProfilesRequest.class).getProfiles(
                new Callback<ProfilesResponse>() {
                    @Override
                    public void success(ProfilesResponse profileResponse, Response response) {
                        Timber.v(profileResponse.toString());
                        callback.onSuccess(profileResponse.data);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        if(!networkUnreachable && !authInProgress){
                            getProfiles(callback);
//                            auth("10903", "new", new OnAuthCompleteListener() {
//                                @Override
//                                public void onAuthComplete() {
//                                    getProfiles(callback);
//                                }
//                            });
                        } else {
                            callback.onFailure("", networkUnreachable);
                        }
                    }
                }
        );
    }

    public static void updateProfiles(Profile profile, final ApiCallback<List<Profile>, String> callback) {
        adaptor.create(UpdateProfileRequest.class).updateProfile(profile.id, profile,
                new Callback<ProfilesResponse>() {
                    @Override
                    public void success(ProfilesResponse profileResponse, Response response) {
                        Timber.v(profileResponse.toString());
                        callback.onSuccess(profileResponse.data);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        boolean networkUnreachable = isNetworkUnreachableError(error);
                        if (!networkUnreachable && !authInProgress) {
                            getProfiles(callback);
//                            auth("10903", "new", new OnAuthCompleteListener() {
//                                @Override
//                                public void onAuthComplete() {
//                                    getProfiles(callback);
//                                }
//                            });
                        } else {
                            callback.onFailure("", networkUnreachable);
                        }
                    }
                }
        );
    }

    private static boolean isNetworkUnreachableError(RetrofitError retrofitError) {
        return retrofitError.isNetworkError() && (retrofitError.getResponse() == null
                || retrofitError.getResponse().getStatus() != 404);
    }

    public static DefaultHttpClient getHttpsClient() {
        try{
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

            HttpClient client = new DefaultHttpClient();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
            SSLSocketFactory sslSocketFactory = new ExSSLSocketFactory(sslContext);
            sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            ClientConnectionManager cm = new ThreadSafeClientConnManager(client.getParams(), schemeRegistry);
            schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
            return new DefaultHttpClient(cm, client.getParams());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static RequestInterceptor createCookiesInterceptor(){
        final CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        // Set up interceptor to include cookie value in the header.
        return new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                if (cookie != null && !TextUtils.isEmpty(cookie.getValue())) {
                    Timber.tag("Cookie values");
                    Timber.v(cookie.toString());
                    String cookieValue = cookie.getName() + "=" + cookie.getValue();
                    request.addHeader("Cookie", cookieValue);
                }
            }
        };
    }


}
