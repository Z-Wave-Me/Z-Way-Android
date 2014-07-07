/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 28.05.14 22:37.
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
import android.widget.*;

import com.squareup.picasso.Picasso;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.DeviceRgbColor;
import me.z_wave.android.dataModel.DeviceType;
import me.z_wave.android.dataModel.Profile;

import java.util.List;

public class DevicesGridAdapter extends ArrayAdapter<Device> {

    public interface DeviceStateUpdatedListener{
        void onSwitchStateChanged(Device updatedDevice);
        void onSeekBarStateChanged(Device updatedDevice);
        void onToggleClicked(Device updatedDevice);
        void onColorViewClicked(Device updatedDevice);
        void onAddRemoveClicked(Device updatedDevice);
    }

    private final DeviceStateUpdatedListener mListener;
    private Profile mProfile;


    public DevicesGridAdapter(Context context, List<Device> objects, Profile profile,
                              DeviceStateUpdatedListener listener) {
        super(context, 0, objects);
        mListener = listener;
        mProfile = profile;
    }

    public void setProfile(Profile profile){
        mProfile = profile;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = View.inflate(getContext(), R.layout.layout_device_greed_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }

        final ViewHolder holder = (ViewHolder) convertView.getTag();
        final Device device = getItem(position);

        holder.name.setText(device.metrics.title);
        setDeviceIcon(holder, device);

        prepareValueView(holder, device);
        prepareSwitch(holder, device);
        prepareSeekBar(holder, device);
        prepareRgbView(holder, device);
        prepareToggle(holder, device);
        prepareAddRemoveView(holder, device);



        final DeviceType deviceType = device.deviceType;
        if(deviceType == DeviceType.FAN ||
                deviceType == DeviceType.THERMOSTAT){
            //TODO unknown device
        }  else if(deviceType == DeviceType.TOGGLE_BUTTON){
            //TODO unknown device
        }

        return convertView;
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
        final boolean isOnDashboard = mProfile != null && mProfile.positions != null
                && mProfile.positions.contains(device.id);
        final int addRemoveTextResId = isOnDashboard ? R.string.dashboadr_remove
                : R.string.dashboard_to_dashboard;
        final int addRemoveBgColorResId = isOnDashboard ? R.color.red
                : R.color.dark_gray;

        holder.addRemove.setText(addRemoveTextResId);
        holder.addRemove.setBackgroundColor(
                getContext().getResources().getColor(addRemoveBgColorResId));
        holder.addRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAddRemoveClicked(device);
            }
        });
    }

    private void prepareValueView(ViewHolder holder, Device device){
        final DeviceType deviceType = device.deviceType;
        final boolean isValueVisible = deviceType == DeviceType.SENSOR_BINARY
                || deviceType == DeviceType.BATTERY
                || deviceType == DeviceType.SENSOR_MULTILEVEL;

        changeViewVisibility(holder.value, isValueVisible);
        if(isValueVisible)
            holder.value.setText(device.getValue());
    }

    private void prepareSwitch(ViewHolder holder, final Device device){
        final DeviceType deviceType = device.deviceType;
        final boolean isSwitcherVisible = deviceType == DeviceType.SWITCH_CONTROLL
                || deviceType == DeviceType.SWITCH_BINARY
                || deviceType == DeviceType.DOORLOCK
                || deviceType == DeviceType.SWITCH_RGBW;

        holder.switcher.setOnClickListener(null);
        changeViewVisibility(holder.switcher, isSwitcherVisible);
        if(isSwitcherVisible) {
            if(deviceType == DeviceType.DOORLOCK) {
                holder.switcher.setTextOff("close");
                holder.switcher.setTextOn("open");

                if(device.metrics.mode != null)
                    holder.switcher.setChecked(!device.metrics.mode.equalsIgnoreCase("close"));
            } else {
                holder.switcher.setTextOff("off");
                holder.switcher.setTextOn("on");

                if(device.metrics.level != null)
                    holder.switcher.setChecked(!device.metrics.level.equalsIgnoreCase("off"));
            }

            holder.switcher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String newState = ((Switch) v).isChecked()
                            ? ((Switch) v).getTextOn().toString()
                            : ((Switch) v).getTextOff().toString();

                    if(device.deviceType == DeviceType.DOORLOCK){
                        if(!device.metrics.mode.equalsIgnoreCase(newState)){
                            device.metrics.mode = newState;
                            mListener.onSwitchStateChanged(device);
                        }
                    } else {
                        if(!device.metrics.level.equalsIgnoreCase(newState)){
                            device.metrics.level = newState;
                            mListener.onSwitchStateChanged(device);
                        }
                    }
                }
            });
        }
    }

    private void prepareSeekBar(ViewHolder holder, final Device device){
        final DeviceType deviceType = device.deviceType;
        final boolean isSeekBarVisible = deviceType == DeviceType.SWITCH_MULTILEVEL;

        holder.seekBar.setOnSeekBarChangeListener(null);
        changeViewVisibility(holder.seekBar, isSeekBarVisible);
        if(isSeekBarVisible){
            holder.seekBar.setProgress(Integer.valueOf(device.metrics.level));
            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    device.metrics.level = String.valueOf(seekBar.getProgress());
                    mListener.onSeekBarStateChanged(device);
                }
            });
        }
    }

    private void prepareRgbView(ViewHolder holder, final Device device){
        final DeviceType deviceType = device.deviceType;
        final boolean isRgbViewVisible = deviceType == DeviceType.SWITCH_RGBW;

        holder.rgbView.setOnClickListener(null);
        changeViewVisibility(holder.rgbView, isRgbViewVisible);
        if(isRgbViewVisible){
            final DeviceRgbColor color = device.metrics.color;
            holder.rgbView.setBackgroundColor(Color.rgb(color.r, color.g, color.b));
            holder.rgbView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onColorViewClicked(device);
                }
            });
        }
    }

    private void prepareToggle(ViewHolder holder, final Device device){
        final DeviceType deviceType = device.deviceType;
        final boolean isToggleVisible = deviceType == DeviceType.TOGGLE_BUTTON;

        holder.toggle.setOnClickListener(null);
        changeViewVisibility(holder.toggle, isToggleVisible);
        if(isToggleVisible){
            holder.toggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onToggleClicked(device);
                }
            });

        }
    }

    private void changeViewVisibility(View view, boolean isVisible){
        final int visibility = isVisible ? View.VISIBLE : View.GONE;
        view.setVisibility(visibility);
    }

    //тогл - только кнопка
    //свич ремоут - в последнюю очередь
    //камера - важно! при клике открывать окно, там 8 кнопок чтобы вертеть камеру зум + оноф;
    // + окошко. где стримится видео с камеры
    //свичg rgb - вкл/выкл + колорпиккер

    private class ViewHolder{
        public ImageView icon;
        public TextView name;
        public TextView value;
        public Switch switcher;
        public SeekBar seekBar;
        public View rgbView;
        public Button toggle;

        public TextView addRemove;

        private ViewHolder(View parent) {
            icon = (ImageView) parent.findViewById(R.id.device_grid_item_icon);
            name = (TextView) parent.findViewById(R.id.device_grid_item_name);
            value = (TextView) parent.findViewById(R.id.device_grid_item_value);
            switcher = (Switch) parent.findViewById(R.id.device_grid_item_switch);
            seekBar = (SeekBar) parent.findViewById(R.id.device_grid_item_seek_bar);
            addRemove = (TextView) parent.findViewById(R.id.device_grid_item_add_remove);
            rgbView = parent.findViewById(R.id.device_grid_item_rgb_color);
            toggle = (Button) parent.findViewById(R.id.device_grid_item_toggle);
        }
    }

}
