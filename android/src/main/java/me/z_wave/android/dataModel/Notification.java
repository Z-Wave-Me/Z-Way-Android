/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 15.06.14 22:50.
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

public class Notification {

    public String id; //?
    public String timestamp;
    public String level;
    public String message;
    public String redeemed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Notification{" +
                "\nid='" + id + '\'' +
                ",\n timestamp='" + timestamp + '\'' +
                ",\n level='" + level + '\'' +
                ",\n message='" + message + '\'' +
                ",\n redeemed='" + redeemed + '\'' +
                " \n }";
    }

    //    {"id":"1387199352223",
//            "timestamp":"2013-12-16T13:09:12.223Z",
//            "level":"error",
//            "message":"Cannot remove location 54545 - doesn't exist",
//            "redeemed":true}

}
