/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 14.12.14 13:18.
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

package me.z_wave.android.database.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.google.common.base.Joiner;

import me.z_wave.android.dataModel.Profile;

/**
 * Created by Ivan Pl on 14.12.2014.
 */
public class ServerProfileTable {

    public static final String TABLE_NAME = "ServerProfiles";

    public static final String SP_SERVER_ID ="SPServerId";
    public static final String SP_LOCAL_ID = "SPLocalId";
    public static final String SP_NAME = "SPName";
    public static final String SP_DESCRIPTION = "SPDescription";
    public static final String SP_POSITIONS = "SPositions";

    private static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
            + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + SP_SERVER_ID + " TEXT NOT NULL,"
            + SP_LOCAL_ID + " TEXT NOT NULL,"
            + SP_NAME + " TEXT,"
            + SP_DESCRIPTION + " TEXT,"
            + SP_POSITIONS + " TEXT,"
            + "UNIQUE (" + SP_SERVER_ID + "," + SP_LOCAL_ID +") ON CONFLICT REPLACE)";

    public static final String SQL_INSERT_BIG_DATA = "INSERT INTO " + TABLE_NAME
            + " (" + SP_SERVER_ID +", " + SP_LOCAL_ID + ", " + SP_NAME +", "
            + SP_DESCRIPTION + ", " + SP_POSITIONS + ") VALUES (?, ?, ?, ?, ?)";

    public static void createTable(SQLiteDatabase database){
        database.execSQL(SQL_CREATE);
    }

    public static void removeTable(SQLiteDatabase database){
        database.execSQL("DROP TABLE IF EXISTS '" + TABLE_NAME + "'");
    }

    public static ContentValues createContentValues(Profile profile, int localProfileId) {
        final ContentValues values = new ContentValues();
        values.put(SP_LOCAL_ID, localProfileId);
        values.put(SP_SERVER_ID, profile.id);
        values.put(SP_NAME, profile.name);
        values.put(SP_DESCRIPTION, profile.description);
        if(profile.positions != null) {
            values.put(SP_POSITIONS, Joiner.on(",").join(profile.positions));
        }
        return values;
    }

}
