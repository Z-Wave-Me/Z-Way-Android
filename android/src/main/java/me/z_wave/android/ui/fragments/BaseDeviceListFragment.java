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
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.URLUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.otto.events.ShowAttentionDialogEvent;
import me.z_wave.android.otto.events.ShowDialogEvent;
import me.z_wave.android.otto.events.StartActivityEvent;
import me.z_wave.android.servises.UpdateDeviceService;
import me.z_wave.android.ui.activity.CameraActivity;
import me.z_wave.android.ui.adapters.DevicesGridAdapter;
import me.z_wave.android.ui.dialogs.ColorPickerDialog;
import me.z_wave.android.utils.CameraUtils;

/**
 * Created by Ivan PL on 22.09.2014.
 */
public class BaseDeviceListFragment extends BaseFragment
        implements DevicesGridAdapter.DeviceStateUpdatedListener {

    public static final int LIST_UPDATE_DELAY = 3000;

    public DevicesGridAdapter adapter;

    public Timer updateDelayTimer;
    public boolean isCanUpdate = true;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getActivity());
        final LocalProfile localProfile = provider.getActiveLocalProfile();
        if(localProfile != null) {
            final Profile serverProfile = provider.getServerProfileWithId(localProfile.serverId);
            adapter = new DevicesGridAdapter(getActivity(), new ArrayList<Device>(),
                    serverProfile, this);
        }
    }

    @Override
    public void onSwitchStateChanged(Device updatedDevice) {
        UpdateDeviceService.updateDeviceState(getActivity(), updatedDevice);
        startUpdaytDelay();
    }

    @Override
    public void onSeekBarStateChanged(final Device updatedDevice) {
        UpdateDeviceService.updateDeviceLevel(getActivity(), updatedDevice);
        startUpdaytDelay();
    }

    @Override
    public void onToggleClicked(Device updatedDevice) {
        UpdateDeviceService.updateDeviceToggle(getActivity(), updatedDevice);
        startUpdaytDelay();
    }

    @Override
    public void onColorViewClicked(final Device device) {
        final ColorPickerDialog dialog = new ColorPickerDialog() {
            @Override
            public void onColorPicked(int color) {
                device.metrics.color.r = Color.red(color);
                device.metrics.color.g = Color.green(color);
                device.metrics.color.b = Color.blue(color);
                UpdateDeviceService.updateRgbColor(getActivity(), device);
                startUpdaytDelay();
            }
        };
        dialog.setOldColor(device.metrics.color.getColorAsInt());
        bus.post(new ShowDialogEvent(dialog));
    }

    @Override
    public void onOpenCameraView(Device updatedDevice) {
        final LocalProfile profile = DatabaseDataProvider.getInstance(getActivity()).getActiveLocalProfile();
        final String cameraUrl = CameraUtils.getCameraUrl(profile, updatedDevice.metrics.url);
        if(!TextUtils.isEmpty(cameraUrl)
                && URLUtil.isValidUrl(cameraUrl)) {
            final Intent intent = new Intent(getActivity(), CameraActivity.class);
            intent.putExtra(CameraActivity.KEY_DEVICE, updatedDevice);
            bus.post(new StartActivityEvent(intent));
        } else {
            bus.post(new ShowAttentionDialogEvent(getString(R.string.invalid_camera_url)));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isCanUpdate = true;
        if(updateDelayTimer != null) {
            updateDelayTimer.cancel();
            updateDelayTimer = null;
        }
    }

    public void startUpdaytDelay() {
        if(updateDelayTimer != null) {
            updateDelayTimer.cancel();
        }

        updateDelayTimer = new Timer();
        updateDelayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isCanUpdate = true;
                if(isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onAfterDelayListUpdate();
                        }
                    });
                }
            }
        }, LIST_UPDATE_DELAY);
        isCanUpdate = false;
    }

    protected void updateDevicesList(List<Device> devices) {

    }

    protected void onAfterDelayListUpdate() {

    }

}
