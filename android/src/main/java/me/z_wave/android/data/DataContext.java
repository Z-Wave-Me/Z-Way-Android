/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 14.06.14 17:39.
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

package me.z_wave.android.data;

import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.Location;

import java.util.ArrayList;
import java.util.List;

public class DataContext {

    private List<Device> mDevices= new ArrayList<Device>();
    private List<Location> mLocation = new ArrayList<Location>();

    public List<Location> getLocations() {
        return mLocation;
    }

    public void setLocations(List<Location> locations) {
        mLocation = locations;
    }

    public List<Device> getDevices() {
        if(mDevices == null)
            mDevices = new ArrayList<Device>();
        return mDevices;
    }

    public void setDevices(List<Device> devices) {
        mDevices = devices;
    }

    public void updateDevices(List<Device> devices) {
            for(Device device : devices){
                final int i = mDevices.indexOf(device);
                if(i >= 0){
                    mDevices.remove(i);
                    mDevices.add(i, device);
                } else {
                    mDevices.add(device);
                }
            }
    }

}
