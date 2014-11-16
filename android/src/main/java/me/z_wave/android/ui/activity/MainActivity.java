/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 25.05.14 19:36.
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
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;

import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import me.z_wave.android.R;
import me.z_wave.android.data.DataContext;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.otto.events.InternetConnectionChangeEvent;
import me.z_wave.android.otto.events.ProgressEvent;
import me.z_wave.android.otto.events.ShowAttentionDialogEvent;
import me.z_wave.android.otto.events.ShowDialogEvent;
import me.z_wave.android.otto.events.ShowReconnectionProgressEvent;
import me.z_wave.android.otto.events.StartActivityEvent;
import me.z_wave.android.otto.events.StartStopLocationListeningEvent;
import me.z_wave.android.servises.AuthService;
import me.z_wave.android.servises.BindHelper;
import me.z_wave.android.servises.DataUpdateService;
import me.z_wave.android.servises.LocationService;
import me.z_wave.android.servises.NotificationService;
import me.z_wave.android.ui.dialogs.ConnectionLoseDialog;
import me.z_wave.android.ui.fragments.MainMenuFragment;
import me.z_wave.android.ui.fragments.dashboard.DashboardFragment;
import me.z_wave.android.utils.InternetConnectionUtils;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements FragmentManager.OnBackStackChangedListener {

    @Inject
    public DataContext dataContext;

    private BindHelper mBindHelper = new BindHelper();
    private MainMenuFragment mMainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(getScreenOrientationOption());
        setContentView(R.layout.activity_main);
        getFragmentManager().addOnBackStackChangedListener(this);
        setupNavigationDrawerFragment();

        mBindHelper.keep(DataUpdateService.class);
        setupActionBar();

        if (savedInstanceState == null)
            commitFragment(new DashboardFragment(), false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startNotificationListening();
        if(isChangeProfileByLocationEnable()) {
            startLocationListening();
        }
        mBindHelper.onBind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        final boolean isOnline = InternetConnectionUtils.isOnline(this);
        showHideInternetConnectionLoseDialog(isOnline);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBindHelper.onUnbind(this);
    }

    @Override
    public void onBackStackChanged() {
        final int entryCount = getFragmentManager().getBackStackEntryCount();
        mMainMenu.setNavigationMenuEnabled(entryCount == 0);
        Timber.v("entryCount " + entryCount + "; showDrawer " + (entryCount == 0));
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
    public void onShowHideProgress(ProgressEvent event) {
        super.onShowHideProgress(event);
    }

    @Subscribe
    public void onShowHideReconnectionProgressEvent(ShowReconnectionProgressEvent event) {
        super.onShowHideReconnectionProgress(event);
    }

    @Subscribe
    public void onInternetConnectionStateChanged(InternetConnectionChangeEvent event) {
        showHideInternetConnectionLoseDialog(event.isOnline);
    }

    private void showHideInternetConnectionLoseDialog(boolean isOnline) {
        if (!isOnline) {
            final ConnectionLoseDialog dialog = new ConnectionLoseDialog();
            dialog.show(getFragmentManager(), ConnectionLoseDialog.class.getSimpleName());
//            stopService(new Intent(this, NotificationService.class));
//            mBindHelper.onUnbind(this);
        } else {
            final Fragment fragment = getFragmentManager().findFragmentByTag(
                    ConnectionLoseDialog.class.getSimpleName());
            if (fragment != null && fragment instanceof ConnectionLoseDialog) {
                ((ConnectionLoseDialog) fragment).dismiss();
            }
            final DatabaseDataProvider provider = DatabaseDataProvider.getInstance(getApplicationContext());
            AuthService.login(this, provider.getActiveLocalProfile());
        }
    }

    @Subscribe
    public void showDialog(ShowDialogEvent event) {
        event.dialogFragment.show(getFragmentManager(), "Dialog");
    }

    @Subscribe
    public void onStartStopLocationListening(StartStopLocationListeningEvent event) {
        if(isChangeProfileByLocationEnable()) {
            startLocationListening();
        }
    }

    private void setupActionBar() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.show();
    }

    private void startNotificationListening() {
        final Intent i = new Intent(this, NotificationService.class);
        startService(i);
    }

    private void startLocationListening() {
        final Intent i = new Intent(this, LocationService.class);
        startService(i);
    }

    private void setupNavigationDrawerFragment() {
        mMainMenu = (MainMenuFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mMainMenu.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        mMainMenu.showDrawer();
    }

    private boolean isChangeProfileByLocationEnable() {
        final SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        return prefs.getBoolean(LocationService.CHANGE_PROFILE_BY_LOCATION, false);
    }

}
