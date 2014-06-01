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

package me.z_wave.android.gui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;

import java.util.List;

public class DevicesGridAdapter extends ArrayAdapter<Device> {

    public interface DeviceStateUpdatedListener{
        void onExactChanged(Device updatedDevice);

//        devices/:deviceId/exact/on
//        devices/:deviceId/exact/off
//
//                thermostat
//        devices/:deviceId/setMode/:integer
//        devices/:deviceId/setTempo/:integer
//
//        multilevel:
//        devices/:deviceId/exact/:integer
//
//        fan:
//        devices/:deviceId/setMode/:mode (возможно :integer)
//        devices/:deviceId/setMode/off (возможно deprecated)
//
//        door:
//        devices/:deviceId/exact/open
//        devices/:deviceId/exact/close

    }

    private final DeviceStateUpdatedListener listener;

    public DevicesGridAdapter(Context context, List<Device> objects,
                              DeviceStateUpdatedListener listener) {
        super(context, 0, objects);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = View.inflate(getContext(), R.layout.layout_device_greed_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }

        final ViewHolder holder = (ViewHolder) convertView.getTag();
        final Device device = getItem(position);

        prepareViewVisibility(device, holder);
        holder.icon.setImageResource(device.getIconId());
        holder.name.setText(device.metrics.title);
        holder.value.setText(device.getValue());
        holder.switchView.setChecked(!device.metrics.level.equalsIgnoreCase("off"));

        holder.switchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newState = ((Switch)v).isChecked() ? "on" : "off";
                updateDeviceState(device, newState);
            }
        });


        return convertView;
    }

    private void prepareViewVisibility(Device device, ViewHolder holder){
        changeViewVisibility(holder.value, device.isSensor());
        changeViewVisibility(holder.switchView, device.isSwitch());
    }

    private void changeViewVisibility(View view, boolean isVisible){
        final int visibility = isVisible ? View.VISIBLE : View.GONE;
        view.setVisibility(visibility);
    }

    private void updateDeviceState(Device device, String level){
        if(!device.metrics.level.equalsIgnoreCase(level)){
            device.metrics.level = level;
            listener.onExactChanged(device);
        }
    }

//    if (model.get('deviceType') === "sensorBinary" || model.get('deviceType') === "sensorMultilevel" || model.get('deviceType') === "battery") {
//        modelView = new ProbeWidgetView({model: model});
//    } else if (model.get('deviceType') === "fan") {
//        modelView = new FanWidgetView({model: model});
//    } else if (model.get('deviceType') === "switchMultilevel") {
//        modelView = new MultilevelWidgetView({model: model});
//    } else if (model.get('deviceType') === "thermostat") {
//        modelView = new ThermostatView({model: model});
//    } else if (model.get('deviceType') === "doorlock") {
//        modelView = new DoorLockView({model: model});
//    } else if (model.get('deviceType') === "switchBinary" || model.get('deviceType') === "switchRGBW") {
//        modelView = new SwitchView({model: model});
//    } else if (model.get('deviceType') === "toggleButton") {
//        modelView = new ToggleView({model: model});
//    } else if (model.get('deviceType') === "camera") {
//        modelView = new CameraView({model: model});
//    } else if (model.get('deviceType') === "switchControl") {
//        modelView = new SwitchControlView({model: model});
//    } else {
//        log(model);
//    }

    private class ViewHolder{
        public ImageView icon;
        public TextView name;
        public TextView value;
        public Switch switchView;

        private ViewHolder(View parent) {
            icon = (ImageView) parent.findViewById(R.id.device_grid_item_icon);
            name = (TextView) parent.findViewById(R.id.device_grid_item_name);
            value = (TextView) parent.findViewById(R.id.device_grid_item_value);
            switchView = (Switch) parent.findViewById(R.id.device_grid_item_switch);

        }
    }

}
