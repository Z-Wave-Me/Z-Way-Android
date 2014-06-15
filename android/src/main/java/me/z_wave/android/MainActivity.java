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

package me.z_wave.android;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import me.z_wave.android.servises.BindHelper;
import me.z_wave.android.servises.DataUpdateService;
import me.z_wave.android.ui.fragments.DashboardFragment;
import me.z_wave.android.ui.fragments.FiltersFragment;
import me.z_wave.android.ui.fragments.NotificationsFragment;
import me.z_wave.android.ui.fragments.ProfilesFragment;

public class MainActivity extends Activity implements ActionBar.TabListener{

    private BindHelper mBindHelper = new BindHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(getScreenOrientationOption());
        setContentView(R.layout.activity_main);
        mBindHelper.keep(DataUpdateService.class);
        setupActionBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBindHelper.onBind(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBindHelper.onUnbind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar().addTab(createTab(R.string.dashboard, R.drawable.ic_dashboard));
        getActionBar().addTab(createTab(R.string.widgets, R.drawable.ic_widgets));
        getActionBar().addTab(createTab(R.string.notifications, R.drawable.ic_notifications));
        getActionBar().addTab(createTab(R.string.profiles, R.drawable.ic_profiles));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        final int tabPosition = tab.getPosition();
        if(tabPosition == 0) {
            commitFragment(new DashboardFragment(), false);
        } else if(tabPosition == 1) {
            commitFragment(new FiltersFragment(), false);
        } else if(tabPosition == 2) {
            commitFragment(new NotificationsFragment(), false);
        }else if(tabPosition == 3) {
            commitFragment(new ProfilesFragment(), false);
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

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
    }

    private ActionBar.Tab createTab(int titleId, int iconId){
        final View tabView = createTabView(titleId, iconId);
        return getActionBar().newTab().setCustomView(tabView)
                .setTabListener(this);
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

    private void commitFragment(Fragment fragment, boolean addToBackStack){
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
