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

import android.text.TextUtils;
import android.webkit.URLUtil;

import me.z_wave.android.R;

import java.util.List;

public class Device {

    public String id;
    public Metrics metrics;
    public List<String> tags;
    public String location;
    public DeviceType deviceType;


    public boolean isIconLink(){
        return URLUtil.isValidUrl(metrics.icon);
    }

    public int getIconId(){
        final String icon = metrics.icon;
        if(TextUtils.isEmpty(icon))
            return 0;

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
        } else if(icon.equalsIgnoreCase("blinds")){
            return R.drawable.ic_device_blinds;
        } else if(icon.equalsIgnoreCase("light")){
            return R.drawable.ic_device_light;
        } else if(icon.equalsIgnoreCase("energy")){
            return R.drawable.ic_device_energy;
        } else if(icon.equalsIgnoreCase("door")){
            return R.drawable.ic_device_door;
        } else if(icon.equalsIgnoreCase("motion")){
            return R.drawable.ic_device_motion;
        } else if(icon.equalsIgnoreCase("cooling")){
            return R.drawable.ic_device_cooling;
        } else if(icon.equalsIgnoreCase("fan")){
            return R.drawable.ic_device_fan;
        } else if(icon.equalsIgnoreCase("flood")){
            return R.drawable.ic_device_flood;
        } else if(icon.equalsIgnoreCase("gas")){
            return R.drawable.ic_device_gas;
        } else if(icon.equalsIgnoreCase("heating")){
            return R.drawable.ic_device_heating;
        } else if(icon.equalsIgnoreCase("humidity")){
            return R.drawable.ic_device_humidity;
        } else if(icon.equalsIgnoreCase("luminosity")){
            return R.drawable.ic_device_luminosity;
        } else if(icon.equalsIgnoreCase("media")){
            return R.drawable.ic_device_media;
        } else if(icon.equalsIgnoreCase("smoke")){
            return R.drawable.ic_device_smoke;
        } else if(icon.equalsIgnoreCase("thermostat")){
            return R.drawable.ic_device_thermostat;
        } else if(icon.equalsIgnoreCase("water")){
            return R.drawable.ic_device_water;
        } else if(icon.equalsIgnoreCase("window")){
            return R.drawable.ic_device_window;
        }
        return 0;
    }

    public String getValue(){
        return String.format("%s %s", metrics.level, metrics.scaleTitle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        if (id != null ? !id.equals(device.id) : device.id != null) return false;
        return true;
    }

}
