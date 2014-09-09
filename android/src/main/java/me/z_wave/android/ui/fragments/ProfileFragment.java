/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 22.06.14 23:57.
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

package me.z_wave.android.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.z_wave.android.R;
import me.z_wave.android.app.Constants;
import me.z_wave.android.data.NewProfileContext;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.dataModel.ServerStatus;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.AccountChangedEvent;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.otto.events.ProfileUpdatedEvent;
import me.z_wave.android.otto.events.ProgressEvent;
import me.z_wave.android.otto.events.ShowAttentionDialogEvent;
import me.z_wave.android.otto.events.ShowReconnectionProgressEvent;

public class ProfileFragment extends BaseFragment {

    private static final int DEFAULT_PROFILE_ID = -1;
    private static final int DEFAULT_PORT = 8083;
    public static final String PROFILE_ID_KEY = "profile_id";

    @InjectView(R.id.profile_name)
    EditText profileName;

    @InjectView(R.id.profile_url)
    EditText profileUrl;

    @InjectView(R.id.profile_login)
    EditText profileLogin;

    @InjectView(R.id.profile_password)
    EditText profilePassword;

    @InjectView(R.id.profile_url_hint)
    TextView urlHint;

    @InjectView(R.id.profile_credentials_hint)
    TextView credentialsHint;

    @InjectView(R.id.profile_location_description)
    TextView location;

    @InjectView(R.id.profile_delete)
    View deleteButton;

    @Inject
    ApiClient apiClient;

    @Inject
    NewProfileContext profileContext;

    private boolean mIsCreateMode;

