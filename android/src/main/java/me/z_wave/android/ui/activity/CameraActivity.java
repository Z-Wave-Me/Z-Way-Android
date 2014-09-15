/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 09.09.14 20:32.
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

package me.z_wave.android.ui.activity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.ui.views.mjpegView.MjpegView;

/**
 * Created by Ivan PL on 09.09.2014.
 */
public class CameraActivity extends BaseActivity implements ApiClient.EmptyApiCallback<Device> {

    public static final String TAG = CameraActivity.class.getSimpleName();

    public static final String KEY_DEVICE = "device";

    @InjectView(R.id.video_mjpeg_view) MjpegView mjpegView;
    private Device mDevice;

    @Inject
    ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        ButterKnife.inject(this);
        mDevice = (Device) getIntent().getSerializableExtra(KEY_DEVICE);

        mjpegView.setDisplayMode(MjpegView.SIZE_BEST_FIT);
        mjpegView.showFps(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mjpegView.setSource(mDevice.metrics.url);
    }

    @Override
    public void onPause() {
        super.onPause();
        mjpegView.stopPlayback();
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure(Device request, boolean isNetworkError) {

    }

    @OnClick(R.id.video_btn_up)
    public void moveCameraUp() {
        apiClient.moveCameraUp(mDevice, this);
    }

    @OnClick(R.id.video_btn_down)
    public void moveCameraDown() {
        apiClient.moveCameraDown(mDevice, this);
    }

    @OnClick(R.id.video_btn_left)
    public void moveCameraLeft() {
        apiClient.moveCameraLeft(mDevice, this);
    }

    @OnClick(R.id.video_btn_right)
    public void moveCameraRight() {
        apiClient.moveCameraRight(mDevice, this);
    }

    @OnClick(R.id.video_btn_zoom_in)
    public void zoomIn() {
        apiClient.cameraZoomIn(mDevice, this);
    }

    @OnClick(R.id.video_btn_zoom_out)
    public void zoomOut() {
        apiClient.cameraZoomOut(mDevice, this);
    }

    @OnClick(R.id.video_btn_open)
    public void cameraOpen() {
        apiClient.openCamera(mDevice, this);
    }

    @OnClick(R.id.video_btn_close)
    public void cameraClose() {
        apiClient.closeCamera(mDevice, this);
    }

}
