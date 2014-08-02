/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 08.07.14 10:21.
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

import android.content.Intent;
import android.os.Bundle;
import com.squareup.otto.Subscribe;

import me.z_wave.android.R;
import me.z_wave.android.otto.events.AccountChangedEvent;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.otto.events.ProgressEvent;
import me.z_wave.android.otto.events.ShowAlertDialogEvent;
import me.z_wave.android.otto.events.ShowAttentionDialogEvent;
import me.z_wave.android.otto.events.StartActivityEvent;
import me.z_wave.android.ui.fragments.SplashFragment;

/**
 * Created by Ivan PL on 08.07.2014.
 */
public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(getScreenOrientationOption());
        setContentView(R.layout.activity_start);

        if(savedInstanceState == null)
            commitFragment(new SplashFragment(), false);

    }

    @Subscribe
    public void onCommitFragment(CommitFragmentEvent event){
        commitFragment(event.fragment, event.addToBackStack);
    }

    @Subscribe
    public void onAccountChanged(AccountChangedEvent event){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Subscribe
    public void onShowAlertDialog(ShowAlertDialogEvent event){
        super.onShowAlertDialog(event);
    }

    @Subscribe
    public void showAttentionDialog(ShowAttentionDialogEvent event){
        super.showAttentionDialog(event);
    }

    @Subscribe
    public void onShowHideProgress(ProgressEvent event){
        super.onShowHideProgress(event);
    }


}
