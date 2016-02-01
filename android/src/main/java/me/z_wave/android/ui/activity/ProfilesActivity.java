/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 07.12.14 19:06.
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
import me.z_wave.android.otto.events.ShowAttentionDialogEvent;
import me.z_wave.android.otto.events.ShowDialogEvent;
import me.z_wave.android.otto.events.ShowReconnectionProgressEvent;
import me.z_wave.android.otto.events.StartActivityEvent;
import me.z_wave.android.ui.fragments.ProfilesFragment;

/**
 * Created by Ivan Pl on 07.12.2014.
 */
public class ProfilesActivity extends BaseActivity {

    public static final String KEY_FROM_SPLASH = "key_from_splash";

    private boolean isFromSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(getScreenOrientationOption());
        setContentView(R.layout.activity_base);
        if (savedInstanceState == null)
            commitFragment(new ProfilesFragment(), false);
        isFromSplash = getIntent().getBooleanExtra(KEY_FROM_SPLASH, false);
    }

    @Subscribe
    public void onCommitFragment(CommitFragmentEvent event) {
        commitFragment(event.fragment, event.addToBackStack);
    }

    @Subscribe
    public void onStartActivity(StartActivityEvent event) {
        startActivity(event.intent);
    }

    @Subscribe
    public void showAttentionDialog(ShowAttentionDialogEvent event) {
        super.showAttentionDialog(event);
    }

    @Subscribe
    public void onAccountChanged(AccountChangedEvent event){
        if(isFromSplash) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Subscribe
    public void onShowHideProgress(ProgressEvent event) {
        super.onShowHideProgress(event);
    }

    @Subscribe
    public void onShowHideReconnectionProgressEvent(ShowReconnectionProgressEvent event) {
        super.onShowHideReconnectionProgress(event);
    }

    @Subscribe
    public void showDialog(ShowDialogEvent event) {
        event.dialogFragment.show(getFragmentManager(), "Dialog");
    }

    @Override
    public void onBackPressed() {
        if (!getFragmentManager().popBackStackImmediate()) {
            if(isFromSplash) {
                finish();
            } else {
                final Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
    }
}
