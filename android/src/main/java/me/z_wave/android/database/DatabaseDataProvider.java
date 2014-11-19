/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 07.07.14 18:55.
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

package me.z_wave.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.database.tables.ProfileTable;

/**
 * Created by Ivan PL on 07.07.2014.
 */
public class DatabaseDataProvider {

    private SQLiteDatabase mDatabase;
    private static DatabaseDataProvider mInstance;

    public static DatabaseDataProvider getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new DatabaseDataProvider(context);
        }
        return mInstance;
    }

    private DatabaseDataProvider(Context context) {
        final DatabaseHelper databaseHelper = new DatabaseHelper(context);
        mDatabase = databaseHelper.getReadableDatabase();
    }

    public long addLocalProfile(LocalProfile localProfile) {
        final ContentValues initialValues = ProfileTable.createContentValues(localProfile);
        long profileId = mDatabase.insert(ProfileTable.TABLE_NAME, null, initialValues);
        return profileId;
    }

    public void removeLocalProfile(LocalProfile localProfile) {
        mDatabase.delete(ProfileTable.TABLE_NAME, BaseColumns._ID + "=" + localProfile.id, null);
    }

    public void updateLocalProfile(LocalProfile localProfile) {
        final ContentValues updateValues = ProfileTable.createContentValues(localProfile);
        mDatabase.update(ProfileTable.TABLE_NAME, updateValues, BaseColumns._ID + "="
                + localProfile.id, null);
    }

    public List<LocalProfile> getLocalProfiles() {
        final List<LocalProfile> profiles = new ArrayList<LocalProfile>();
        final Cursor cursor = mDatabase.query(ProfileTable.TABLE_NAME, null,
                null, null, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    final LocalProfile profile = new LocalProfile(cursor);
                    profiles.add(profile);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return profiles;
    }

    public LocalProfile getLocalProfileWithId(int id) {
        final Cursor cursor = mDatabase.query(ProfileTable.TABLE_NAME, null,
                BaseColumns._ID + "=" + id, null, null, null, null, "1");
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                final LocalProfile localProfile = new LocalProfile(cursor);
                cursor.close();
                return localProfile;
            }
            cursor.close();
        }
        return null;
    }

    public LocalProfile getActiveLocalProfile() {
        final Cursor cursor = mDatabase.query(ProfileTable.TABLE_NAME, null,
                ProfileTable.P_ACTIVE + "='1'", null, null, null, null,"1");
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                LocalProfile profile = new LocalProfile(cursor);
                cursor.close();
                return profile;
            }
            cursor.close();
        }
        return null;
    }

    public LocalProfile getNearestLocalProfile(double userLat, double userLng) {
        final List<LocalProfile> localProfiles = getLocalProfiles();
        LocalProfile nearestProfile = null;
        double nearestDist = 1000000;
        for(LocalProfile localProfile : localProfiles) {
            double latDistance = Math.toRadians(userLat - localProfile.latitude);
            double lngDistance = Math.toRadians(userLng - localProfile.longitude);
            double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
                    (Math.cos(Math.toRadians(userLat))) *
                            (Math.cos(Math.toRadians(localProfile.latitude))) *
                            (Math.sin(lngDistance / 2)) *
                            (Math.sin(lngDistance / 2));

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            double dist = 6371 * c;
            if (dist <= 2) {
                if(dist < nearestDist) {
                    nearestProfile = localProfile;
                    nearestDist = dist;
                }
            }
        }
        return nearestProfile;
    }

}
