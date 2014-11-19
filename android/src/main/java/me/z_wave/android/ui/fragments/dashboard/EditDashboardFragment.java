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

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.ProgressEvent;
import me.z_wave.android.ui.adapters.EditDashboardGridAdapter;
import me.z_wave.android.ui.fragments.BaseFragment;

public class EditDashboardFragment extends BaseFragment implements
        DragSortListView.DropListener, DragSortListView.RemoveListener {

    @InjectView(R.id.edit_dashboard_widgets)
    DragSortListView dragSortGridView;

    @Inject
    ApiClient apiClient;

    private EditDashboardGridAdapter mAdapter;
    private List<String> mDevicesIds;
    private DragSortController mController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_edit_dashboard, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDevicesIds = getDashboardDevicesIds();
        prepareDevicesView();

        dragSortGridView.setDropListener(this);
        dragSortGridView.setRemoveListener(this);

        mController = buildController(dragSortGridView);
        dragSortGridView.setFloatViewManager(mController);
        dragSortGridView.setOnTouchListener(mController);
        dragSortGridView.setDragEnabled(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_dashboard, menu);
        getActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void drop(int from, int to) {
        if (from != to) {
            Device item = mAdapter.getItem(from);
            mAdapter.remove(item);
            mAdapter.insert(item, to);

            final String position = mDevicesIds.get(from);
            mDevicesIds.remove(from);
            mDevicesIds.add(to, position);
        }
    }

    @Override
    public void remove(int which) {
        mAdapter.remove(mAdapter.getItem(which));
        mDevicesIds.remove(which);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_dashboard_done:
                final Profile profile = dataContext.getActiveProfile();
                profile.positions = mDevicesIds;

                bus.post(new ProgressEvent(true, false));
                apiClient.updateProfile(profile, new ApiClient.ApiCallback<List<Profile>, String>() {
                    @Override
                    public void onSuccess(List<Profile> result) {
                        bus.post(new ProgressEvent(false, false));
                        dataContext.addProfiles(result);
                        goBack();
                    }

                    @Override
                    public void onFailure(String request, boolean isNetworkError) {
                        bus.post(new ProgressEvent(false, false));
                        //TODO IVAN_PL error show
                        goBack();
                    }
                });

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void prepareDevicesView() {
        mAdapter = new EditDashboardGridAdapter(getActivity(), dataContext.getDashboardDevices());
        dragSortGridView.setAdapter(mAdapter);
    }

    private List<String> getDashboardDevicesIds() {
        final List<Device> devices = dataContext.getDashboardDevices();
        final List<String> result = new ArrayList<String>();
        for(Device device : devices) {
            result.add(device.id);
        }
        return result;
    }

    public DragSortController buildController(DragSortListView dslv) {
        DragSortController controller = new DragSortController(dslv);
        controller.setDragHandleId(R.id.drag_handle);
        controller.setClickRemoveId(R.id.click_remove);
        controller.setRemoveEnabled(true);
        controller.setSortEnabled(true);
        controller.setDragInitMode(DragSortController.ON_DOWN);
        controller.setRemoveMode(DragSortController.CLICK_REMOVE);
        return controller;
    }
}
