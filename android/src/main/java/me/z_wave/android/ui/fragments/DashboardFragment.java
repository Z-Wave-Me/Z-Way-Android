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

package me.z_wave.android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.OnDataUpdatedEvent;
import me.z_wave.android.ui.adapters.DevicesGridAdapter;

public class DashboardFragment extends BaseFragment implements
        DevicesGridAdapter.DeviceStateUpdatedListener {

    @InjectView(R.id.dashboard_widgets)
    GridView widgetsGridView;

    @InjectView(R.id.dashboard_msg_empty)
    View emptyListMsg;

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
    }

    @Override
    public void onExactChanged(Device updatedDevice) {
        ApiClient.updateDevicesState(updatedDevice, new ApiClient.EmptyApiCallback<Device>() {
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

    @Subscribe
    public void onDataUpdated(OnDataUpdatedEvent event){
        mAdapter.notifyDataSetChanged();
        changeEmptyDashboardMsgVisibility();
    }

    private void prepareDevicesView(){
        mAdapter = new DevicesGridAdapter(getActivity(), dataContext.getDevices(), this);
        widgetsGridView.setAdapter(mAdapter);
    }


    private void changeEmptyDashboardMsgVisibility(){
        final int msgVisibility = dataContext.getDevices() == null || dataContext.getDevices().isEmpty() ? View.VISIBLE : View.GONE;
        if(emptyListMsg.getVisibility() != msgVisibility){
            emptyListMsg.setVisibility(msgVisibility);
        }
    }
}
