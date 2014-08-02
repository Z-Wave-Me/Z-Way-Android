/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 02.08.14 9:45.
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

package me.z_wave.android.ui.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.Filter;
import me.z_wave.android.dataModel.Notification;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.otto.events.AccountChangedEvent;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.otto.events.OnDataUpdatedEvent;
import me.z_wave.android.otto.events.OnGetNotificationEvent;
import me.z_wave.android.ui.fragments.dashboard.DashboardFragment;
import timber.log.Timber;

public class MainMenuFragment extends BaseFragment {

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    @InjectView(R.id.nav_drawer_notifications)
    TextView notificationsView;

    @InjectView(R.id.nav_drawer_rooms)
    ViewGroup roomsGroupView;

    @InjectView(R.id.nav_drawer_rooms_menu)
    ViewGroup roomsGroupMenuView;

    @InjectView(R.id.nav_drawer_types)
    ViewGroup typesGroupView;

    @InjectView(R.id.nav_drawer_types_menu)
    ViewGroup typesGroupMenuView;

    @InjectView(R.id.nav_drawer_tags)
    ViewGroup tagsGroupView;

    @InjectView(R.id.nav_drawer_tags_menu)
    ViewGroup tagsGroupMenuView;

    @InjectView(R.id.nav_drawer_profile)
    View profileView;

    @InjectView(R.id.nav_drawer_profile_name)
    TextView profileName;

    @InjectView(R.id.nav_drawer_profile_location)
    TextView profileLocation;

    private View mSelectedView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final View dashboardView = getView().findViewById(R.id.nav_drawer_dashboard);
        setSelectedView(dashboardView);

        prepareRoomsList();
        prepareTypesList();
        prepareTagsList();

        final Profile profile = dataContext.getActiveProfile();
        if(profile != null)
            profileName.setText(profile.name);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mDrawerLayout != null && isOpen()) {
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onGetNotification(OnGetNotificationEvent event){
        final List<Notification> notifications = dataContext.getNotifications();
        if(notifications.isEmpty()) {
            notificationsView.setText(R.string.notification_everything_ok);
        } else {
            notificationsView.setText(
                    String.format(getString(R.string.notification_count),
                            notifications.size()));
        }
    }

    @Subscribe
    public void onDataUpdated(OnDataUpdatedEvent event){
        Timber.v("Device list updated!");
        prepareRoomsList();
        prepareTypesList();
        prepareTagsList();
    }

    @Subscribe
    public void onAccountChanged(AccountChangedEvent event){
//        mNotificationsCount.setVisibility(View.GONE);
//        mNotificationsCount.setText("");
    }

    @OnClick(R.id.nav_drawer_profile)
    public void showProfiles(View v){
        changeFragment(v, new ProfilesFragment());
    }

    @OnClick(R.id.nav_drawer_dashboard)
    public void showDashboard(View v){
        changeFragment(v, new DashboardFragment());
    }

    @OnClick(R.id.nav_drawer_all_devices)
    public void showAllDevices(View v){
        changeFragment(v, DevicesFragment.newInstance(Filter.TYPE, Filter.DEFAULT_FILTER));
    }

    @OnClick(R.id.nav_drawer_notifications)
    public void showNotifications(View v){
        changeFragment(v, new NotificationsFragment());
    }

    private void setSelectedView(View selectedView){
        if(mSelectedView != null)
            mSelectedView.setSelected(false);

        mSelectedView = selectedView;
        mSelectedView.setSelected(true);
    }

    private void prepareRoomsList(){
        final List<String> rooms = dataContext.getLocations();
        roomsGroupView.setVisibility(rooms.isEmpty() ? View.GONE : View.VISIBLE);
        if(!rooms.isEmpty()) {
            roomsGroupMenuView.removeAllViews();
            for(String room : rooms) {
                final View item = inflateMenuItem(roomsGroupMenuView, Filter.LOCATION, room);
                roomsGroupMenuView.addView(item);
            }
        }
    }

    private void prepareTypesList(){
        final List<String> types = dataContext.getDeviceTypes();
        typesGroupView.setVisibility(types.isEmpty() ? View.GONE : View.VISIBLE);
        if(!types.isEmpty()) {
            typesGroupMenuView.removeAllViews();
            for(String type : types) {
                final View item = inflateMenuItem(typesGroupMenuView, Filter.TYPE, type);
                typesGroupMenuView.addView(item);
            }
        }
    }

    private void prepareTagsList(){
        final List<String> tags = dataContext.getDeviceTags();
        tagsGroupView.setVisibility(tags.isEmpty() ? View.GONE : View.VISIBLE);
        if(!tags.isEmpty()) {
            tagsGroupMenuView.removeAllViews();
            for(String room : tags) {
                final View item = inflateMenuItem(tagsGroupMenuView, Filter.LOCATION, room);
                tagsGroupMenuView.addView(item);
            }
        }
    }

    private View inflateMenuItem(ViewGroup parent, final Filter filter, final String filterValue){
        final TextView menuItem = (TextView) LayoutInflater.from(getActivity()).
                inflate(R.layout.layout_menu_item, parent, false);
        menuItem.setText(filterValue);
        menuItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(v, DevicesFragment.newInstance(filter, filterValue));
            }
        });
        return menuItem;
    }

    private void changeFragment(View v, Fragment fragment) {
        if(!mSelectedView.equals(v)){
            bus.post(new CommitFragmentEvent(fragment, false));
            setSelectedView(v);
        }
        closeDrawer();
    }



