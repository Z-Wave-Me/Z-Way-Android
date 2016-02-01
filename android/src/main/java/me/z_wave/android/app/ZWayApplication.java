/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 31.05.14 13:20.
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

package me.z_wave.android.app;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

import dagger.ObjectGraph;
import me.z_wave.android.R;
import timber.log.Timber;

public class ZWayApplication extends Application {

    private ObjectGraph objectGraph;
    private HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    @Override
    public void onCreate() {
        super.onCreate();
        if (Constants.DEBUG_MODE) {
            Timber.plant(new Timber.DebugTree());
        }

        objectGraph = ObjectGraph.create(new ZWayModule());
    }

    public void inject(Object object) {
        objectGraph.inject(object);
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            final GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.enableAutoActivityReports(this);
            final Tracker t = analytics.newTracker(R.xml.global_tracker);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }

}
