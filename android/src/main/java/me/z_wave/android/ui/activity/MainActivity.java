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
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import me.z_wave.android.R;
import me.z_wave.android.data.DataContext;
import me.z_wave.android.dataModel.Notification;
import me.z_wave.android.otto.events.AccountChangedEvent;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.otto.events.OnGetNotificationEvent;
import me.z_wave.android.otto.events.ProgressEvent;
import me.z_wave.android.otto.events.ShowAlertDialogEvent;
import me.z_wave.android.otto.events.ShowAttentionDialogEvent;
import me.z_wave.android.otto.events.StartActivityEvent;
import me.z_wave.android.servises.BindHelper;
import me.z_wave.android.servises.DataUpdateService;
import me.z_wave.android.servises.NotificationService;
import me.z_wave.android.ui.fragments.MainMenuFragment;
import me.z_wave.android.ui.fragments.dashboard.DashboardFragment;
import me.z_wave.android.ui.fragments.FiltersFragment;
import me.z_wave.android.ui.fragments.NotificationsFragment;
import me.z_wave.android.ui.fragments.ProfilesFragment;
import timber.log.Timber;

public class MainActivity extends BaseActivity  implements FragmentManager.OnBackStackChangedListener {

    @Inject
    public DataContext dataContext;

    private BindHelper mBindHelper = new BindHelper();
    private TextView mNotificationsCount;
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

        if(savedInstanceState == null)
            commitFragment(new DashboardFragment(), false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startNotificationListening();
        mBindHelper.onBind(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBindHelper.onUnbind(this);
    }

    @Subscribe
    public void onCommitFragment(CommitFragmentEvent event){
        commitFragment(event.fragment, event.addToBackStack);
    }

    @Subscribe
    public void onStartActivity(StartActivityEvent event){
        startActivity(event.intent);
        finish();
    }

    @Subscribe
    public void onGetNotification(OnGetNotificationEvent event){
        updateNotificationsCount();
    }

    @Subscribe
    public void onAccountChanged(AccountChangedEvent event){
        mNotificationsCount.setVisibility(View.GONE);
        mNotificationsCount.setText("");
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

    private void setupActionBar() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.show();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(createTab(R.string.dashboard, R.drawable.ic_dashboard,
                new ZWayTabListener(new DashboardFragment())));
        actionBar.addTab(createTab(R.string.widgets, R.drawable.ic_widgets,
                new ZWayTabListener(new FiltersFragment())));
        actionBar.addTab(createNotificationTab(R.string.notifications, R.drawable.ic_notifications,
                new ZWayTabListener(new NotificationsFragment())));
        actionBar.addTab(createTab(R.string.profiles, R.drawable.ic_profiles,
                new ZWayTabListener(new ProfilesFragment())));
    }

    private ActionBar.Tab createTab(int titleId, int iconId, ZWayTabListener listener){
        final View tabView = createTabView(titleId, iconId);
        return getActionBar().newTab().setCustomView(tabView)
                .setTabListener(listener);
    }

    private ActionBar.Tab createNotificationTab(int titleId, int iconId, ZWayTabListener listener){
        final View tabView = createNotificationTabView(titleId, iconId);
        return getActionBar().newTab().setCustomView(tabView)
                .setTabListener(listener);
    }

    private View createTabView(int titleId, int iconId){
        final View tabView = getLayoutInflater().inflate(R.layout.layout_tab_view, null);
        final TextView tabTitle = (TextView) tabView.findViewById(R.id.tab_title);
        final ImageView tabIcon = (ImageView) tabView.findViewById(R.id.tab_icon);

        tabIcon.setImageResource(iconId);
        tabTitle.setText(titleId);
        return tabView;
    }

    private View createNotificationTabView(int titleId, int iconId){
        final View tabView = getLayoutInflater().inflate(R.layout.layout_tab_view_notifications, null);
        final TextView tabTitle = (TextView) tabView.findViewById(R.id.tab_title);
        final ImageView tabIcon = (ImageView) tabView.findViewById(R.id.tab_icon);
        mNotificationsCount = (TextView) tabView.findViewById(R.id.tab_notifications_count);

        tabIcon.setImageResource(iconId);
        tabTitle.setText(titleId);
        updateNotificationsCount();
        return tabView;
    }

    private void updateNotificationsCount() {
        final List<Notification> notifications = dataContext.getNotifications();
        if(notifications != null && !notifications.isEmpty()){
            mNotificationsCount.setText("" + notifications.size());
            mNotificationsCount.setVisibility(View.VISIBLE);
        } else {
            mNotificationsCount.setVisibility(View.GONE);
        }
    }

    private void startNotificationListening(){
        final Intent i = new Intent(this, NotificationService.class);
        startService(i);
    }

    @Override
    public void onBackStackChanged() {
        final int entryCount = getFragmentManager().getBackStackEntryCount();
        mMainMenu.setNavigationMenuEnabled(entryCount == 0);
        Timber.v("entryCount " + entryCount + "; showDrawer " + (entryCount == 0));
    }

    private class ZWayTabListener implements ActionBar.TabListener{

        public Fragment fragment;

        public ZWayTabListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.replace(R.id.fragment_container, fragment);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.remove(fragment);
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }
    }

    private void setupNavigationDrawerFragment() {
        mMainMenu = (MainMenuFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mMainMenu.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        mMainMenu.showDrawer();
    }


}
