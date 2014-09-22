/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 15.06.14 15:01.
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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.Filter;
import me.z_wave.android.otto.events.CommitFragmentEvent;
import me.z_wave.android.otto.events.OnDataUpdatedEvent;
import me.z_wave.android.ui.adapters.DevicesGridAdapter;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;


public class DevicesFragment extends BaseDeviceListFragment {

    public static final String FILTER_KEY = "filter_key";
    public static final String FILTER_NAME_KEY = "filter_name_key";

    @InjectView(R.id.devices_widgets)
    GridView widgetsGridView;

    @InjectView(R.id.devices_msg_empty)
    View emptyListMsg;


    private DevicesGridAdapter mAdapter;
    private Filter mFilter;
    private String mFilterValue;

    public static DevicesFragment newInstance(Filter filter, String filterValue){
        final DevicesFragment devicesFragment = new DevicesFragment();
        final Bundle args = new Bundle();
        args.putInt(FILTER_KEY, filter.ordinal());
        args.putString(FILTER_NAME_KEY, filterValue);
        devicesFragment.setArguments(args);
        return devicesFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_devices, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareDevicesView();
        changeEmptyMsgVisibility();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_devices, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.devices_edit:
                bus.post(new CommitFragmentEvent(EditDevicesFragment.newInstance(mFilter, mFilterValue), true));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void updateDevicesList(List<Device> devices) {
        for(Device device : devices) {
            if(isAppropriateDevice(device)) {
                updateDevice(device);
            } else {
                mDevices.remove(device);
            }
        }
    }

    @Subscribe
    public void onDataUpdated(OnDataUpdatedEvent event){
        Timber.v("Device list updated!");
        mAdapter.setProfile(dataContext.getActiveProfile());
        updateDevicesList(event.devices);
        mAdapter.notifyDataSetChanged();
        changeEmptyMsgVisibility();
    }

    private void changeEmptyMsgVisibility(){
        final int msgVisibility = mAdapter == null || mAdapter.getCount() == 0 ? View.VISIBLE : View.GONE;
        if(emptyListMsg.getVisibility() != msgVisibility){
            emptyListMsg.setVisibility(msgVisibility);
        }
    }

    private void prepareDevicesView(){
        mDevices =  getFilteredDeviceList();
        mAdapter = new DevicesGridAdapter(getActivity(), mDevices,
                dataContext.getActiveProfile(), this);
        widgetsGridView.setAdapter(mAdapter);
    }

    private List<Device> getFilteredDeviceList(){
        mFilter = Filter.values()[getArguments().getInt(FILTER_KEY, 0)];
        mFilterValue = getArguments().getString(FILTER_NAME_KEY, Filter.DEFAULT_FILTER);
        switch (mFilter){
            case LOCATION:
                return dataContext.getDevicesForLocation(mFilterValue);
            case TYPE:
                return dataContext.getDevicesWithType(mFilterValue);
            case TAG:
                return dataContext.getDevicesWithTag(mFilterValue);
        }
        return new ArrayList<Device>();
    }

    private boolean isAppropriateDevice(Device device) {
        if(mFilterValue.equalsIgnoreCase(Filter.DEFAULT_FILTER))
            return true;

        switch (mFilter){
            case LOCATION:
                return device.location != null &&  device.location.equalsIgnoreCase(mFilterValue);
            case TYPE:
                return device.deviceType.toString().equalsIgnoreCase(mFilterValue);
            case TAG:
                return device.tags.contains(mFilterValue);
        }
        return true;
    }
}
