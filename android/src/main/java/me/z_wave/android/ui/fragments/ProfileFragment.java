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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.Profile;

public class ProfileFragment extends BaseFragment{

    private static final int DEFAULT_PROFILE_ID = -1;
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

    @InjectView(R.id.profile_delete)
    View deleteButton;


    private Profile mProfile;

    public static ProfileFragment newInstance(int profileId){
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
        mProfile = getProfile();

        profileName.setText(mProfile.name);
        if(mProfile.id == DEFAULT_PROFILE_ID){
            deleteButton.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.profile_delete)
    void deleteProfile(){
        showToast("Delete profile");
    }

    @OnClick(R.id.profile_location)
    void changeLocation(){
        showToast("change location");
    }

    public Profile getProfile(){
        Profile profile = null;
        if(getArguments() != null){
            final int profileId = getArguments().getInt(PROFILE_ID_KEY, DEFAULT_PROFILE_ID);
            profile = dataContext.getProfileWithId(profileId);
        }
        if(profile == null){
            profile = new Profile();
            profile.id = DEFAULT_PROFILE_ID;
        }
        return profile;
    }

}
