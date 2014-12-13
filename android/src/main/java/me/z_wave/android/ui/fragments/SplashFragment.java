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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.otto.events.AuthEvent;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.otto.events.StartActivityEvent;
import me.z_wave.android.servises.AuthService;
import me.z_wave.android.ui.activity.ProfilesActivity;

/**
 * Created by Ivan PL on 08.07.2014.
 */
public class SplashFragment extends BaseFragment {

    private static final long SPLASH_DISPLAY_LENGTH = 3000;
    private static final int SPLASH_LOGIN_REQUEST_DELAY = 3000;

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
        final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getActivity());
        mLocalProfile = provider.getActiveLocalProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsAuthFiled) {
            final Intent intent = new Intent(getActivity(), ProfilesActivity.class);
            bus.post(new StartActivityEvent(intent));
        } else {
            tryToConnect();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActionBar().hide();
    }

    @Subscribe
    public void onAuthFail(AuthEvent.Fail event) {
        onCantConnect();
    }

    private void tryToConnect() {
        if (mLocalProfile == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Intent intent = new Intent(getActivity(), ProfilesActivity.class);
                    bus.post(new StartActivityEvent(intent));
                }
            }, SPLASH_DISPLAY_LENGTH);
        } else {
            AuthService.login(getActivity(), mLocalProfile, SPLASH_LOGIN_REQUEST_DELAY);
        }
    }

    private void onCantConnect() {
        mIsAuthFiled = true;
        unselectActiveProfile();
        final Intent intent = new Intent(getActivity(), ProfilesActivity.class);
        bus.post(new StartActivityEvent(intent));
    }

    private void unselectActiveProfile() {
        final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getActivity());
        final LocalProfile unselectedProfile = provider.getActiveLocalProfile();
        if (unselectedProfile != null) {
            unselectedProfile.active = false;
            provider.updateLocalProfile(unselectedProfile);
        }
    }
}
