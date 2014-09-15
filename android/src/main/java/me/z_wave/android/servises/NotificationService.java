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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.squareup.otto.Subscribe;

import me.z_wave.android.R;
import me.z_wave.android.app.ZWayApplication;
import me.z_wave.android.data.DataContext;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.network.notification.NotificationDataWrapper;
import me.z_wave.android.otto.MainThreadBus;
import me.z_wave.android.otto.events.AccountChangedEvent;
import me.z_wave.android.otto.events.OnGetNotificationEvent;
import me.z_wave.android.ui.activity.MainActivity;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

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

    @Subscribe
    public void onAccountChanged(AccountChangedEvent event){
        mLastUpdateTime = 0;
        mTimer.cancel();
        startNotificationListening();
    }

    private void startNotificationListening() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                apiClient.getNotifications(mLastUpdateTime,
                        new ApiClient.ApiCallback<NotificationDataWrapper, Long>() {

                    @Override
                    public void onSuccess(NotificationDataWrapper result) {
                        mLastUpdateTime = result.updateTime;
                        if (result.notifications != null && !result.notifications.isEmpty()) {
                            Timber.v("Notification updated! notifications count " + result.notifications.size());
                            dataContext.addNotifications(result.notifications);
                            //TODO IVAN_PL should be refactored
                            showNotification(result.notifications.get(result.notifications.size()-1));
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

    private void showNotification(me.z_wave.android.dataModel.Notification notification){
        int notificationId = 001;
        Intent viewIntent = new Intent(this, MainActivity.class);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_attention)
                        .setContentTitle(getString(R.string.attention))
                        .setContentText(notification.message)
                        .setContentIntent(viewPendingIntent);

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

}
