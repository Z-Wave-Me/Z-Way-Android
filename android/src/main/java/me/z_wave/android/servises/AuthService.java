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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import me.z_wave.android.app.Constants;
import me.z_wave.android.app.ZWayApplication;
import me.z_wave.android.data.DataContext;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.dataModel.Location;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.network.HttpClientHelper;
import me.z_wave.android.network.auth.AuthRequest;
import me.z_wave.android.network.auth.LocalAuth;
import me.z_wave.android.network.auth.LocalAuthRequest;
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
    private static final String EXTRA_LOGIN_REQUEST_DELAY = "me.z_wave.android.servises.extra.REQUEST_DELAY";

    private static final int DEFAULT_AUTH_REQUEST_DELAY = 10000; //10 sec

    private static final String CLOUD_COOKIE = "ZBW_SESSID";
    private static final String ZWAY_COOKIE = "ZWaySession";


    @Inject
    ApiClient apiClient;
    @Inject
    DataContext dataContext;
    @Inject
    MainThreadBus bus;

    private DefaultHttpClient mClient;
    private Cookie mCloudCookie;
    private Cookie mZWayCookie;
    private int mCloudAuthTriesCounter;
    private int mZWayAuthTriesCounter;
    private boolean mCancelEvent;
    private int mDelay = DEFAULT_AUTH_REQUEST_DELAY;

    private Cookie getCookie (String name){
        final CookieStore cookieStore = mClient.getCookieStore();
        if (cookieStore == null)
            return null;
        for (int i = 0; i < cookieStore.getCookies().size(); ++i) {
            if (cookieStore.getCookies().get(i).getName().compareToIgnoreCase(name) == 0 &&
                    !TextUtils.isEmpty(cookieStore.getCookies().get(i).getValue())) {
                return cookieStore.getCookies().get(i);
            }
        }
        return null;
    }


    public static void login(Context context, LocalProfile profile, int requestDelay) {
        Intent intent = new Intent(context, AuthService.class);
        intent.setAction(ACTION_LOGIN);
        intent.putExtra(EXTRA_LOGIN_PROFILE, profile);
        intent.putExtra(EXTRA_LOGIN_REQUEST_DELAY, requestDelay);
        context.startService(intent);
    }

    public static void login(Context context, LocalProfile profile) {
        Intent intent = new Intent(context, AuthService.class);
        intent.setAction(ACTION_LOGIN);
        intent.putExtra(EXTRA_LOGIN_PROFILE, profile);
        intent.putExtra(EXTRA_LOGIN_REQUEST_DELAY, DEFAULT_AUTH_REQUEST_DELAY);
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
                mDelay = intent.getIntExtra(EXTRA_LOGIN_REQUEST_DELAY,
                        DEFAULT_AUTH_REQUEST_DELAY);
                handleActionAuth(profile);
            }
        }
    }

    @Subscribe
    public void onCancelEvent(CancelConnectionEvent event) {
        mCancelEvent = true;
    }

    private void handleActionAuth(LocalProfile profile) {

        final boolean useDefaultUrl = TextUtils.isEmpty(profile.indoorServer);
        final RestAdapter adapter = prepareRestAdaptor(profile, useDefaultUrl);
        if (useDefaultUrl) {
            cloudAuth(adapter, profile);
        } else {
            ZWayAuth(adapter, profile);
        }
    }

    public RestAdapter prepareRestAdaptor(LocalProfile profile, boolean useDefaultUrl) {
        if (profile != null) {
            Timber.v("init ApiClient for %s", profile);
            mClient = HttpClientHelper.createHttpsClient(mDelay);
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
        Timber.v("Connect to outdoor...");
        if (mCancelEvent) {
            return;
        }

        try {
            adapter.create(ServerStatusRequest.class).getServerStatus();
            onAuthSuccess(profile, LoginType.OUTDOOR);
        } catch (RetrofitError e) {
            final RestAdapter newAdaptor = prepareRestAdaptor(profile, true);
            if (!TextUtils.isEmpty(profile.login) && !TextUtils.isEmpty(profile.password)) {
                cloudAuth(newAdaptor, profile);
            } else {
                ZWayAuth (newAdaptor, profile);
            }
        }
    }

    private void cloudAuth (RestAdapter adapter, final LocalProfile profile) {

        for (int i = 0; i < Constants.AUTH_TRIES_COUNT; ++i) {

            Timber.v("Auth with find.z-wave, try " + i);
            if (mCancelEvent)
                return;

            try {
                adapter.create(AuthRequest.class).auth("login", profile.login, profile.password);
            } catch (RetrofitError e) {
                if (e.isNetworkError()) {
                    onAuthFail(profile, LoginType.WITH_CREDENTIALS, true);
                }
            }

            if (getCookie(CLOUD_COOKIE) != null) {
                mCloudCookie = getCookie(CLOUD_COOKIE);
                ZWayAuth(adapter, profile);
                return;
            }
        }
        onAuthFail(profile, LoginType.WITH_CREDENTIALS, true);
    }

    private void ZWayAuth(RestAdapter adapter, final LocalProfile profile) {
        for (int i = 0; i < Constants.AUTH_TRIES_COUNT; ++i) {

            Timber.v("Auth with ZBox, try " + i);
            if (mCancelEvent)
                return;

            try {
                adapter.create(LocalAuthRequest.class).auth(new LocalAuth(true, "admin", "admin", false, 1));
            } catch (RetrofitError e) {
                if (e.isNetworkError()) {
                    onAuthFail(profile, LoginType.WITH_CREDENTIALS, true);
                }
            }

            if (getCookie(ZWAY_COOKIE) != null) {
                mZWayCookie = getCookie(ZWAY_COOKIE);
                onAuthSuccess(profile, LoginType.WITH_CREDENTIALS);
                return;
            }
        }
        onAuthFail(profile, LoginType.WITH_CREDENTIALS, true);

    }


    private void onAuthSuccess(LocalProfile profile, LoginType loginType) {
        Timber.v("Auth success!");
        if(mCancelEvent) {
            return;
        }

        final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getApplicationContext());
        final LocalProfile unselectedProfile = provider.getActiveLocalProfile();
        if (unselectedProfile != null && !unselectedProfile.equals(profile)) {
            unselectedProfile.active = false;
            provider.updateLocalProfile(unselectedProfile);
        }

        profile.active = true;
        apiClient.init(profile, mCloudCookie,mZWayCookie);

        dataContext.clear();
        final List<Profile> serverProfiles = loadProfiles();
        final List<Location> locations = loadLocation();

        dataContext.addProfiles(serverProfiles);
        provider.addServerProfiles(serverProfiles, profile.id);
        dataContext.addLocations(locations);

        if((profile.serverId < 0 || !isServerProfileExist(serverProfiles, profile.serverId))
                && serverProfiles.size() > 0) {
            profile.serverId = serverProfiles.get(0).id;
        }

        provider.updateLocalProfile(profile);
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

    private List<Profile> loadProfiles(){
        try {
            return apiClient.getProfiles().data;
        } catch (RetrofitError e){
            e.printStackTrace();
        }
        return new ArrayList<Profile>();
    }

    private List<Location> loadLocation(){
        try {
            return apiClient.getLocations().data;
        } catch (RetrofitError e){
            e.printStackTrace();
        }
        return new ArrayList<Location>();
    }

    private boolean isServerProfileExist(List<Profile> profiles, int serverProfileId) {
        for(Profile profile : profiles) {
            if(profile.id == serverProfileId) {
                return true;
            }
        }
        return false;
    }
}
