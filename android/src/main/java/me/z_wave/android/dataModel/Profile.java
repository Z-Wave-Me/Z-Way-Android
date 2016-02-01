/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 22.06.14 19:48.
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

package me.z_wave.android.dataModel;

import android.database.Cursor;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.z_wave.android.database.tables.ServerProfileTable;

public class Profile {

    public int id;
    public String name;
    public String description;
    public List<String> dashboard;

    public Profile() {
    }

    public Profile(Cursor cursor) {
        id = cursor.getInt(cursor.getColumnIndex(ServerProfileTable.SP_SERVER_ID));
        name = cursor.getString(cursor.getColumnIndex(ServerProfileTable.SP_NAME));
        description = cursor.getString(cursor.getColumnIndex(ServerProfileTable.SP_DESCRIPTION));
        dashboard = new ArrayList<String>();

        final String savedPositions = cursor.getString(cursor.getColumnIndex(ServerProfileTable.SP_POSITIONS));
        if(!TextUtils.isEmpty(savedPositions)) {
            final String[] positionsArray = savedPositions.split(",");
            dashboard = Arrays.asList(positionsArray);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return id == profile.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }

}
