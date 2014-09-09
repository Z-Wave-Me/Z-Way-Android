/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 03.07.14 11:28.
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

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public enum DeviceType implements Serializable{

    @SerializedName("battery")
    BATTERY,
    @SerializedName("sensorBinary")
    SENSOR_BINARY,
    @SerializedName("sensorMultilevel")
    SENSOR_MULTILEVEL,
    @SerializedName("switchMultilevel")
    SWITCH_MULTILEVEL,
    @SerializedName("switchBinary")
    SWITCH_BINARY,
    @SerializedName("doorlock")
    DOORLOCK,
    @SerializedName("toggleButton")
    TOGGLE_BUTTON,
    @SerializedName("switchRGBW")
    SWITCH_RGBW,
    @SerializedName("camera")
    CAMERA,
    @SerializedName("switchControl")
    SWITCH_CONTROLL,
    @SerializedName("thermostat")
    THERMOSTAT,
    @SerializedName("fan")
    FAN;

    @Override
    public String toString() {
        if(this == BATTERY)
            return "battery";
        if(this == SENSOR_BINARY)
            return "sensorBinary";
        if(this == SENSOR_MULTILEVEL)
            return "sensorMultilevel";
        if(this == SWITCH_MULTILEVEL)
            return "switchMultilevel";
        if(this == SWITCH_BINARY)
            return "switchBinary";
        if(this == DOORLOCK)
            return "doorlock";
        if(this == TOGGLE_BUTTON)
            return "toggleButton";
        if(this == SWITCH_RGBW)
            return "switchRGBW";
        if(this == CAMERA)
            return "camera";
        if(this == SWITCH_CONTROLL)
            return "switchControl";
        if(this == THERMOSTAT)
            return "thermostat";
        if(this == FAN)
            return "fan";
        return super.toString();
    }


}
