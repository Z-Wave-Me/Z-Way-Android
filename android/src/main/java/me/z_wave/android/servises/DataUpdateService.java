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
import com.squareup.otto.Subscribe;

import me.z_wave.android.app.ZWayApplication;
import me.z_wave.android.data.DataContext;
import me.z_wave.android.dataModel.DevicesStatus;
import me.z_wave.android.dataModel.Location;
import me.z_wave.android.dataModel.Profile;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.network.devices.DevicesStateResponse;
import me.z_wave.android.otto.MainThreadBus;
import me.z_wave.android.otto.events.AccountChangedEvent;
import me.z_wave.android.otto.events.OnDataUpdatedEvent;
import me.z_wave.android.otto.events.ProgressEvent;
import retrofit.RetrofitError;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DataUpdateService extends Service {

    private final IBinder mBinder = new LocalBinder();

    @Inject
    MainThreadBus bus;

    @Inject
    ApiClient apiClient;

    @Inject
    DataContext dataContext;

    private long mLastUpdateTime;
    private Timer mTimer;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.v("On start");
        ((ZWayApplication)getApplication()).inject(this);
        bus.register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.v("On start command");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.v("On bind");
        requestAppData();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Timber.v("On unbind");
        if(mTimer != null)
            mTimer.cancel();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.v("On destroy");
        bus.unregister(this);
    }

    @Subscribe
    public void onAccountChanged(AccountChangedEvent event){
        mLastUpdateTime = 0;
        mTimer.cancel();
        requestAppData();
    }

    private void requestAppData() {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    List<Profile> profiles = apiClient.getProfiles().data;
                    List<Location> locations = apiClient.getLocations().data;
                    DevicesStateResponse devicesStateResponse = apiClient.getDevices();
                    mLastUpdateTime = devicesStateResponse.data.updateTime;

                    dataContext.setProfiles(profiles);
                    dataContext.setLocations(locations);
                    dataContext.setDevices(devicesStateResponse.data.devices);

                    bus.post(new OnDataUpdatedEvent());
                    startDevicesUpdates();
                    bus.post(new ProgressEvent(false, false));

                } catch (RetrofitError e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
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
        apiClient.getDevicesState(mLastUpdateTime, new ApiClient.ApiCallback<DevicesStatus, Long>() {

            @Override
            public void onSuccess(DevicesStatus result) {
                Timber.v("Device updated! Devices count " + result.devices.size());
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

    public class LocalBinder extends Binder {
    }


}
