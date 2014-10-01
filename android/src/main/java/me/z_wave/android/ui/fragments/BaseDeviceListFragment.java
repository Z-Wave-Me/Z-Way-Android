/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 22.09.14 21:18.
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
import android.text.TextUtils;
import android.webkit.URLUtil;

import java.util.List;

import javax.inject.Inject;

import me.z_wave.android.R;
import me.z_wave.android.app.Constants;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.Filter;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.ShowAttentionDialogEvent;
import me.z_wave.android.otto.events.StartActivityEvent;
import me.z_wave.android.ui.activity.CameraActivity;
import me.z_wave.android.ui.adapters.DevicesGridAdapter;

/**
 * Created by Ivan PL on 22.09.2014.
 */
public class BaseDeviceListFragment extends BaseFragment
        implements DevicesGridAdapter.DeviceStateUpdatedListener {

    @Inject
    ApiClient apiClient;

    protected List<Device> mDevices;

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
        apiClient.updateToggle(updatedDevice, new ApiClient.EmptyApiCallback<Device>() {
            @Override
            public void onSuccess() {
                showToast("Toggle clicked");
            }

            @Override
            public void onFailure(Device request, boolean isNetworkError) {
                if (isAdded()) {
                    if (isNetworkError) {
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

    @Override
    public void onOpenCameraView(Device updatedDevice) {
        if(!TextUtils.isEmpty(updatedDevice.metrics.url)
                && URLUtil.isValidUrl(updatedDevice.metrics.url)) {
            final Intent intent = new Intent(getActivity(), CameraActivity.class);
            intent.putExtra(CameraActivity.KEY_DEVICE, updatedDevice);
            bus.post(new StartActivityEvent(intent));
        } else {
            bus.post(new ShowAttentionDialogEvent(getString(R.string.invalid_camera_url)));
        }
    }

    protected void updateDevicesList(List<Device> devices) {
        for(Device device : devices) {
            updateDevice(device);
        }
    }

    protected void updateDevice(Device device) {
        final int devicePosition = mDevices.indexOf(device);
        if(devicePosition >= 0) {
            mDevices.remove(device);
            mDevices.add(devicePosition, device);
        } else {
            mDevices.add(device);
        }
    }

}
