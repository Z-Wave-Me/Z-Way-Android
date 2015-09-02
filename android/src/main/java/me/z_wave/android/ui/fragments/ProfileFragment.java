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
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.z_wave.android.R;
import me.z_wave.android.app.Constants;
import me.z_wave.android.data.NewProfileContext;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.dataModel.ServerStatus;
import me.z_wave.android.dataModel.Theme;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.AccountChangedEvent;
import me.z_wave.android.otto.events.AuthEvent;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.otto.events.ProfileUpdatedEvent;
import me.z_wave.android.otto.events.ProgressEvent;
import me.z_wave.android.otto.events.ShowAttentionDialogEvent;
import me.z_wave.android.otto.events.ShowDialogEvent;
import me.z_wave.android.otto.events.ShowReconnectionProgressEvent;
import me.z_wave.android.servises.AuthService;
import me.z_wave.android.ui.dialogs.ChooseThemeDialog;

public class ProfileFragment extends NetworkScanFragment {

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

    @InjectView(R.id.zbox_login)
    EditText zboxLogin;

    @InjectView(R.id.zbox_password)
    EditText zboxPassword;

    @InjectView(R.id.profile_url_hint)
    TextView urlHint;

    @InjectView(R.id.profile_credentials_hint)
    TextView credentialsHint;

    @InjectView(R.id.profile_location_description)
    TextView location;

    @InjectView(R.id.profile_delete)
    View deleteButton;

    @InjectView(R.id.profile_app_theme_name)
    TextView themeName;

    @InjectView(R.id.profile_app_theme_color)
    View themeColor;

    @InjectView(R.id.profile_server_profile)
    View serverProfilesContainer;

    @InjectView(R.id.profile_server_profile_spinner)
    Spinner serverProfilesSpinner;

    @Inject
    ApiClient apiClient;

    @Inject
    NewProfileContext profileContext;

    private boolean mIsCreateMode;
    private boolean mIgnoreAuthEvents = true;

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
        if(profile != null) {
            profileName.setText(profile.name);
            profileUrl.setText(profile.indoorServer);
            profileLogin.setText(profile.login);
            profilePassword.setText(profile.password);
            zboxLogin.setText(profile.zboxLogin);
            zboxPassword.setText(profile.zboxPassword);

            if(profile.theme != null) {
                themeName.setText(profile.theme.getThemeTitle(getActivity()));
                final int color = getResources().getColor(profile.theme.getThemeColorId());
                themeColor.setBackgroundColor(color);

            }

            location.setVisibility(TextUtils.isEmpty(profile.address) ? View.GONE : View.VISIBLE);
            if (!TextUtils.isEmpty(profile.address)) {
                location.setText(profile.address);
            }

            prepareServerProfilesSpinner(profile);
        } else {
            serverProfilesContainer.setVisibility(View.GONE);
        }
    }

    private void prepareServerProfilesSpinner(final LocalProfile profile) {
        DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getActivity());
        final List<Profile> serverProfiles = provider.getServerProfiles(profile.id);
        if(serverProfiles.size() > 0) {
            final ArrayAdapter<Profile> adapter = new ArrayAdapter<Profile>(getActivity(),
                    android.R.layout.simple_spinner_item, serverProfiles);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            serverProfilesSpinner.setAdapter(adapter);

            final int selectedProfilePosition = getSelectedServerProfilePosition(
                    serverProfiles, profile.serverId);
            serverProfilesSpinner.setSelection(selectedProfilePosition);
            serverProfilesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    final Profile serverProfile = adapter.getItem(position);
                    profile.serverId = serverProfile.id;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        } else {
            serverProfilesContainer.setVisibility(View.GONE);
        }
    }

    private int getSelectedServerProfilePosition(List<Profile> profiles, int selectedProfileId) {
        for(int i = 0; i < profiles.size(); i++) {
            if(profiles.get(i).id == selectedProfileId) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getActivity());
        saveEnteredData();
        final LocalProfile profile = profileContext.getProfile();
        if (item.getItemId() == R.id.action_done) {
            if (mIsCreateMode) {
                trackEvent(R.string.category_profiles, R.string.action_add_profile);
                if (TextUtils.isEmpty(profile.name)) {
                    showToast("Profile name can't be empty");
                } else {
                    mIgnoreAuthEvents = false;
                    bus.post(new ShowReconnectionProgressEvent(true, false, profile.name));
                    long profileId = provider.addLocalProfile(profile);
                    profile.id = (int) profileId;
                    AuthService.login(getActivity(), profile);
                }
            } else {
                trackEvent(R.string.category_profiles, R.string.action_edit_profile);
                provider.updateLocalProfile(profile);
                showToast(R.string.profile_changes_are_saved);
                bus.post(new ProfileUpdatedEvent());
                goBack();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onUrlSelected(String url) {
        profileUrl.setText(url);
    }

    @Subscribe
    public void onAuthSuccess(AuthEvent.Success event) {
        if(!mIgnoreAuthEvents) {
            mIgnoreAuthEvents = true;
            bus.post(new ShowReconnectionProgressEvent(false, false, ""));
            goBack();
        }
    }

    @Subscribe
    public void onAuthFail(AuthEvent.Fail event) {
        if(!mIgnoreAuthEvents) {
            mIgnoreAuthEvents = true;
            bus.post(new ShowReconnectionProgressEvent(false, false, ""));
            bus.post(new ShowAttentionDialogEvent("New profile was saved."));
            goBack();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.profile_delete)
    void deleteProfile() {
        trackEvent(R.string.category_profiles, R.string.action_remove_profile);
        final LocalProfile profile = profileContext.getProfile();
        if (!profile.active) {
            final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getActivity());
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

    @OnClick(R.id.scan_network)
    void scanNetwork() {
        startDiscovering();
    }

    @OnClick(R.id.profile_app_theme)
    void chooseAppTheme() {
        final ChooseThemeDialog dialog = new ChooseThemeDialog(){
            @Override
            public void onThemeSelected(Theme theme) {
                final int color = getActivity().getResources().getColor(theme.getThemeColorId());
                themeName.setText(theme.getThemeTitle(getActivity()));
                themeColor.setBackgroundColor(color);

                final LocalProfile profile = profileContext.getProfile();
                profile.theme = theme;
            }
        };
        bus.post(new ShowDialogEvent(dialog));
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
        profile.zboxLogin = zboxLogin.getText().toString();
        profile.zboxPassword = zboxPassword.getText().toString();
    }

    public LocalProfile getProfile() {
        if (getArguments() == null) {
            mIsCreateMode = true;
            return null;
        }

        final int profileId = getArguments().getInt(PROFILE_ID_KEY, DEFAULT_PROFILE_ID);
        final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getActivity());
        return provider.getLocalProfileWithId(profileId);
    }

    public String setDefaultUriPort(String uri) {
        try {
            URI oldUri = new URI(uri);
            if (oldUri.getPort() == -1) {
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
