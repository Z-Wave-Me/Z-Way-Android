/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 14.06.14 20:57.
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

package me.z_wave.android.servises;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.squareup.otto.Bus;
import me.z_wave.android.app.ZWayApplication;
import me.z_wave.android.data.DataContext;
import me.z_wave.android.dataModel.DevicesStatus;
import me.z_wave.android.dataModel.Location;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.OnDataUpdatedEvent;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DataUpdateService extends Service {

    private final IBinder mBinder = new LocalBinder();

    @Inject
    Bus bus;

    @Inject
    DataContext dataContext;

    private long mLastUpdateTime;
    private Timer mTimer;
    private ApiClient mApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.v("On start");
        ((ZWayApplication)getApplication()).inject(this);
        bus.register(this);

        mApiClient = new ApiClient(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.v("On start command");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.v("On bind");
        startDevicesUpdates();
        requestLocations();
        requestProfiles();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Timber.v("On unbind");
        mTimer.cancel();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.v("On destroy");
        bus.unregister(this);
    }

    private void startDevicesUpdates() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateDevices();
            }
        }, 0, 5000);
    }

    private void updateDevices() {
        mApiClient.getDevicesState(mLastUpdateTime, new ApiClient.ApiCallback<DevicesStatus, Long>() {

            @Override
            public void onSuccess(DevicesStatus result) {
                Timber.v("Device updated!", result);
                mLastUpdateTime = result.updateTime;
                if (result.devices != null && !result.devices.isEmpty()) {
                    dataContext.addDevices(result.devices);
                    bus.post(new OnDataUpdatedEvent());
                }
            }

            @Override
            public void onFailure(Long request, boolean isNetworkError) {
                if (isNetworkError) {
                    Timber.v("Device update filed! Something wrong with network connection.");
                } else {
                    Timber.v("Device update filed! Something wrong with server.");
                }
            }
        });
    }

    public void requestLocations(){
        mApiClient.getLocations(new ApiClient.ApiCallback<List<Location>, String>() {
            @Override
            public void onSuccess(List<Location> result) {
                Timber.v(result.toString());
                dataContext.setLocations(result);
                bus.post(new OnDataUpdatedEvent());
            }

            @Override
            public void onFailure(String request, boolean isNetworkError) {
                    if(isNetworkError){
                        Timber.v("Request Location update filed! Something wrong with network connection.");
                    } else {
                        Timber.v("Request Location update filed! Something wrong with server.");
                    }
            }
        });
    }

    public void requestProfiles(){
        mApiClient.getProfiles(new ApiClient.ApiCallback<List<Profile>, String>() {
            @Override
            public void onSuccess(List<Profile> result) {
                Timber.tag("sdfsdfsdfsdfs");
                Timber.v(result.toString());
                dataContext.addProfiles(result);
            }

            @Override
            public void onFailure(String request, boolean isNetworkError) {
                if(isNetworkError){
                    Timber.v("Request Profile update filed! Something wrong with network connection.");
                } else {
                    Timber.v("Request Profile update filed! Something wrong with server.");
                }
            }
        });
    }

    public class LocalBinder extends Binder {
    }


}
