/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 28.05.14 18:05.
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

package me.z_wave.android.dataModel;

import me.z_wave.android.R;

import java.util.List;

public class Device {

    public String id;
    public Metrics metrics;
    public List<String> tags;
    public String location;
    public String deviceType;
    public long updateTime;

    public int getIconId(){
        final String icon = metrics.icon;
        if(icon == null)
            return R.drawable.ic_plase_holder;

        if(icon.equalsIgnoreCase("switch")){
            return R.drawable.ic_device_switch;
        } else if(icon.equalsIgnoreCase("meter")){
            return R.drawable.ic_device_meter;
        } else if(icon.equalsIgnoreCase("battery")){
            final double batteryLevel = Double.parseDouble(metrics.level);
            if(batteryLevel >= 90){
                return R.drawable.ic_device_battery;
            } else if(batteryLevel >= 50 && batteryLevel < 90){
                return R.drawable.ic_battery_less90;
            } else if(batteryLevel >= 10 && batteryLevel < 50){
                return R.drawable.ic_battery_less50;
            } else if(batteryLevel < 10){
                return R.drawable.ic_battery_less10;
            }
        } else if(icon.equalsIgnoreCase("luminosity")){
            return R.drawable.ic_device_luminosity;
        } else if(icon.equalsIgnoreCase("temperature")){
            return R.drawable.ic_device_temperature;
        }
        return R.drawable.ic_plase_holder;

//        if(deviceType.equalsIgnoreCase("fan")){
//            return R.drawable.ic_device_fan;
//        } else if(deviceType.equalsIgnoreCase("thermostat")){
//            return R.drawable.ic_device_thermostat;
//        } else if(deviceType.equalsIgnoreCase("switchMultilevel")){
//            return R.drawable.ic_device_light;
//        } else if(deviceType.equalsIgnoreCase("switchBinary")){
//            return R.drawable.ic_device_switch;
//        } else if(deviceType.equalsIgnoreCase("probe") || deviceType.equalsIgnoreCase("battery")) {
//            return R.drawable.ic_device_battery;
//        }
//        return R.drawable.ic_plase_holder;
    }

    public String getValue(){
        return String.format("%s %s", metrics.level, metrics.scaleTitle);
    }

    public boolean isSensor(){
        return deviceType.equalsIgnoreCase("sensorBinary")
                || deviceType.equalsIgnoreCase("sensorMultilevel")
                || deviceType.equalsIgnoreCase("battery");
    }

    public boolean isSwitch(){
        return deviceType.equalsIgnoreCase("switchControl")
                || deviceType.equalsIgnoreCase("switchBinary")
                || deviceType.equalsIgnoreCase("switchRGBW")
                || deviceType.equalsIgnoreCase("toggleButton");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        if (id != null ? !id.equals(device.id) : device.id != null) return false;
        return true;
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

}
