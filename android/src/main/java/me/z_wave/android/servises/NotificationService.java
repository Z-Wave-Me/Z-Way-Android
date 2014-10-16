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
import me.z_wave.android.network.notification.NotificationResponse;
import me.z_wave.android.otto.MainThreadBus;
import me.z_wave.android.otto.events.AccountChangedEvent;
import me.z_wave.android.otto.events.OnGetNotificationEvent;
import me.z_wave.android.ui.activity.MainActivity;
import retrofit.RetrofitError;
import timber.log.Timber;

import javax.inject.Inject;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends BaseUpdateDataService {
    private long mLastUpdateTime;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startDataUpdates();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onUpdateData() {
        try{
            final NotificationResponse result =  apiClient.getNotifications(mLastUpdateTime);

            mLastUpdateTime = result.data.updateTime;
            if (result.data.notifications != null && !result.data.notifications.isEmpty()) {
                Timber.v("Notification updated! notifications count " + result.data.notifications.size());
                dataContext.addNotifications(result.data.notifications);
                showNotification(result.data.notifications.get(result.data.notifications.size() - 1));
                bus.post(new OnGetNotificationEvent());
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    @Override
    @Subscribe
    public void onAccountChanged(AccountChangedEvent event) {
        mLastUpdateTime = 0;
        onRestart();
    }

    private void showNotification(me.z_wave.android.dataModel.Notification notification) {
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
