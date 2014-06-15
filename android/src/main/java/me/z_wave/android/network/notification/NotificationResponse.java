/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 15.06.14 22:54.
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

package me.z_wave.android.network.notification;

public class NotificationResponse {

    public String error;
    public NotificationDataWrapper data;

//    {"error":null,
//      "data":{
//          "updateTime":1387884437,
//          "notifications":[
//              {"id":"1387199352223","timestamp":"2013-12-16T13:09:12.223Z","level":"error","message":"Cannot remove location 54545 - doesn't exist","redeemed":true},
//              {"id":"1387200419730","timestamp":"2013-12-16T13:26:59.730Z","level":"error","message":"Cannot remove location 54545 - doesn't exist","redeemed":true}
//          ]
//      }
//    }

}
