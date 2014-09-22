/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 08.07.14 10:24.
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

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.dataModel.ServerStatus;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.AccountChangedEvent;
import me.z_wave.android.otto.events.CommitFragmentEvent;

/**
 * Created by Ivan PL on 08.07.2014.
 */
public class SplashFragment extends BaseFragment {

    private static final long SPLASH_DISPLAY_LENGTH = 3000;

    @Inject
    ApiClient apiClient;
    private LocalProfile mLocalProfile;

    private boolean mIsAuthFiled = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_splash, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final DatabaseDataProvider provider = new DatabaseDataProvider(getActivity());
        mLocalProfile = provider.getActiveLocalProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mIsAuthFiled) {
            bus.post(new CommitFragmentEvent(new ProfilesFragment(), false));
        } else {
            tryConnectToTheServer();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActionBar().hide();
    }

    private void tryConnectToTheServer() {
        if(mLocalProfile == null){
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    bus.post(new CommitFragmentEvent(new ProfilesFragment(), false));
                }
            }, SPLASH_DISPLAY_LENGTH);
        } else {
            apiClient.init(mLocalProfile);
            if(!TextUtils.isEmpty(mLocalProfile.indoorServer)) {
                checkServerState();
            } else {
                authenticate();
            }
        }
    }

    private void authenticate() {
        apiClient.auth(new ApiClient.OnAuthCompleteListener() {
            @Override
            public void onAuthComplete() {
                bus.post(new AccountChangedEvent());
            }

            @Override
            public void onAuthFiled() {
                onCantConnect();
            }
        });
    }

    private void checkServerState(){
        apiClient.checkServerStatus(new ApiClient.SimpleApiCallback<ServerStatus>() {
            @Override
            public void onSuccess(ServerStatus response) {
                bus.post(new AccountChangedEvent());
            }

            @Override
            public void onFailure(boolean isNetworkError) {
                if(isAdded()) {
                    if(!TextUtils.isEmpty(mLocalProfile.login) || !TextUtils.isEmpty(mLocalProfile.password)) {
                        authenticate();
                    } else {
                        onCantConnect();
                    }
                }
            }
        });
    }

    private void onCantConnect() {
        mIsAuthFiled = true;
        unselectActiveProfile();
        bus.post(new CommitFragmentEvent(new ProfilesFragment(), false));
    }

    private void unselectActiveProfile() {
        final DatabaseDataProvider provider = new DatabaseDataProvider(getActivity());
        final LocalProfile unselectedProfile = provider.getActiveLocalProfile();
        if(unselectedProfile != null) {
            unselectedProfile.active = false;
            provider.updateLocalProfile(unselectedProfile);
        }
    }
}
