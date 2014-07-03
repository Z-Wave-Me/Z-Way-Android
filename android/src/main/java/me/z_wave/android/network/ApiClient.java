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
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import me.z_wave.android.app.Constants;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.DeviceType;
import me.z_wave.android.dataModel.DevicesStatus;
import me.z_wave.android.dataModel.Location;
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
import me.z_wave.android.network.profiles.ProfilesRequest;
import me.z_wave.android.network.profiles.ProfilesResponse;
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

    private interface OnAuthCompleteListener{
        public void onAuthComplete();
    }

    private RestAdapter adaptor;
    private DefaultHttpClient client;

    private Cookie cookie;

    private boolean authInProgress;

    public ApiClient(Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(Constants.Z_WAY_PREFERENCES, 0);
        final String serverUrl = preferences.getString(Constants.URL_KEY, Constants.DEFAULT_URL);

        client = getHttpsClient((createHttpClient()));
        adaptor = new RestAdapter.Builder()
                .setEndpoint(serverUrl)
                .setLogLevel(Constants.API_LOG_LEVEL)
                .setClient(new ApacheClient(client))
                .setRequestInterceptor(createCookiesInterceptor())
                .build();
    }

    public void getDevicesState(final long lastUpdateTime, final ApiCallback<DevicesStatus, Long> callback) {
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
                    auth("10382", "demo", new OnAuthCompleteListener() {
                        @Override
                        public void onAuthComplete() {
                            getDevicesState(lastUpdateTime, callback);
                        }
                    });
                } else {
                    callback.onFailure(lastUpdateTime, networkUnreachable);
                }
            }
        });
    }

    public void updateDevicesState(final Device updatedDevice, final EmptyApiCallback<Device> callback) {
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
                            auth("10382", "demo", new OnAuthCompleteListener() {
                                @Override
                                public void onAuthComplete() {
                                    updateDevicesState(updatedDevice, callback);
                                }
                            });
                        } else {
                            callback.onFailure(updatedDevice, networkUnreachable);
                        }

                    }
                }
        );
    }

    public void updateDevicesMode(final Device updatedDevice, final EmptyApiCallback<Device> callback) {
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
                            auth("10382", "demo", new OnAuthCompleteListener() {
                                @Override
                                public void onAuthComplete() {
                                    updateDevicesState(updatedDevice, callback);
                                }
                            });
                        } else {
                            callback.onFailure(updatedDevice, networkUnreachable);
                        }

                    }
                }
        );
    }

    public void updateDevicesLevel(final Device updatedDevice, final EmptyApiCallback<Device> callback) {
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
                            auth("10382", "demo", new OnAuthCompleteListener() {
                                @Override
                                public void onAuthComplete() {
                                    updateDevicesState(updatedDevice, callback);
                                }
                            });
                        } else {
                            callback.onFailure(updatedDevice, networkUnreachable);
                        }

                    }
                }
        );
    }

    public void updateTogle(final Device updatedDevice, final EmptyApiCallback<Device> callback) {
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
                            auth("10382", "demo", new OnAuthCompleteListener() {
                                @Override
                                public void onAuthComplete() {
                                    updateDevicesState(updatedDevice, callback);
                                }
                            });
                        } else {
                            callback.onFailure(updatedDevice, networkUnreachable);
                        }

                    }
                }
        );
    }

    public void getLocations(final ApiCallback<List<Location>, String> callback) {
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
                    auth("10382", "demo", new OnAuthCompleteListener() {
                        @Override
                        public void onAuthComplete() {
                            getLocations(callback);
                        }
                    });
                } else {
                    callback.onFailure("", networkUnreachable);
                }
            }
        });
    }

    public void auth(final String login, final String password,final OnAuthCompleteListener listener) {
        authInProgress = true;
        adaptor.create(AuthRequest.class).auth("login", login, password,
                new Callback<Object>() {
                    @Override
                    public void success(Object obj, Response response) {
                        Timber.v(obj.toString());
                        if(client.getCookieStore().getCookies().size() > 0)
                            cookie = client.getCookieStore().getCookies().get(0);
                        listener.onAuthComplete();
                        authInProgress = false;
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        error.printStackTrace();
                        if(client.getCookieStore().getCookies().size() > 0)
                            cookie = client.getCookieStore().getCookies().get(0);
                        listener.onAuthComplete();
                        authInProgress = false;
                    }
                }
        );
    }

    public void getNotifications(final long lastUpdateTime,
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
                            auth("10382", "demo", new OnAuthCompleteListener() {
                                @Override
                                public void onAuthComplete() {
                                    getNotifications(lastUpdateTime, callback);
                                }
                            });
                        } else {
                            callback.onFailure(lastUpdateTime, networkUnreachable);
                        }
                    }
                }
        );
    }

    public void getProfiles(final ApiCallback<List<Profile>, String> callback) {
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
                            auth("10382", "demo", new OnAuthCompleteListener() {
                                @Override
                                public void onAuthComplete() {
                                    getProfiles(callback);
                                }
                            });
                        } else {
                            callback.onFailure("", networkUnreachable);
                        }
                    }
                }
        );
    }

    private boolean isNetworkUnreachableError(RetrofitError retrofitError) {
        return retrofitError.isNetworkError() && (retrofitError.getResponse() == null
                || retrofitError.getResponse().getStatus() != 404);
    }

    private HttpClient createHttpClient(){
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("https",
                SSLSocketFactory.getSocketFactory(), 443));

        HttpParams params = new BasicHttpParams();

        SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);

        return new DefaultHttpClient(mgr, params);
    }

    public class ExSSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public ExSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);
            TrustManager x509TrustManager = new X509TrustManager() {

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {

                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {

                }

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { x509TrustManager }, null);
        }

        public ExSSLSocketFactory(SSLContext context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
            super(null);
            sslContext = context;
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    public DefaultHttpClient getHttpsClient(HttpClient client) {
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

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
            SSLSocketFactory sslSocketFactory = new ExSSLSocketFactory(sslContext);
            sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager clientConnectionManager = client.getConnectionManager();
            SchemeRegistry schemeRegistry = clientConnectionManager.getSchemeRegistry();
            schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
            return new DefaultHttpClient(clientConnectionManager, client.getParams());
        } catch (Exception ex) {
            return null;
        }
    }

    private RequestInterceptor createCookiesInterceptor(){
        final CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        // Set up interceptor to include cookie value in the header.
        return new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {

                if (cookie != null && !TextUtils.isEmpty(cookie.getValue())) {
                    // Set up expiration in format desired by cookies
                    // (arbitrarily one hour from now).
                    Timber.tag("Cookie values");
                    Timber.v(cookie.toString());
                    Date expiration = new Date(System.currentTimeMillis() + 60 * 60 * 1000);
                    String expires = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
                            .format(expiration);

                    String cookieValue = cookie.getName() + "=" + cookie.getValue();
//                    + "; " +
//                            "path=" + cookie.getPath() + "; " +
//                            "domain=" + cookie.getDomain() + ";" +
//                            "expires=" + cookie.getExpiryDate().toGMTString();

                    request.addHeader("Cookie", cookieValue);
                }
            }
        };
    }


}
