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

package me.z_wave.android.gui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.DevicesStatus;
import me.z_wave.android.gui.adapters.DevicesGridAdapter;
import me.z_wave.android.network.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardFragment extends BaseFragment implements
        DevicesGridAdapter.DeviceStateUpdatedListener {

    private Timer mTimer;
    private DevicesGridAdapter mAdapter;

    private List<Device> mDeviceList;
    private long mLastUpdateTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDeviceList = new ArrayList<Device>();
        prepareDevicesView();
    }

    @Override
    public void onResume() {
        super.onResume();
        startDevicesUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimer.cancel();

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

    private void startDevicesUpdates(){
        mTimer =  new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                ApiClient.getDevicesState(mLastUpdateTime, new ApiClient.ApiCallback<DevicesStatus, Long>() {

                    @Override
                    public void onSuccess(DevicesStatus result) {
                        if(isAdded()){
                            mLastUpdateTime = result.updateTime;
                            if(result.devices != null && !result.devices.isEmpty()){
                                updateDevicesList(result.devices);
                                mAdapter.notifyDataSetChanged();
                            }
                            changeEmptyDashboardMsgVisibility();
                        }
                    }

                    @Override
                    public void onFailure(Long request, boolean isNetworkError) {
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
        }, 0, 5000);
    }

    private void updateDevicesList(List<Device> updatedDeviceList){
        if(mDeviceList == null || mDeviceList.isEmpty()){
            mDeviceList.addAll(updatedDeviceList);
        } else {
            for(Device updatedDevice : updatedDeviceList){
                final int position = mDeviceList.indexOf(updatedDevice);
                if(position >= 0){
                    mDeviceList.remove(position);
                    mDeviceList.add(position, updatedDevice);
                }
            }
        }
    }

    private void prepareDevicesView(){
        mAdapter = new DevicesGridAdapter(getActivity(), mDeviceList, this);
        final GridView gridView = (GridView) getView().findViewById(R.id.dashboard_widgets);
        gridView.setAdapter(mAdapter);
    }


    private void changeEmptyDashboardMsgVisibility(){
        final View emptyDashboardMsgView = getView().findViewById(R.id.dashboard_msg_empty);
        final int msgVisibility = mDeviceList == null || mDeviceList.isEmpty() ? View.VISIBLE : View.GONE;
        if(emptyDashboardMsgView.getVisibility() != msgVisibility){
            emptyDashboardMsgView.setVisibility(msgVisibility);
        }
    }
}
