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

import android.util.Log;

import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.Filter;
import me.z_wave.android.dataModel.Location;
import me.z_wave.android.dataModel.Notification;
import me.z_wave.android.dataModel.Profile;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;

public class DataContext {

    private List<Device> mDevices;
    private List<Location> mLocation;
    private List<Notification> mNotifications;
    private List<Profile> mProfiles;

    public DataContext() {
        mDevices = new ArrayList<Device>();
        mLocation = new ArrayList<Location>();
        mNotifications = new ArrayList<Notification>();
        mProfiles = new ArrayList<Profile>();
    }

    public void addNotifications(List<Notification> notifications) {
        Timber.v("Add " + notifications.size() + " notifications");
        if (mNotifications.isEmpty()) {
            mNotifications.addAll(notifications);
        } else {
            for (Notification notification : notifications) {
                final int i = mNotifications.indexOf(notification);
                if (i >= 0) {
                    try {
                        Timber.v("remove " + i + " of " + mNotifications.size());
                        mNotifications.remove(i);
                        mNotifications.add(i, notification);
                    } catch (IndexOutOfBoundsException e) {
                        //TODO Need to find the reason of this exception!
                        e.printStackTrace();
                    }
                } else {
                    mNotifications.add(notification);
                }
            }
        }
        Timber.v("Notifications count " + mProfiles.size());
        Timber.v("---------------------------");
    }

    public void addLocations(List<Location> locations) {
        Timber.v("Add " + locations.size() + " locations");
        if (mLocation.isEmpty()) {
            mLocation.addAll(locations);
        } else {
            for (Location location : locations) {
                final int i = mLocation.indexOf(location);
                if (i >= 0) {
                    mLocation.remove(i);
                    mLocation.add(i, location);
                } else {
                    mLocation.add(location);
                }
            }
        }
        Timber.v("Locations count " + mLocation.size());
        Timber.v("---------------------------");
    }

    public void addDevices(List<Device> devices) {
        Timber.v("Add " + devices.size() + " devices");
        for (Device device : devices) {
            if (!device.permanentlyHidden) {
                final int i = mDevices.indexOf(device);
                if (i >= 0) {
                    mDevices.remove(i);
                    mDevices.add(i, device);
                } else {
                    mDevices.add(device);
                }
            }
        }
        Timber.v("Devices count " + mDevices.size());
        Timber.v("---------------------------");
    }

    public void addProfiles(List<Profile> profiles) {
        Timber.v("Add " + profiles.size() + " profiles");
        if (mProfiles.isEmpty()) {
            mProfiles.addAll(profiles);
        } else {
            for (Profile profile : profiles) {
                final int i = mProfiles.indexOf(profile);
                if (i >= 0) {
                    mProfiles.remove(i);
                    mProfiles.add(i, profile);
                } else {
                    mProfiles.add(profile);
                }
            }
        }
        Timber.v("Profiles count " + mProfiles.size());
        Timber.v("---------------------------");
    }

    public List<String> getDeviceTypes() {
        final List<String> result = new ArrayList<String>();
        if (mDevices != null) {
            for (Device device : mDevices) {
                if (device != null && device.deviceType != null) {
                    final String deviceType = device.deviceType.toString();
                    if (!result.contains(deviceType))
                        result.add(deviceType);
                }
            }
        }
        return result;
    }

    public List<String> getDeviceTags() {
        final List<String> result = new ArrayList<String>();
        if (mDevices != null) {
            for (Device device : mDevices) {
                for (String tag : device.tags) {
                    if (!result.contains(tag))
                        result.add(tag);
                }
            }
        }
        return result;
    }

    public List<String> getLocationsNames() {
        final List<String> result = new ArrayList<String>();
        if (mLocation != null) {
            for (Location location : mLocation) {
                if (!result.contains(location.title))
                    result.add(location.title);
            }
        }
        return result;
    }

    public List<Location> getLocations() {
        return mLocation == null ? new ArrayList<Location>() : mLocation;
    }


    public List<Notification> getNotifications() {
        return mNotifications;
    }

    public List<Device> getDevicesWithType(String deviceType) {
        if (deviceType.equalsIgnoreCase(Filter.DEFAULT_FILTER))
            return mDevices;

        final ArrayList<Device> result = new ArrayList<Device>();
        for (Device device : mDevices) {
            if (device.deviceType.toString().equalsIgnoreCase(deviceType))
                result.add(device);
        }
        return result;
    }

    public List<Device> getDevicesWithTag(String deviceTag) {
        if (deviceTag.equalsIgnoreCase(Filter.DEFAULT_FILTER))
            return mDevices;

        final ArrayList<Device> result = new ArrayList<Device>();
        for (Device device : mDevices) {
            if (device.tags.contains(deviceTag))
                result.add(device);
        }
        return result;
    }

    public List<Device> getDevicesForLocation(String location) {
        if (location.equalsIgnoreCase(Filter.DEFAULT_FILTER))
            return mDevices;

        final ArrayList<Device> result = new ArrayList<Device>();
        for (Device device : mDevices) {
            if (device.location != null && device.location.equalsIgnoreCase(location))
                result.add(device);
        }
        return result;
    }

    public List<Device> getDashboardDevices() {
        final Profile profile = getActiveProfile();
        final List<Device> result = new ArrayList<Device>();

        if (profile != null && profile.positions != null && mDevices != null) {
            for (String position : profile.positions) {
                for (Device device : mDevices) {
                    if (device.id.equals(position)) {
                        result.add(device);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public List<Profile> getProfiles() {
        return mProfiles;
    }

    public Profile getProfileWithId(int id) {
        if (mProfiles != null) {
            for (Profile profile : mProfiles) {
                if (profile.id == id)
                    return profile;
            }
        }
        return null;
    }

    public Profile getActiveProfile() {
        if (mProfiles != null && mProfiles.size() > 0) {
            return mProfiles.get(0);
//            for(Profile profile : mProfiles){
//                if(profile.active)
//                    return profile;
//            }
        }
        return null;
    }

    public void clear() {
        Timber.v("Clear data context");
        clearList(mDevices);
        clearList(mLocation);
        clearList(mNotifications);
        clearList(mProfiles);
    }

    private void clearList(List list) {
        if (list != null)
            list.clear();
    }

}
