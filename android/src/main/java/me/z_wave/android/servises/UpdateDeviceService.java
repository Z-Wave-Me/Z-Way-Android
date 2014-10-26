/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 25.10.14 17:29.
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

package me.z_wave.android.servises;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import javax.inject.Inject;

import me.z_wave.android.app.ZWayApplication;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.network.ApiClient;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UpdateDeviceService extends IntentService {

    private static final String ACTION_UPDATE_SWITCH_STATE = "me.z_wave.android.servises.action.UPDATE_SWITCH_STATE";
    private static final String ACTION_UPDATE_RGB = "me.z_wave.android.servises.action.UPDATE_RGB";
    private static final String ACTION_UPDATE_MODE = "me.z_wave.android.servises.action.UPDATE_MODE";
    private static final String ACTION_UPDATE_LEVEL = "me.z_wave.android.servises.action.UPDATE_LEVEL";
    private static final String ACTION_UPDATE_TOGGLE = "me.z_wave.android.servises.action.UPDATE_TOGGLE";
    private static final String ACTION_ZOOM_IN = "me.z_wave.android.servises.action.UPDATE_ZOOM_IN";
    private static final String ACTION_ZOOM_OUT = "me.z_wave.android.servises.action.UPDATE_ZOOM_OUT";
    private static final String ACTION_MOVE_CAMERA_LEFT = "me.z_wave.android.servises.action.MOVE_CAMERA_LEFT";
    private static final String ACTION_MOVE_CAMERA_RIGHT = "me.z_wave.android.servises.action.MOVE_CAMERA_RIGHT";
    private static final String ACTION_MOVE_CAMERA_UP = "me.z_wave.android.servises.action.MOVE_CAMERA_UP";
    private static final String ACTION_MOVE_CAMERA_DOWN = "me.z_wave.android.servises.action.MOVE_CAMERA_DOWN";
    private static final String ACTION_OPEN_CAMERA = "me.z_wave.android.servises.action.OPEN_CAMERA";
    private static final String ACTION_CLOSE_CAMERA = "me.z_wave.android.servises.action.CLOSE_CAMERA";

    private static final String EXTRA_DEVICE = "me.z_wave.android.servises.extra.EXTRA_DEVICE";

    @Inject
    ApiClient apiClient;

    public static void updateRgbColor(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_UPDATE_RGB);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void updateDeviceState(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_UPDATE_SWITCH_STATE);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void updateDeviceMode(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_UPDATE_MODE);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void updateDeviceLevel(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_UPDATE_LEVEL);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void updateDeviceToggle(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_UPDATE_TOGGLE);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void zoomCameraIn(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_ZOOM_IN);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void zoomCameraOut(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_ZOOM_OUT);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void moveCameraLeft(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_MOVE_CAMERA_LEFT);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void moveCameraRight(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_MOVE_CAMERA_RIGHT);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void moveCameraUp(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_MOVE_CAMERA_UP);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void moveCameraDown(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_MOVE_CAMERA_DOWN);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void openCamera(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_OPEN_CAMERA);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void closeCamera(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_CLOSE_CAMERA);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public UpdateDeviceService() {
        super("UpdateDeviceService");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        ((ZWayApplication) getApplicationContext()).inject(this);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final Device device = (Device) intent.getSerializableExtra(EXTRA_DEVICE);
            final String action = intent.getAction();
            if (ACTION_UPDATE_RGB.equals(action)) {
                updateRgbColor(device);
            } else if(ACTION_UPDATE_SWITCH_STATE.equals(action)) {
                updateSwitchState(device);
            } else if(ACTION_UPDATE_MODE.equals(action)) {
                updateMode(device);
            } else if(ACTION_UPDATE_LEVEL.equals(action)) {
                updateLevel(device);
            } else if(ACTION_UPDATE_TOGGLE.equals(action)) {
                updateToggle(device);
            } else if(ACTION_ZOOM_IN.equals(action)) {
                zoomIn(device);
            } else if(ACTION_ZOOM_OUT.equals(action)) {
                zoomOut(device);
            } else if(ACTION_MOVE_CAMERA_LEFT.equals(action)) {
                moveCameraLeft(device);
            } else if(ACTION_MOVE_CAMERA_RIGHT.equals(action)) {
                moveCameraRight(device);
            } else if(ACTION_MOVE_CAMERA_UP.equals(action)) {
                moveCameraUp(device);
            } else if(ACTION_MOVE_CAMERA_DOWN.equals(action)) {
                moveCameraDown(device);
            } else if(ACTION_OPEN_CAMERA.equals(action)) {
                openCamera(device);
            } else if(ACTION_CLOSE_CAMERA.equals(action)) {
                closeCamera(device);
            }
        }
    }


    private void updateRgbColor(Device device) {
        apiClient.updateRGBColor(device);
    }

    private void updateSwitchState(Device device) {
        apiClient.updateDevicesState(device);
    }

    private void updateMode(Device device) {
        apiClient.updateDevicesMode(device);
    }

    private void updateLevel(Device device) {
        apiClient.updateDevicesLevel(device);
    }

    private void updateToggle(Device device) {
        apiClient.updateToggle(device);
    }

    private void zoomIn(Device device) {
        apiClient.zoomCameraIn(device);
    }

    private void zoomOut(Device device) {
        apiClient.zoomCameraOut(device);
    }

    private void moveCameraLeft(Device device) {
        apiClient.moveCameraLeft(device);
    }

    private void moveCameraRight(Device device) {
        apiClient.moveCameraRight(device);
    }

    private void moveCameraUp(Device device) {
        apiClient.moveCameraUp(device);
    }

    private void moveCameraDown(Device device) {
        apiClient.moveCameraDown(device);
    }

    private void openCamera(Device device) {
        apiClient.openCamera(device);
    }

    private void closeCamera(Device device) {
        apiClient.closeCamera(device);
    }
}
