/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 15.06.14 16:21.
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

package me.z_wave.android.utils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

public class FragmentUtils {

    public static void commitFragment(FragmentManager fragmentManager, int containerId, Fragment fragment,
                                      boolean addToBackStack) {
        final String tag = ((Object)fragment).getClass().getSimpleName();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(containerId, fragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.commitAllowingStateLoss();
    }

    public static Fragment getFragmentByTag(FragmentManager fragmentManager, Class<?> fragment) {
        return fragmentManager.findFragmentByTag(fragment.getSimpleName());
    }

    public static void popBackStack(FragmentManager fragmentManager){
        fragmentManager.popBackStack();
    }

}
