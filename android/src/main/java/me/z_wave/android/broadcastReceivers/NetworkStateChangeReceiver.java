/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 12.10.14 11:54.
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

package me.z_wave.android.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import me.z_wave.android.app.ZWayApplication;
import me.z_wave.android.otto.MainThreadBus;
import me.z_wave.android.otto.events.InternetConnectionChangeEvent;
import me.z_wave.android.utils.InternetConnectionUtils;

public class NetworkStateChangeReceiver extends BroadcastReceiver {

    @Inject
    MainThreadBus bus;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        ((ZWayApplication) context.getApplicationContext()).inject(this);
        bus.post(new InternetConnectionChangeEvent(InternetConnectionUtils.isOnline(context)));
    }

}
