/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 25.05.14 22:33.
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.ui.adapters.ProfilesListAdapter;

public class ProfilesFragment extends BaseFragment {

    @InjectView(R.id.profiles_list)
    ListView profilesList;

    private ProfilesListAdapter mAdapter;
    private ApiClient mApiClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profiles, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mApiClient = new ApiClient(getActivity());
        if(dataContext.getProfiles().size() != 0){
            prepareProfilesList();
        } else {
            requestProfiles();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_profiles, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.profile_add:
                bus.post(new CommitFragmentEvent(new ProfileFragment(), true));
                break;
            case R.id.profile_edit:
                bus.post(new CommitFragmentEvent(new EditProfilesFragment(), true));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void prepareProfilesList(){
        mAdapter = new ProfilesListAdapter(getActivity(), dataContext.getProfiles(), false);
        profilesList.addFooterView(createListFooter(), null, false);
        profilesList.setAdapter(mAdapter);
    }

    private void requestProfiles(){
        mApiClient.getProfiles(new ApiClient.ApiCallback<List<Profile>, String>() {
            @Override
            public void onSuccess(List<Profile> result) {
                dataContext.addProfiles(result);
                prepareProfilesList();
            }

            @Override
            public void onFailure(String request, boolean isNetworkError) {
                if(isAdded()){
                    if(isNetworkError){
                        showToast(R.string.request_network_problem);
                    } else {
                        showToast(R.string.request_server_problem_msg);
                    }
                }
            }
        });
    }

    private View createListFooter(){
        return View.inflate(getActivity(), R.layout.layout_profile_footer, null);
    }
}
