/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 14.06.14 20:51.
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

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

public class BindHelper {

    public static interface OnServiceConnectedListener {
        public void onServiceConnected();
    }

    private static class BindInfo {
        public final Class<? extends Service> service;
        public final ServiceConnection connection;
        public final Bundle parameters;

        private BindInfo(Class<? extends Service> service, ServiceConnection connection, Bundle parameters) {
            this.service = service;
            this.connection = connection;
            this.parameters = parameters;
        }
    }

    private Set<BindInfo> mPairs = new HashSet<BindInfo>();

    public void keep(Class<? extends Service> ... services){
        for(Class<? extends Service> service: services){
            mPairs.add(new BindInfo(service, makeStubConnection(), null));
        }
    }

    public void keep(Class<? extends Service> service, Bundle parameters){
        mPairs.add(new BindInfo(service, makeStubConnection(), parameters));
    }

    public void keep(Class<? extends Service> service, OnServiceConnectedListener listener){
        mPairs.add(new BindInfo(service, makeStubConnection(listener), null));
    }

    public void onBind(Activity activity){
        for(BindInfo bindInfo : mPairs){
            final Intent intent = new Intent(activity, bindInfo.service);
            if(bindInfo.parameters != null){
                intent.putExtras(bindInfo.parameters);
            }
            activity.bindService(intent, bindInfo.connection, Context.BIND_AUTO_CREATE);
        }
    }

    public void onUnbind(Activity activity){
        for(BindInfo bindInfo : mPairs){
            try {
                activity.unbindService(bindInfo.connection);
            } catch (IllegalArgumentException e) {
                Timber.w(e, "service not registered!");
            }
        }
    }

    private ServiceConnection makeStubConnection(){
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
    }

    private ServiceConnection makeStubConnection(final OnServiceConnectedListener listener){
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                if(listener!=null) {
                    listener.onServiceConnected();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
    }

}
