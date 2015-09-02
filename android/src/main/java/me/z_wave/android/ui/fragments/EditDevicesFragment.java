/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 07.09.14 21:28.
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

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.Filter;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.ProgressEvent;
import me.z_wave.android.ui.adapters.EditDevicesListAdapter;

/**
 * Created by Ivan PL on 07.09.2014.
 */
public class EditDevicesFragment extends BaseListFragment{

    public static final String FILTER_KEY = "filter_key";
    public static final String FILTER_NAME_KEY = "filter_name_key";

    private EditDevicesListAdapter mAdapter;

    @Inject
    ApiClient apiClient;

    public static EditDevicesFragment newInstance(Filter filter, String filterValue){
        final EditDevicesFragment devicesFragment = new EditDevicesFragment();
        final Bundle args = new Bundle();
        args.putInt(FILTER_KEY, filter.ordinal());
        args.putString(FILTER_NAME_KEY, filterValue);
        devicesFragment.setArguments(args);
        return devicesFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_devices, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareDevicesView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_dashboard, menu);
        getActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_dashboard_done:
                final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getActivity());
                final LocalProfile localProfile = provider.getActiveLocalProfile();
                final Profile profile = provider.getServerProfileWithId(localProfile.serverId);
                //TODO hotfix, need find the reason of nullpointer in profile.positions = mAdapter.getDashboardDevicesIds()
                if(profile != null && mAdapter != null) {
                    profile.dashboard = mAdapter.getDashboardDevicesIds();
                    bus.post(new ProgressEvent(true, false));
                    apiClient.updateProfile(profile, new ApiClient.ApiCallback<List<Profile>, String>() {
                        @Override
                        public void onSuccess(List<Profile> result) {
                            bus.post(new ProgressEvent(false, false));
                            dataContext.addProfiles(result);
                            goBack();
                        }

                        @Override
                        public void onFailure(String request, boolean isNetworkError) {
                            bus.post(new ProgressEvent(false, false));
                            //TODO IVAN_PL error show
                            goBack();
                        }
                    });
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void prepareDevicesView(){
        final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getActivity());
        final LocalProfile localProfile = provider.getActiveLocalProfile();
        final Profile profile = provider.getServerProfileWithId(localProfile.serverId);
        mAdapter = new EditDevicesListAdapter(getActivity(), getFilteredDeviceList(), profile);
        setListAdapter(mAdapter);
    }

    private List<Device> getFilteredDeviceList(){
        final Filter filter = Filter.values()[getArguments().getInt(FILTER_KEY, 0)];
        final String filterValue = getArguments().getString(FILTER_NAME_KEY, Filter.DEFAULT_FILTER);
        switch (filter){
            case LOCATION:
                return dataContext.getDevicesForLocation(filterValue);
            case TYPE:
                return dataContext.getDevicesWithType(filterValue);
            case TAG:
                return dataContext.getDevicesWithTag(filterValue);
        }
        return new ArrayList<Device>();
    }
}
