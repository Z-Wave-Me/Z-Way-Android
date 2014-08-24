/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 08.06.14 13:02.
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import butterknife.ButterKnife;
import butterknife.InjectView;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.Filter;
import me.z_wave.android.otto.events.CommitFragmentEvent;

import java.util.ArrayList;
import java.util.List;

public class FiltersFragment extends BaseFragment
        implements RadioGroup.OnCheckedChangeListener, AdapterView.OnItemClickListener {

    @InjectView(R.id.filter_container)
    RadioGroup filterType;

    @InjectView(R.id.filter_list)
    ListView filtersList;

    private Filter mSelectedFilter = Filter.LOCATION;
    private List<String> mFilters = new ArrayList<String>();
    private ArrayAdapter<String> mAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_filters, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        filterType.setOnCheckedChangeListener(this);
        prepareFiltersList();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(checkedId == R.id.filter_rooms){
            mSelectedFilter = Filter.LOCATION;
        } else if(checkedId == R.id.filter_types){
            mSelectedFilter = Filter.TYPE;
        } else {
            mSelectedFilter = Filter.TAG;
        }

        mFilters.clear();
        mFilters.addAll(getUpdatedFilters());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final String selectedFilter = mAdapter.getItem(position);
        bus.post(new CommitFragmentEvent(DevicesFragment.newInstance(mSelectedFilter, selectedFilter), true));
    }

    private void prepareFiltersList(){
        mFilters = getUpdatedFilters();
        mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.layout_filter_list_item, mFilters);
        filtersList.setAdapter(mAdapter);
        filtersList.setOnItemClickListener(this);
    }

    private List<String> getUpdatedFilters(){
        final List<String> result = new ArrayList<String>();
        result.add(Filter.DEFAULT_FILTER);
        if(mSelectedFilter == Filter.LOCATION){
            result.addAll(dataContext.getLocationsNames());
        } else if(mSelectedFilter == Filter.TYPE) {
            result.addAll(dataContext.getDeviceTypes());
        } else {
            result.addAll(dataContext.getDeviceTags());
        }
        return result;
    }
}
