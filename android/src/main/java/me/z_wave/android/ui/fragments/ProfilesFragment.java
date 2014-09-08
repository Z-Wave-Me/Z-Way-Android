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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.dataModel.ServerStatus;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.AccountChangedEvent;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.otto.events.ProgressEvent;
import me.z_wave.android.otto.events.ShowAttentionDialogEvent;
import me.z_wave.android.otto.events.ShowReconnectionProgressEvent;
import me.z_wave.android.ui.adapters.ProfilesListAdapter;

public class ProfilesFragment extends BaseFragment implements AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener {

    @InjectView(R.id.profiles_list)
    ListView profilesList;

    @Inject
    ApiClient apiClient;

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActionBar().show();
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final LocalProfile selectedProfile = mAdapter.getItem(position);
        if(!selectedProfile.active) {
            bus.post(new ShowReconnectionProgressEvent(true, false, selectedProfile.name));
            if(!TextUtils.isEmpty(selectedProfile.indoorServer)) {
                checkServerState(selectedProfile);
            } else {
                authenticate(selectedProfile);
            }
        }
    }

    private void prepareProfilesList(){
        final DatabaseDataProvider provider = new DatabaseDataProvider(getActivity());
        mAdapter = new ProfilesListAdapter(getActivity(), provider.getLocalProfiles(), false);
        profilesList.addFooterView(createListFooter(), null, false);
        profilesList.setOnItemClickListener(this);
        profilesList.setAdapter(mAdapter);
    }

    private View createListFooter(){
        final View footerView = View.inflate(getActivity(), R.layout.layout_profile_footer, null);
        final Switch switcher = (Switch) footerView.findViewById(R.id.profile_auto_switch_location);
        switcher.setOnCheckedChangeListener(this);
        return footerView;
    }

    private void authenticate(final LocalProfile selectedProfile) {
        apiClient.init(selectedProfile, true);
        apiClient.auth(new ApiClient.OnAuthCompleteListener() {
            @Override
            public void onAuthComplete() {
                bus.post(new ShowReconnectionProgressEvent(false, false, ""));
                selectedProfile.active = true;
                final DatabaseDataProvider provider = new DatabaseDataProvider(getActivity());
                final LocalProfile unselectedProfile = provider.getActiveLocalProfile();
                provider.updateLocalProfile(selectedProfile);
                if(unselectedProfile != null) {
                    unselectedProfile.active = false;
                    provider.updateLocalProfile(unselectedProfile);
                }
                dataContext.clear();

                mAdapter.clear();
                mAdapter.addAll(provider.getLocalProfiles());
                mAdapter.notifyDataSetChanged();
                bus.post(new AccountChangedEvent());
            }

            @Override
            public void onAuthFiled() {
                final DatabaseDataProvider provider = new DatabaseDataProvider(getActivity());
                apiClient.init(provider.getActiveLocalProfile());
                bus.post(new ShowReconnectionProgressEvent(false, false, ""));
                bus.post(new ShowAttentionDialogEvent("Can't connect!"));
            }
        });
    }

    private void checkServerState(final LocalProfile selectedProfile){
        apiClient.init(selectedProfile);
        apiClient.checkServerStatus(new ApiClient.SimpleApiCallback<ServerStatus>() {
            @Override
            public void onSuccess(ServerStatus response) {
                bus.post(new ShowReconnectionProgressEvent(false, false, ""));
                selectedProfile.active = true;
                final DatabaseDataProvider provider = new DatabaseDataProvider(getActivity());
                final LocalProfile unselectedProfile = provider.getActiveLocalProfile();
                provider.updateLocalProfile(selectedProfile);
                if(unselectedProfile != null) {
                    unselectedProfile.active = false;
                    provider.updateLocalProfile(unselectedProfile);
                }
                dataContext.clear();

                mAdapter.clear();
                mAdapter.addAll(provider.getLocalProfiles());
                mAdapter.notifyDataSetChanged();
                bus.post(new AccountChangedEvent());
            }

            @Override
            public void onFailure(boolean isNetworkError) {
                if(!TextUtils.isEmpty(selectedProfile.login)
                        || !TextUtils.isEmpty(selectedProfile.password)) {
                    authenticate(selectedProfile);
                } else {
                    final DatabaseDataProvider provider = new DatabaseDataProvider(getActivity());
                    apiClient.init(provider.getActiveLocalProfile());
                    bus.post(new ShowReconnectionProgressEvent(false, false, ""));
                    bus.post(new ShowAttentionDialogEvent("Can't connect!"));
                }

            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }
}