//    @OnClick(R.id.menu_profile)
//    public void showProfile(){
//        Toast.makeText(getActivity(), "menu_profile", Toast.LENGTH_SHORT).show();
//        closeDrawer();
//    }

//    @OnClick(R.id.menu_buy)
//    public void searchSales(){
//        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
//            @Override
//            public void onDrawerSlide(View drawerView, float slideOffset) {
//                mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
//            }
//
//            @Override
//            public void onDrawerOpened(View drawerView) {
//                mDrawerToggle.onDrawerOpened(drawerView);
//            }
//
//            @Override
//            public void onDrawerClosed(View drawerView) {
//                mDrawerToggle.onDrawerClosed(drawerView);
//                mDrawerLayout.setDrawerListener(mDrawerToggle);
//                bus.post(new CommitFragmentEvent(SearchFragment.newInstance(true), true));
//            }
//
//            @Override
//            public void onDrawerStateChanged(int newState) {
//                mDrawerToggle.onDrawerStateChanged(newState);
//            }
//        });
//        mDrawerLayout.closeDrawers();
//    }

//    @OnClick(R.id.menu_rent)
//    public void searchRent(){
//        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
//            @Override
//            public void onDrawerSlide(View drawerView, float slideOffset) {
//                mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
//            }
//
//            @Override
//            public void onDrawerOpened(View drawerView) {
//                mDrawerToggle.onDrawerOpened(drawerView);
//            }
//
//            @Override
//            public void onDrawerClosed(View drawerView) {
//                mDrawerToggle.onDrawerClosed(drawerView);
//                mDrawerLayout.setDrawerListener(mDrawerToggle);
//                bus.post(new CommitFragmentEvent(SearchFragment.newInstance(false), true));
//            }
//
//            @Override
//            public void onDrawerStateChanged(int newState) {
//                mDrawerToggle.onDrawerStateChanged(newState);
//            }
//        });
//        mDrawerLayout.closeDrawers();
//    }

//    @OnClick(R.id.menu_add)
//    public void addRealty(){
//        mDrawerLayout.closeDrawers();
//        Toast.makeText(getActivity(), "menu_add", Toast.LENGTH_SHORT).show();
//    }

//    @OnClick(R.id.menu_favourite)
//    public void showFavourites(){
//        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
//            @Override
//            public void onDrawerSlide(View drawerView, float slideOffset) {
//                mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
//            }
//
//            @Override
//            public void onDrawerOpened(View drawerView) {
//                mDrawerToggle.onDrawerOpened(drawerView);
//            }
//
//            @Override
//            public void onDrawerClosed(View drawerView) {
//                mDrawerToggle.onDrawerClosed(drawerView);
//                mDrawerLayout.setDrawerListener(mDrawerToggle);
//                bus.post(new CommitFragmentEvent(RealtyListFragment.newInstance(true), false));
//            }
//
//            @Override
//            public void onDrawerStateChanged(int newState) {
//                mDrawerToggle.onDrawerStateChanged(newState);
//            }
//        });
//        mDrawerLayout.closeDrawers();
//    }
//
//    @OnClick(R.id.menu_history)
//    public void showHistory(){
//        Toast.makeText(getActivity(), "menu_history", Toast.LENGTH_SHORT).show();
//        mDrawerLayout.closeDrawers();
//    }

//    @Subscribe
//    public void onUnlockMainMenu(UnlockMainMenuEvent event){
//        setNavigationMenuEnabled(true);
//    }

    public void setNavigationMenuEnabled(boolean enabled){
        final int lockMode = enabled ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        mDrawerLayout.setDrawerLockMode(lockMode);
        mDrawerToggle.setDrawerIndicatorEnabled(enabled);
    }

    public boolean isOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
                getActivity().invalidateOptionsMenu();
            }
        };

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void closeDrawer(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                mDrawerLayout.closeDrawers();
            }
        }, 150);
    }

    public void showDrawer() {
        mDrawerLayout.openDrawer(getView());
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

}
