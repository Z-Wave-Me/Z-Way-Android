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
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;

import org.apache.http.cookie.Cookie;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.z_wave.android.R;
import me.z_wave.android.app.Constants;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.dataModel.Metrics;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.servises.UpdateDeviceService;
import me.z_wave.android.ui.views.mjpegView.MjpegView;
import me.z_wave.android.utils.CameraUtils;

/**
 * Created by Ivan PL on 09.09.2014.
 */
public class CameraActivity extends BaseActivity {

    public static final String KEY_DEVICE = "device";

    @InjectView(R.id.video_mjpeg_view) MjpegView mjpegView;
    private Device mDevice;

    @Inject ApiClient apiClient;

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
        prepareHeaders();

        Metrics cameraMetrics = mDevice.metrics;
        changeButtonVisibility(findViewById(R.id.video_btn_up), cameraMetrics.hasUp);
        changeButtonVisibility(findViewById(R.id.video_btn_down), cameraMetrics.hasDown);
        changeButtonVisibility(findViewById(R.id.video_btn_left), cameraMetrics.hasLeft);
        changeButtonVisibility(findViewById(R.id.video_btn_right), cameraMetrics.hasRight);
        changeButtonVisibility(findViewById(R.id.video_btn_zoom_in), cameraMetrics.hasZoomIn);
        changeButtonVisibility(findViewById(R.id.video_btn_zoom_out), cameraMetrics.hasZoomOut);
        changeButtonVisibility(findViewById(R.id.video_btn_open), cameraMetrics.hasOpen);
        changeButtonVisibility(findViewById(R.id.video_btn_close), cameraMetrics.hasClose);
    }

    @Override
    public void onResume() {
        super.onResume();
        final LocalProfile profile = DatabaseDataProvider.getInstance(this).getActiveLocalProfile();
        final String cameraUrl = CameraUtils.getCameraUrl(profile, mDevice.metrics.url);
        mjpegView.setSource(cameraUrl);
    }

    @Override
    public void onPause() {
        super.onPause();
        mjpegView.stopPlayback();
    }

    @OnClick(R.id.video_btn_up)
    public void moveCameraUp() {
        UpdateDeviceService.moveCameraUp(this, mDevice);
    }

    @OnClick(R.id.video_btn_down)
    public void moveCameraDown() {
        UpdateDeviceService.moveCameraDown(this, mDevice);
    }

    @OnClick(R.id.video_btn_left)
    public void moveCameraLeft() {
        UpdateDeviceService.moveCameraLeft(this, mDevice);
    }

    @OnClick(R.id.video_btn_right)
    public void moveCameraRight() {
        UpdateDeviceService.moveCameraRight(this, mDevice);
    }

    @OnClick(R.id.video_btn_zoom_in)
    public void zoomIn() {
        UpdateDeviceService.zoomCameraIn(this, mDevice);
    }

    @OnClick(R.id.video_btn_zoom_out)
    public void zoomOut() {
        UpdateDeviceService.zoomCameraOut(this, mDevice);
    }

    @OnClick(R.id.video_btn_open)
    public void cameraOpen() {
        UpdateDeviceService.openCamera(this, mDevice);
    }

    @OnClick(R.id.video_btn_close)
    public void cameraClose() {
        UpdateDeviceService.closeCamera(this, mDevice);
    }

    private void changeButtonVisibility(View v, boolean isVisible) {
        v.setVisibility(isVisible ? View.VISIBLE :View.INVISIBLE);
    }

    private void prepareHeaders() {
        final Cookie cookie = apiClient.getCookie();
        if(cookie != null && !TextUtils.isEmpty(cookie.getValue())) {
            mjpegView.addHeader(cookie.getName(), cookie.getValue());
        }
    }

}
