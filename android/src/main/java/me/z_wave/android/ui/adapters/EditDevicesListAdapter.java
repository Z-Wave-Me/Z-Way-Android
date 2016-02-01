/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 07.09.14 21:45.
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

package me.z_wave.android.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.DeviceRgbColor;
import me.z_wave.android.dataModel.DeviceType;
import me.z_wave.android.dataModel.Profile;

public class EditDevicesListAdapter extends ArrayAdapter<Device> {

    private List<String> mDevicesIds;

    public EditDevicesListAdapter(Context context, List<Device> objects, Profile profile) {
        super(context, 0, objects);
        mDevicesIds = new ArrayList<String>();
        if(profile != null) {
            mDevicesIds.addAll(profile.dashboard);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = View.inflate(getContext(), R.layout.layout_edit_device_list_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }

        final ViewHolder holder = (ViewHolder) convertView.getTag();
        final Device device = getItem(position);
        holder.name.setText(device.metrics.title);
        setDeviceIcon(holder, device);
        prepareAddRemoveView(holder, device);
        return convertView;
    }

    public List<String> getDashboardDevicesIds(){
        return mDevicesIds;
    }

    private void setDeviceIcon(ViewHolder holder, Device device) {
        if(device.isIconLink()){
            Picasso.with(getContext()).load(device.metrics.icon).into(holder.icon);
        } else {
            if(device.getIconId() == 0){
                holder.icon.setImageDrawable(null);
            } else {
                holder.icon.setImageResource(device.getIconId());
            }
        }
    }

    private void prepareAddRemoveView(ViewHolder holder, final Device device) {
        final boolean isOnDashboard = mDevicesIds != null && mDevicesIds.contains(device.id);
        final int addRemoveTextResId = isOnDashboard ? R.string.dashboard_remove
                : R.string.dashboard_to_dashboard;
        final int addRemoveBgColorResId = isOnDashboard ? R.color.red
                : R.color.dark_gray;

        holder.addRemove.setText(addRemoveTextResId);
        holder.addRemove.setBackgroundColor(
                getContext().getResources().getColor(addRemoveBgColorResId));
        holder.addRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDevicesIds.contains(device.id)) {
                    mDevicesIds.remove(device.id);
                } else {
                    mDevicesIds.add(device.id);
                }
                notifyDataSetChanged();
            }
        });
    }

    //тогл - только кнопка
    //свич ремоут - в последнюю очередь
    //камера - важно! при клике открывать окно, там 8 кнопок чтобы вертеть камеру зум + оноф;
    // + окошко. где стримится видео с камеры
    //свичg rgb - вкл/выкл + колорпиккер

    private class ViewHolder{
        public ImageView icon;
        public TextView name;
        public TextView addRemove;

        private ViewHolder(View parent) {
            icon = (ImageView) parent.findViewById(R.id.device_list_item_icon);
            name = (TextView) parent.findViewById(R.id.device_list_item_name);
            addRemove = (TextView) parent.findViewById(R.id.device_list_item_add_remove);
        }
    }

}
