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

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import javax.inject.Inject;

import me.z_wave.android.app.ZWayApplication;
import me.z_wave.android.otto.MainThreadBus;

public class BaseDialogFragment extends DialogFragment {

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //workaround for http://code.google.com/p/android/issues/detail?id=19917
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
    }

    public View findViewById(int id) {
        return getView().findViewById(id);
    }

}
