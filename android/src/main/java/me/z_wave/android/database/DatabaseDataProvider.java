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

    private DatabaseHelper mDatabaseHelper;


    public DatabaseDataProvider(Context context) {
        mDatabaseHelper = new DatabaseHelper(context);
    }

    public void addLocalProfile(LocalProfile localProfile) {
        final ContentValues initialValues = ProfileTable.createContentValues(localProfile);
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.insert(ProfileTable.TABLE_NAME, null, initialValues);
        database.close();
    }

    public void removeLocalProfile(LocalProfile localProfile) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.delete(ProfileTable.TABLE_NAME, BaseColumns._ID + "=" + localProfile.id, null);
        database.close();
    }

    public void updateLocalProfile(LocalProfile localProfile) {
        final ContentValues updateValues = ProfileTable.createContentValues(localProfile);
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.update(ProfileTable.TABLE_NAME, updateValues, BaseColumns._ID + "="
                + localProfile.id, null);
        database.close();
    }

    public List<LocalProfile> getLocalProfiles() {
        final List<LocalProfile> profiles = new ArrayList<LocalProfile>();
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        final Cursor cursor = database.query(ProfileTable.TABLE_NAME, null,
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
        database.close();
        return profiles;
    }

    public LocalProfile getLocalProfileWithId(int id) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        final Cursor cursor = database.query(ProfileTable.TABLE_NAME, null,
                BaseColumns._ID + "=" + id, null, null, null, null, "1");
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                final LocalProfile localProfile = new LocalProfile(cursor);
                cursor.close();
                database.close();
                return localProfile;
            }
            cursor.close();
        }
        database.close();
        return null;
    }

    public LocalProfile getActiveLocalProfile() {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        final Cursor cursor = database.query(ProfileTable.TABLE_NAME, null,
                ProfileTable.P_ACTIVE + "=1", null, null, null, "1");
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                LocalProfile profile = new LocalProfile(cursor);
                cursor.close();
                database.close();
                return profile;
            }
            cursor.close();
        }
        database.close();
        return null;
    }

}
