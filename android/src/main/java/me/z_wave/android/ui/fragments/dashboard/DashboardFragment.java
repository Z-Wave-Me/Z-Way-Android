/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 25.05.14 22:30.
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

package me.z_wave.android.ui.fragments.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.otto.events.OnDataUpdatedEvent;
import me.z_wave.android.ui.adapters.DevicesGridAdapter;
import me.z_wave.android.ui.fragments.BaseDeviceListFragment;
import timber.log.Timber;

public class DashboardFragment extends BaseDeviceListFragment {

    @InjectView(R.id.dashboard_widgets)
    GridView widgetsGridView;

    @InjectView(R.id.dashboard_msg_empty)
    View emptyListMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareDevicesView();
        changeEmptyDashboardMsgVisibility();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_dashboard, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dashboard_edit:
                bus.post(new CommitFragmentEvent(new EditDashboardFragment(), true));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void updateDevicesList(List<Device> devices) {
        final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getActivity());
        final LocalProfile localProfile = provider.getActiveLocalProfile();
        final Profile profile = provider.getServerProfileWithId(localProfile.serverId);
        if(profile != null) {
            for (Device device : devices) {
                if (profile.dashboard.contains(device.id)) {
                    adapter.update(device);
                } else {
                    adapter.remove(device);
                }
            }
        }
    }

    @Override
    protected void onAfterDelayListUpdate() {
        final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getActivity());
        final LocalProfile localProfile = provider.getActiveLocalProfile();
        final Profile profile = provider.getServerProfileWithId(localProfile.serverId);
        final List<Device> device = dataContext.getDashboardDevices(profile);
        updateDevicesList(device);
        adapter.notifyDataSetChanged();
        changeEmptyDashboardMsgVisibility();
    }

    @Subscribe
    public void onDataUpdated(OnDataUpdatedEvent event) {
        if(isCanUpdate) {
            Timber.v("Dashboard list updated!");
            final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getActivity());
            final LocalProfile localProfile = provider.getActiveLocalProfile();
            if(localProfile != null) {
                final Profile profile = provider.getServerProfileWithId(localProfile.serverId);
                adapter.setProfile(profile);
                updateDevicesList(event.devices);
                adapter.notifyDataSetChanged();
                changeEmptyDashboardMsgVisibility();
            }
        }
    }

    private void prepareDevicesView() {
        final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getActivity());
        final LocalProfile localProfile = provider.getActiveLocalProfile();
        final Profile profile = provider.getServerProfileWithId(localProfile.serverId);
        final List<Device> device = dataContext.getDashboardDevices(profile);
        if(device != null) {
            adapter.addAll(device);
        }
        widgetsGridView.setAdapter(adapter);
    }

    private void changeEmptyDashboardMsgVisibility() {
        final int msgVisibility = adapter.isEmpty() ? View.VISIBLE : View.GONE;
        if (emptyListMsg.getVisibility() != msgVisibility) {
            emptyListMsg.setVisibility(msgVisibility);
        }
    }

}
