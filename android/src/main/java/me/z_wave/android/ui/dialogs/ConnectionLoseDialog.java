/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 10.07.14 14:44.
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

package me.z_wave.android.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import me.z_wave.android.R;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.MainThreadBus;
import me.z_wave.android.otto.events.CloseAppEvent;
import me.z_wave.android.otto.events.ShowNetworkSettingsEvent;
import me.z_wave.android.otto.events.ShowReconnectionProgressEvent;

public class ConnectionLoseDialog extends BaseDialogFragment implements View.OnClickListener {

    @Inject
    public MainThreadBus bus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_connection_lose, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().findViewById(R.id.connection_lose_close_app).setOnClickListener(this);
        getView().findViewById(R.id.connection_lose_open_settings).setOnClickListener(this);
        setCancelable(false);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.connection_lose_close_app) {
            bus.post(new CloseAppEvent());
        } else if(view.getId() == R.id.connection_lose_open_settings) {
            bus.post(new ShowNetworkSettingsEvent());
        }
    }

}