    public static ProfileFragment newInstance(int profileId) {
        final ProfileFragment fragment = new ProfileFragment();
        final Bundle args = new Bundle();
        args.putInt(PROFILE_ID_KEY, profileId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (profileContext.isEmpty()) {
            final LocalProfile profile = getProfile();
            if (profile != null) {
                profileContext.setProfile(profile);
            } else {
                profileContext.createNew();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        deleteButton.setVisibility(getProfile() != null ? View.VISIBLE : View.GONE);
        LocalProfile profile = profileContext.getProfile();
        fillPage(profile);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveEnteredData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        profileContext.reset();
    }

    private void fillPage(LocalProfile profile) {
        profileName.setText(profile.name);
        profileUrl.setText(profile.indoorServer);
        profileLogin.setText(profile.login);
        profilePassword.setText(profile.password);

        location.setVisibility(TextUtils.isEmpty(profile.address) ? View.GONE : View.VISIBLE);
        if (!TextUtils.isEmpty(profile.address)) {
            location.setText(profile.address);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //TODO IVAN_PL refactor this!!!
        final DatabaseDataProvider provider = new DatabaseDataProvider(getActivity());
        final LocalProfile profile = profileContext.getProfile();
        saveEnteredData();
        if (item.getItemId() == R.id.action_done) {
            if(mIsCreateMode) {
                if (TextUtils.isEmpty(profile.name)) {
                    showToast("Profile name can't be empty");
                } else {
                    bus.post(new ShowReconnectionProgressEvent(true, false, profile.name));
                    long profileId = provider.addLocalProfile(profile);
                    profile.id = (int) profileId;
                    if(!TextUtils.isEmpty(profile.indoorServer)) {
                        connectToOutdorService(profile);
                    } else {
                        loginWithUserCredentials(profile);
                    }
                }
            } else {
                provider.updateLocalProfile(profile);
                showToast(R.string.profile_changes_are_saved);
                bus.post(new ProfileUpdatedEvent());
                goBack();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void connectToOutdorService(final LocalProfile profile) {
        final DatabaseDataProvider provider = new DatabaseDataProvider(getActivity());
        apiClient.init(profile);
        apiClient.checkServerStatus(new ApiClient.SimpleApiCallback<ServerStatus>() {
            @Override
            public void onSuccess(ServerStatus response) {
                bus.post(new ShowReconnectionProgressEvent(false, false, ""));
                final LocalProfile unselectedProfile = provider.getActiveLocalProfile();
                if (unselectedProfile != null) {
                    unselectedProfile.active = false;
                    provider.updateLocalProfile(unselectedProfile);
                }

                profile.active = true;
                provider.updateLocalProfile(profile);

                dataContext.clear();
                bus.post(new AccountChangedEvent());
                goBack();
            }

            @Override
            public void onFailure(boolean isNetworkError) {
                if (!TextUtils.isEmpty(profile.login) && !TextUtils.isEmpty(profile.password)) {
                    loginWithUserCredentials(profile);
                } else {
                    apiClient.init(provider.getActiveLocalProfile());
                    bus.post(new ShowReconnectionProgressEvent(false, false, ""));
//                            bus.post(new ShowAttentionDialogEvent("Can't Login!\nPlease check entered data."));
                    bus.post(new ShowAttentionDialogEvent("New profile was saved."));
                    goBack();
                }
            }
        });
    }

    private void loginWithUserCredentials(final LocalProfile profile) {
        final DatabaseDataProvider provider = new DatabaseDataProvider(getActivity());
            apiClient.init(profile, true);
            apiClient.auth(new ApiClient.OnAuthCompleteListener() {
                @Override
                public void onAuthComplete() {
                    bus.post(new ShowReconnectionProgressEvent(false, false, ""));
                    final LocalProfile unselectedProfile = provider.getActiveLocalProfile();
                    if (unselectedProfile != null) {
                        unselectedProfile.active = false;
                        provider.updateLocalProfile(unselectedProfile);
                    }

                    profile.active = true;
                    provider.updateLocalProfile(profile);

                    dataContext.clear();
                    bus.post(new AccountChangedEvent());
                    goBack();
                }

                @Override
                public void onAuthFiled() {
                    apiClient.init(provider.getActiveLocalProfile());
                    bus.post(new ShowReconnectionProgressEvent(false, false, ""));
//                                bus.post(new ShowAttentionDialogEvent("Can't Login!\nPlease check entered data."));
                    bus.post(new ShowAttentionDialogEvent("New profile was saved."));
                    goBack();
                }
            });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.profile_delete)
    void deleteProfile() {
        final LocalProfile profile = profileContext.getProfile();
        if (!profile.active) {
            final DatabaseDataProvider provider = new DatabaseDataProvider(getActivity());
            showToast("Profile " + profile.name + " deleted");
            provider.removeLocalProfile(profile);
            goBack();
        } else {
            bus.post(new ShowAttentionDialogEvent("You can't delete active profile!"));
        }
    }

    @OnClick(R.id.profile_location)
    void changeLocation() {
        bus.post(new CommitFragmentEvent(new ChooseLocationFragment(), true));
        showToast("change location");
    }

    private String getUrl() {
        String url = profileUrl.getText().toString();
        if (url.endsWith("/"))
            url = url.substring(0, url.length() - 1);
        if (!TextUtils.isEmpty(url)) {
            if (Patterns.WEB_URL.matcher(url).matches()) {
                if (!URLUtil.isHttpsUrl(url) && !URLUtil.isHttpUrl(url)) {
                    url = "http://" + url;
                }
            }
            return setDefaultUriPort(url);
        }
        return null;
    }

    private void saveEnteredData() {
        final LocalProfile profile = profileContext.getProfile();
        profile.name = profileName.getText().toString();
        profile.indoorServer = getUrl();
        profile.login = profileLogin.getText().toString();
        profile.password = profilePassword.getText().toString();
    }

    public LocalProfile getProfile() {
        if (getArguments() == null) {
            mIsCreateMode = true;
            return null;
        }

        final int profileId = getArguments().getInt(PROFILE_ID_KEY, DEFAULT_PROFILE_ID);
        final DatabaseDataProvider provider = new DatabaseDataProvider(getActivity());
        return provider.getLocalProfileWithId(profileId);
    }

    public String setDefaultUriPort(String uri){
        try {
            URI oldUri = new URI(uri);
            if(oldUri.getPort() == -1) {
                URI newUri = new URI(oldUri.getScheme(), oldUri.getUserInfo(),
                        oldUri.getHost(), DEFAULT_PORT, oldUri.getPath(),
                        oldUri.getQuery(), oldUri.getFragment());
                return newUri.toString();
            }
            return uri;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

}
