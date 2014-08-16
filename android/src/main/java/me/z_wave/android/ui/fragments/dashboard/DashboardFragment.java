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
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import me.z_wave.android.R;
import me.z_wave.android.data.DataContext;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.otto.events.OnDataUpdatedEvent;
import me.z_wave.android.ui.adapters.DevicesGridAdapter;
import me.z_wave.android.ui.fragments.BaseFragment;
import me.z_wave.android.ui.fragments.EditProfilesFragment;
import me.z_wave.android.ui.fragments.ProfileFragment;
import me.z_wave.android.ui.views.SwipeGridView;
import timber.log.Timber;

public class DashboardFragment extends BaseFragment implements
        DevicesGridAdapter.DeviceStateUpdatedListener {

    @InjectView(R.id.dashboard_widgets)
    GridView widgetsGridView;

    @InjectView(R.id.dashboard_msg_empty)
    View emptyListMsg;

    @Inject
    ApiClient apiClient;

    private DevicesGridAdapter mAdapter;

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
        switch (item.getItemId()){
            case R.id.dashboard_edit:
                bus.post(new CommitFragmentEvent(new EditDashboardFragment(), true));
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSwitchStateChanged(Device updatedDevice) {
        apiClient.updateDevicesState(updatedDevice, new ApiClient.EmptyApiCallback<Device>() {
            @Override
            public void onSuccess() {
                showToast("Device state changed!");
            }

            @Override
            public void onFailure(Device request, boolean isNetworkError) {
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

    @Override
    public void onSeekBarStateChanged(final Device updatedDevice) {
        apiClient.updateDevicesLevel(updatedDevice, new ApiClient.EmptyApiCallback<Device>() {
            @Override
            public void onSuccess() {
                showToast("Seek changed " + updatedDevice.metrics.level);
            }

            @Override
            public void onFailure(Device request, boolean isNetworkError) {
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

    @Override
    public void onToggleClicked(Device updatedDevice) {
        apiClient.updateTogle(updatedDevice, new ApiClient.EmptyApiCallback<Device>() {
            @Override
            public void onSuccess() {
                showToast("Toggle clicked");
            }

            @Override
            public void onFailure(Device request, boolean isNetworkError) {
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

    @Override
    public void onColorViewClicked(Device updatedDevice) {
        showToast("rgb clicked");
    }

//    @Override
//    public void onAddRemoveClicked(Device updatedDevice) {
//        final Profile profile = dataContext.getActiveProfile();
//        if(profile != null){
//            widgetsGridView.closeOpenedItems();
//            mAdapter.remove(updatedDevice);
//            profile.positions.remove(updatedDevice.id);
//            mAdapter.notifyDataSetChanged();
//            apiClient.updateProfiles(profile, new ApiClient.ApiCallback<List<Profile>, String>() {
//                @Override
//                public void onSuccess(List<Profile> result) {
//
//                }
//
//                @Override
//                public void onFailure(String request, boolean isNetworkError) {
//
//                }
//            });
//        }
//    }

    @Subscribe
    public void onDataUpdated(OnDataUpdatedEvent event){
        Timber.v("Dashboard list updated!");
        mAdapter.setProfile(dataContext.getActiveProfile());
        mAdapter.clear();
        mAdapter.addAll(dataContext.getDashboardDevices());
        mAdapter.notifyDataSetChanged();
        changeEmptyDashboardMsgVisibility();
    }

    private void prepareDevicesView(){
        mAdapter = new DevicesGridAdapter(getActivity(), dataContext.getDashboardDevices(),
                dataContext.getActiveProfile(), this);
        widgetsGridView.setAdapter(mAdapter);
    }

    private void changeEmptyDashboardMsgVisibility(){
        final int msgVisibility = mAdapter.isEmpty() ? View.VISIBLE : View.GONE;
        if(emptyListMsg.getVisibility() != msgVisibility){
            emptyListMsg.setVisibility(msgVisibility);
        }
    }

    private List<Device> getDashboardDevices(){
        return dataContext.getDashboardDevices();
    }
}
