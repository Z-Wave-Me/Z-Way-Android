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
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.otto.Subscribe;
import me.z_wave.android.R;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.otto.events.OnGetNotificationEvent;
import me.z_wave.android.servises.BindHelper;
import me.z_wave.android.servises.DataUpdateService;
import me.z_wave.android.servises.NotificationService;
import me.z_wave.android.ui.fragments.dashboard.DashboardFragment;
import me.z_wave.android.ui.fragments.FiltersFragment;
import me.z_wave.android.ui.fragments.NotificationsFragment;
import me.z_wave.android.ui.fragments.ProfilesFragment;
import me.z_wave.android.utils.FragmentUtils;

public class MainActivity extends BaseActivity {

    private BindHelper mBindHelper = new BindHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(getScreenOrientationOption());
        setContentView(R.layout.activity_main);
        mBindHelper.keep(DataUpdateService.class);
        ApiClient.auth("10903","newui", new ApiClient.OnAuthCompleteListener() {
            @Override
            public void onAuthComplete() {

            }
        });
//        mBindHelper.keep(NotificationService.class);
        setupActionBar();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Subscribe
    public void onCommitFragment(CommitFragmentEvent event){
        commitFragment(event.fragment, event.addToBackStack);
    }

    @Subscribe
    public void onGetNotification(OnGetNotificationEvent event){

    }

    private int getScreenOrientationOption(){
        final boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        return  isTablet ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    private void setupActionBar() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(createTab(R.string.dashboard, R.drawable.ic_dashboard,
                new ZWayTabListener(new DashboardFragment())));
        actionBar.addTab(createTab(R.string.widgets, R.drawable.ic_widgets,
                new ZWayTabListener(new FiltersFragment())));
        actionBar.addTab(createTab(R.string.notifications, R.drawable.ic_notifications,
                new ZWayTabListener(new NotificationsFragment())));
        actionBar.addTab(createTab(R.string.profiles, R.drawable.ic_profiles,
                new ZWayTabListener(new ProfilesFragment())));
    }

    private ActionBar.Tab createTab(int titleId, int iconId, ZWayTabListener listener){
        final View tabView = createTabView(titleId, iconId);
        return getActionBar().newTab().setCustomView(tabView)
                .setTabListener(listener);
    }

    private View createTabView(int titleId, int iconId){
        final LayoutInflater inflater = getLayoutInflater();
        final View tabView = inflater.inflate(R.layout.layout_tab_view, null);
        final TextView tabTitle = (TextView) tabView.findViewById(R.id.tab_title);
        final ImageView tabIcon = (ImageView) tabView.findViewById(R.id.tab_icon);

        tabIcon.setImageResource(iconId);
        tabTitle.setText(titleId);
        return tabView;
    }

    private void startNotificationListening(){
        final Intent i = new Intent(this, NotificationService.class);
        startService(i);
    }

    public void commitFragment(Fragment fragment, boolean addToBackStack){
        FragmentUtils.commitFragment(getFragmentManager(),
                R.id.fragment_container, fragment, addToBackStack);
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

}
