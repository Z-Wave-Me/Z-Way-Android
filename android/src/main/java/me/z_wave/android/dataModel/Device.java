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
        if(deviceType.equalsIgnoreCase("fan")){
            return R.drawable.ic_device_fan;
        } else if(deviceType.equalsIgnoreCase("thermostat")){
            return R.drawable.ic_device_thermostat;
        } else if(deviceType.equalsIgnoreCase("switchMultilevel")){
            return R.drawable.ic_device_light;
        } else if(deviceType.equalsIgnoreCase("switchBinary")){
            return R.drawable.ic_device_switch;
        } else if(deviceType.equalsIgnoreCase("probe") || deviceType.equalsIgnoreCase("battery")) {
            return R.drawable.ic_device_battery;
        }
        return R.drawable.ic_plase_holder;
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
