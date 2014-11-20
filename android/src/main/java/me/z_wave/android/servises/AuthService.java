/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 19.10.14 13:04.
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

package me.z_wave.android.servises;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Subscribe;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.inject.Inject;

import me.z_wave.android.app.Constants;
import me.z_wave.android.app.ZWayApplication;
import me.z_wave.android.data.DataContext;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.network.HttpClientHelper;
import me.z_wave.android.network.auth.AuthRequest;
import me.z_wave.android.network.server.ServerStatusRequest;
import me.z_wave.android.otto.MainThreadBus;
import me.z_wave.android.otto.events.AccountChangedEvent;
import me.z_wave.android.otto.events.AuthEvent;
import me.z_wave.android.otto.events.CancelConnectionEvent;
import me.z_wave.android.utils.BooleanTypeAdapter;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;
import retrofit.converter.GsonConverter;
import timber.log.Timber;

public class AuthService extends IntentService {

    public enum LoginType {
        OUTDOOR, WITH_CREDENTIALS
    }

    private static final String ACTION_LOGIN = "me.z_wave.android.servises.action.LOGIN";

    private static final String EXTRA_LOGIN_PROFILE = "me.z_wave.android.servises.extra.PROFILE";

    @Inject
    ApiClient apiClient;
    @Inject
    DataContext dataContext;
    @Inject
    MainThreadBus bus;

    private DefaultHttpClient mClient;
    private Cookie mCookie;
    private int mAuthTriesCounter;
    private boolean mCancelEvent;

    public static void login(Context context, LocalProfile profile) {
        Intent intent = new Intent(context, AuthService.class);
        intent.setAction(ACTION_LOGIN);
        intent.putExtra(EXTRA_LOGIN_PROFILE, profile);
        context.startService(intent);
    }

