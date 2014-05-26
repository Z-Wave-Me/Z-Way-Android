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
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements ActionBar.TabListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(getScreenOrientationOption());
        setContentView(R.layout.activity_main);

        setupActionBar();
    }

    private void setupActionBar() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar().addTab(createTab(R.string.dashboard, R.drawable.ic_dashboard));
        getActionBar().addTab(createTab(R.string.widgets, R.drawable.ic_widgets));
        getActionBar().addTab(createTab(R.string.notifications, R.drawable.ic_notifications));
        getActionBar().addTab(createTab(R.string.profiles, R.drawable.ic_profiles));
        return super.onCreateOptionsMenu(menu);
    }

    private ActionBar.Tab createTab(int titleId, int iconId){
        final View tabView = createTabView(titleId, iconId);
        return getActionBar().newTab().setCustomView(tabView)
                .setTabListener(this);
    }

    /**
     * Called when a tab enters the selected state.
     * @param tab The tab that was selected
     * @param ft  A {@link android.app.FragmentTransaction} for queuing fragment operations to execute
     *            during a tab switch. The previous tab's unselect and this tab's select will be
     *            executed in a single transaction. This FragmentTransaction does not support
     */
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new PlaceholderFragment())
                    .commit();
    }

    /**
     * Called when a tab exits the selected state.
     * @param tab The tab that was unselected
     * @param ft  A {@link android.app.FragmentTransaction} for queuing fragment operations to execute
     *            during a tab switch. This tab's unselect and the newly selected tab's select
     *            will be executed in a single transaction. This FragmentTransaction does not
     */
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    /**
     * Called when a tab that is already selected is chosen again by the user.
     * Some applications may use this action to return to the top level of a category.
     * @param tab The tab that was reselected.
     * @param ft  A {@link android.app.FragmentTransaction} for queuing fragment operations to execute
     *            once this method returns. This FragmentTransaction does not support
     */
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    private int getScreenOrientationOption(){
        final boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        return  isTablet ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
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
}
