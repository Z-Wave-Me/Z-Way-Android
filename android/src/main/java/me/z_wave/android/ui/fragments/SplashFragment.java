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
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.otto.events.StartActivityEvent;
import me.z_wave.android.ui.activity.MainActivity;

/**
 * Created by Ivan PL on 08.07.2014.
 */
public class SplashFragment extends BaseFragment {

    private static final long SPLASH_DISPLAY_LENGTH = 3000;
    @Inject
    ApiClient apiClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_splash, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DatabaseDataProvider provider = new DatabaseDataProvider(getActivity());
        LocalProfile localProfile = provider.getActiveLocalProfile();
        if(localProfile == null){
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    bus.post(new CommitFragmentEvent(new ProfilesFragment(), false));
                }
            }, SPLASH_DISPLAY_LENGTH);
        } else {
            apiClient.init(localProfile);
            apiClient.auth(new ApiClient.OnAuthCompleteListener() {
                @Override
                public void onAuthComplete() {
                    final Intent intent = new Intent(getActivity(), MainActivity.class);
                    bus.post(new StartActivityEvent(intent));
                }

                @Override
                public void onAuthFiled() {
                    bus.post(new CommitFragmentEvent(new ProfilesFragment(), false));
                }
            });
        }
    }

}
