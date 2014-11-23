/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 11.10.14 13:22.
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

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import me.z_wave.android.app.ZWayApplication;
import me.z_wave.android.data.DataContext;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.MainThreadBus;
import me.z_wave.android.otto.events.AccountChangedEvent;
import timber.log.Timber;

/**
 * Created by Ivan Pl on 11.10.2014.
 */
public abstract class BaseUpdateDataService extends Service {

    public static final int UPDATE_TIME = 1000; //15 sec

    private final IBinder mBinder = new LocalBinder();

    @Inject
    MainThreadBus bus;

    @Inject
    ApiClient apiClient;

    @Inject
    DataContext dataContext;

    private Timer mTimer;

    public abstract void onUpdateData();
    public abstract void onAccountChanged(AccountChangedEvent event);

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
        startDataUpdates();
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

    protected void onRestart(){
        if (mTimer != null) mTimer.cancel();
        startDataUpdates();
    }

    protected void startDataUpdates() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 0, UPDATE_TIME);
    }

    private void update() {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(apiClient.isPrepared()) {
                   onUpdateData();
                }
            }
        });
        thread.start();
    }

    public class LocalBinder extends Binder {
    }

}
