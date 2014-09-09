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

import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.VideoView;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;

/**
 * Created by Ivan PL on 09.09.2014.
 */
public class CameraActivity extends BaseActivity {

    public static final String KEY_DEVICE = "device";

    private VideoView mVideoView;
    private Device mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mVideoView = (VideoView)findViewById(R.id.myVideo);
        mDevice = (Device) getIntent().getSerializableExtra(KEY_DEVICE);
        mVideoView.setVideoURI(Uri.parse("http://webcam.st-malo.com/axis-cgi/mjpg/video.cgi?resolution=352x288"));//mDevice.metrics.url));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mDevice.metrics.title);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
