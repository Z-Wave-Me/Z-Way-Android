/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 15.06.14 19:39.
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
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.network.notification.NotificationDataWrapper;
import me.z_wave.android.otto.events.OnGetNotificationEvent;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

    private final IBinder mBinder = new LocalBinder();

    @Inject
    Bus bus;

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
        if(mTimer == null)
            startNotificationListening();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.v("On bind");
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
        mTimer.cancel();
        bus.unregister(this);
    }

    private void startNotificationListening() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                ApiClient.getNotifications(mLastUpdateTime,
                        new ApiClient.ApiCallback<NotificationDataWrapper, Long>() {

                    @Override
                    public void onSuccess(NotificationDataWrapper result) {
                        Timber.v("Notification updated!", result.toString());
                        mLastUpdateTime = result.updateTime;
                        if (result.notifications != null && !result.notifications.isEmpty()) {
                            dataContext.addNotifications(result.notifications);
                            bus.post(new OnGetNotificationEvent());
                        }
                    }

                    @Override
                    public void onFailure(Long request, boolean isNetworkError) {
                        if (isNetworkError) {
                            Timber.v("Notification update filed! Something wrong with network connection.");
                        } else {
                            Timber.v("Notification update filed! Something wrong with server.");
                        }
                    }
                });
            }
        }, 0, 5000);
    }


    public class LocalBinder extends Binder {
    }


}
