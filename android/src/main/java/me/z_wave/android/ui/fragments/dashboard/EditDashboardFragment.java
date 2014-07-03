/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 02.07.14 15:38.
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

package me.z_wave.android.ui.fragments.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.OnDataUpdatedEvent;
import me.z_wave.android.ui.adapters.EditDashboardGridAdapter;
import me.z_wave.android.ui.fragments.BaseFragment;
import me.z_wave.android.ui.views.DragSortGridView;
import timber.log.Timber;

public class EditDashboardFragment extends BaseFragment implements EditDashboardGridAdapter.EditDashboardListener, DragSortGridView.OnReorderingListener {

    @InjectView(R.id.edit_dashboard_widgets)
    DragSortGridView dragSortGridView;

    private EditDashboardGridAdapter mAdapter;
    private List<Device> mDashboardDevices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_edit_dashboard, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDashboardDevices = dataContext.getDashboardDevices();
        prepareDevicesView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_dashboard, menu);
        getActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit_dashboard_done:
                //TODO save changes
                goBack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Subscribe
    public void onDataUpdated(OnDataUpdatedEvent event) {
        Timber.v("Dashboard list updated!");
        if(mDashboardDevices.isEmpty()){
            mDashboardDevices.addAll(dataContext.getDashboardDevices());
            mAdapter.notifyDataSetChanged();
        }
    }

    private void prepareDevicesView() {
        mAdapter = new EditDashboardGridAdapter(getActivity(), mDashboardDevices, this);
        dragSortGridView.setOnReorderingListener(this);
        dragSortGridView.setAdapter(mAdapter);
    }

    @Override
    public void onDeleteDevice(Device device) {
        mAdapter.remove(device);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRearrangeStarted(View item, int position) {
        item.startDrag(null, new View.DragShadowBuilder(item), position, 0);
    }

    @Override
    public void onReordering(int fromPosition, int toPosition) {
            if (fromPosition != toPosition) {
                final Device device = mDashboardDevices.remove(fromPosition);
                mDashboardDevices.add(toPosition, device);

//                int position = positions.remove(from);
//                positions.add(to, position);

                mAdapter.notifyDataSetChanged();
            }
    }
}
