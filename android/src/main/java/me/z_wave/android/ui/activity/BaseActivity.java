/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 15.06.14 16:25.
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

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import com.crittercism.app.Crittercism;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import me.z_wave.android.R;
import me.z_wave.android.app.ZWayApplication;
import me.z_wave.android.otto.events.ProgressEvent;
import me.z_wave.android.otto.events.ShowAlertDialogEvent;
import me.z_wave.android.otto.events.ShowAttentionDialogEvent;
import me.z_wave.android.ui.dialogs.AlertDialog;
import me.z_wave.android.ui.dialogs.BaseDialogFragment;
import me.z_wave.android.ui.dialogs.ProgressDialog;
import me.z_wave.android.utils.FragmentUtils;

import javax.inject.Inject;

public class BaseActivity extends Activity {

    @Inject
    Bus bus;

    private ProgressDialog mProgressDialog;
    private boolean mIsDialogVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crittercism.initialize(getApplicationContext(), "53ef3abdd478bc401300000a");
        ((ZWayApplication) getApplication()).inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    public int getScreenOrientationOption(){
        final boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        return  isTablet ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    public void commitFragment(Fragment fragment, boolean addToBackStack){
        FragmentUtils.commitFragment(getFragmentManager(),
                R.id.fragment_container, fragment, addToBackStack);
    }

    public void onShowAlertDialog(ShowAlertDialogEvent event){
        final AlertDialog alertDialog = new AlertDialog.AlertDialogBuilder(this).build(event);
        alertDialog.show(getFragmentManager(), AlertDialog.class.getSimpleName());
    }

    public void showAttentionDialog(ShowAttentionDialogEvent event){
        if(!mIsDialogVisible){
            onShowHideProgress(new ProgressEvent(false, false));
            AlertDialog.AlertDialogBuilder builder = new AlertDialog.AlertDialogBuilder(this);
            builder.setTitle(R.string.attention);
            builder.setIcon(R.drawable.ic_launcher);
            builder.setMessage(event.alertMessage);
            builder.setPositiveButton(R.string.ok, new BaseDialogFragment.DialogOnClickListener() {
                @Override
                public void onClick(View view, BaseDialogFragment dialog) {
                    dialog.dismiss();
                    mIsDialogVisible = false;
                }
            });

            final AlertDialog dialog = builder.build();
            dialog.setCancelable(false);
            dialog.show(getFragmentManager(), AlertDialog.class.getSimpleName());
            mIsDialogVisible = true;
        }
    }

    public void onShowHideProgress(ProgressEvent event){
        if(event.show && mProgressDialog == null){
            mProgressDialog = new ProgressDialog();
            mProgressDialog.show(getFragmentManager(), ProgressDialog.class.getSimpleName());
        } else if(!event.show && mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

}
