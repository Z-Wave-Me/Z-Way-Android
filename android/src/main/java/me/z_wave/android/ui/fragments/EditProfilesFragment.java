/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 22.06.14 22:15.
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
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.ui.adapters.ProfilesListAdapter;

public class EditProfilesFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    @InjectView(R.id.profiles_list)
    ListView profilesList;

    private ProfilesListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profiles, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareProfilesList();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Profile profile = mAdapter.getItem(position);
        bus.post(new CommitFragmentEvent(ProfileFragment.newInstance(profile.id), true));
    }

    private void prepareProfilesList(){
        //TODO remove hardcode
        mAdapter = new ProfilesListAdapter(getActivity(),
                dataContext.getProfiles() == null ? new ArrayList<Profile>() : dataContext.getProfiles(), true);
        profilesList.setOnItemClickListener(this);
        profilesList.setAdapter(mAdapter);
    }
}
