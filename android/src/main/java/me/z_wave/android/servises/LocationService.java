/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 08.09.14 21:25.
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
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.squareup.otto.Subscribe;

import me.z_wave.android.R;
import me.z_wave.android.app.ZWayApplication;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.database.DatabaseDataProvider;
import me.z_wave.android.otto.events.AuthEvent;
import me.z_wave.android.otto.events.StartStopLocationListeningEvent;
import me.z_wave.android.ui.activity.MainActivity;
import timber.log.Timber;

/**
 * Created by Ivan PL on 08.09.2014.
 */
public class LocationService extends Service {

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    public static final String CHANGE_PROFILE_BY_LOCATION = "change_profile_by_location";

    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;
    public DatabaseDataProvider databaseDataProvider;

    @Override
    public void onCreate() {
        super.onCreate();
        ((ZWayApplication) getApplication()).inject(this);
        Timber.v("onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.v("onStartCommand");
        databaseDataProvider = DatabaseDataProvider.getInstance(getApplicationContext());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 100, listener);
        if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 100, listener);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe
    public void onStartStopLocationListening(StartStopLocationListeningEvent event) {

    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.v("onDestroy");
        locationManager.removeUpdates(listener);
    }

    @Subscribe
    public void onLoginSuccess(AuthEvent.Success event) {
        showChangeProfileNotification(event.profile);
    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location loc) {
            if (isBetterLocation(loc, previousBestLocation)) {
                Timber.v("onLocationChanged: %s %s", loc.getLatitude(), loc.getLongitude());
                if (isChangeProfileByLocationEnable()) {
                    final LocalProfile profile = databaseDataProvider.getNearestLocalProfile(
                            loc.getLatitude(), loc.getLongitude());
                    if (profile != null && !profile.active) {
                        AuthService.login(LocationService.this, profile);
                    }
                    previousBestLocation = loc;
                }
            }
        }

        public void onProviderDisabled(String provider) {
            Timber.v("onProviderDisabled");
        }


        public void onProviderEnabled(String provider) {
            Timber.v("onProviderEnabled");
        }


        public void onStatusChanged(String provider, int status, Bundle extras) {
            Timber.v("onStatusChanged " + provider + " " + status);
        }

    }

    private void showChangeProfileNotification(LocalProfile profile) {
        int notificationId = 001;
        Intent viewIntent = new Intent(this, MainActivity.class);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_home)
                        .setContentTitle(getString(R.string.profile_changed))
                        .setContentText(
                                String.format(
                                        getString(R.string.profile_changed_to_desc), profile.name))
                        .setContentIntent(viewPendingIntent);

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private boolean isChangeProfileByLocationEnable() {
        SharedPreferences prefs = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        return prefs.getBoolean(CHANGE_PROFILE_BY_LOCATION, false);
    }
}