    public AuthService() {
        super("AuthorizationService");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        ((ZWayApplication) getApplicationContext()).inject(this);
        bus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOGIN.equals(action)) {
                final LocalProfile profile = (LocalProfile) intent
                        .getSerializableExtra(EXTRA_LOGIN_PROFILE);
                handleActionAuth(profile);
            }
        }
    }

    @Subscribe
    public void onCancelEvent(CancelConnectionEvent event) {
        mCancelEvent = true;
    }

    private void handleActionAuth(LocalProfile profile) {
        mAuthTriesCounter = 0;
        final boolean useDefaultUrl = TextUtils.isEmpty(profile.indoorServer);
        final RestAdapter adapter = prepareRestAdaptor(profile, useDefaultUrl);
        if (useDefaultUrl) {
            authWithUserCredentials(adapter, profile);
        } else {
            connectToOutdoorService(adapter, profile);
        }
    }

    public RestAdapter prepareRestAdaptor(LocalProfile profile, boolean useDefaultUrl) {
        if (profile != null) {
            Timber.v("init ApiClient for %s", profile);
            mClient = HttpClientHelper.createHttpsClient();
            final String url = getServerUrl(profile, useDefaultUrl);
            final Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Boolean.class, new BooleanTypeAdapter())
                    .create();

            return new RestAdapter.Builder()
                    .setEndpoint(url)
                    .setLogLevel(Constants.API_LOG_LEVEL)
                    .setClient(new ApacheClient(mClient))
                    .setConverter(new GsonConverter(gson))
                    .build();
        }
        return null;
    }


    private void connectToOutdoorService(RestAdapter adapter, final LocalProfile profile) {
        Timber.v("Connect to outdoor, try " + (mAuthTriesCounter + 1));
        if(mCancelEvent) {
            return;
        }

        mAuthTriesCounter++;
        try {
            adapter.create(ServerStatusRequest.class).getServerStatus();
            onAuthSuccess(profile, LoginType.OUTDOOR);
        } catch (RetrofitError e) {
            if (!TextUtils.isEmpty(profile.login) && !TextUtils.isEmpty(profile.password)) {
                final RestAdapter newAdaptor = prepareRestAdaptor(profile, true);
                mAuthTriesCounter = 0; //we should try to login with find.z-wave 3 times
                authWithUserCredentials(newAdaptor, profile);
            } else {
                if (mAuthTriesCounter >= Constants.AUTH_TRIES_COUNT) {
                    mAuthTriesCounter = 0;
                    onAuthFail(profile, LoginType.OUTDOOR, e.isNetworkError());
                } else {
                    connectToOutdoorService(adapter, profile);
                }
            }
        }
    }

    private void authWithUserCredentials(RestAdapter adapter, final LocalProfile profile) {
        Timber.v("Auth with find.z-wave, try " + (mAuthTriesCounter + 1));
        if(mCancelEvent) {
            return;
        }

        mAuthTriesCounter++;
        try {
            adapter.create(AuthRequest.class).auth("login", profile.login, profile.password);
            final CookieStore cookieStore = mClient.getCookieStore();
            if (cookieStore == null || cookieStore.getCookies().size() == 0 ||
                    TextUtils.isEmpty(cookieStore.getCookies().get(0).getValue())) {
                if (mAuthTriesCounter >= Constants.AUTH_TRIES_COUNT) {
                    mAuthTriesCounter = 0;
                    onAuthFail(profile, LoginType.WITH_CREDENTIALS, false);
                } else {
                    authWithUserCredentials(adapter, profile);
                }
            } else {
                mCookie = mClient.getCookieStore().getCookies().get(0);
                onAuthSuccess(profile, LoginType.WITH_CREDENTIALS);
            }
        } catch (RetrofitError e) {
            if (e.isNetworkError()) {
                onAuthFail(profile, LoginType.WITH_CREDENTIALS, true);
            } else {
                checkCookie(adapter, profile);
            }
        }
    }

    private void checkCookie(RestAdapter adapter,
                             final LocalProfile profile) {
        final CookieStore cookieStore = mClient.getCookieStore();
        if (cookieStore == null || cookieStore.getCookies().size() == 0 ||
                TextUtils.isEmpty(cookieStore.getCookies().get(0).getValue())) {
            if (mAuthTriesCounter >= Constants.AUTH_TRIES_COUNT) {
                mAuthTriesCounter = 0;
                onAuthFail(profile, LoginType.WITH_CREDENTIALS, false);
            } else {
                authWithUserCredentials(adapter, profile);
            }
        } else {
            mCookie = mClient.getCookieStore().getCookies().get(0);
            onAuthSuccess(profile, LoginType.WITH_CREDENTIALS);
        }
    }

    private void onAuthSuccess(LocalProfile profile, LoginType loginType) {
        Timber.v("Auth success!");
        if(mCancelEvent) {
            return;
        }

        final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getApplicationContext());
        final LocalProfile unselectedProfile = provider.getActiveLocalProfile();
        if (unselectedProfile != null) {
            unselectedProfile.active = false;
            provider.updateLocalProfile(unselectedProfile);
        }

        profile.active = true;
        provider.updateLocalProfile(profile);
        if (loginType == LoginType.OUTDOOR) {
            apiClient.init(profile);
        } else {
            apiClient.init(profile, mCookie);
        }

        dataContext.clear();
        loadProfiles();
        loadLocation();
        bus.post(new AuthEvent.Success(profile, loginType));
        bus.post(new AccountChangedEvent());
    }

    private void onAuthFail(LocalProfile profile, LoginType loginType, boolean isNetworkError) {
        Timber.v("Auth fail");
        bus.post(new AuthEvent.Fail(profile, loginType, isNetworkError));
    }

    private String getServerUrl(LocalProfile profile, boolean useDefaultUrl) {
        String url;
        if (useDefaultUrl) {
            url = Constants.DEFAULT_URL;
        } else {
            url = TextUtils.isEmpty(profile.indoorServer)
                    ? Constants.DEFAULT_URL : profile.indoorServer;
        }
        return url;
    }

    private void loadProfiles(){
        try {
            dataContext.addProfiles(apiClient.getProfiles().data);
        } catch (RetrofitError e){
            e.printStackTrace();
        }
    }

    private void loadLocation(){
        try {
            dataContext.addLocations(apiClient.getLocations().data);
        } catch (RetrofitError e){
            e.printStackTrace();
        }
    }
}
