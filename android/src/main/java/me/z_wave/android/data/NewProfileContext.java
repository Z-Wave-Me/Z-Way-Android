/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 28.07.14 21:56.
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

package me.z_wave.android.data;

import me.z_wave.android.dataModel.LocalProfile;

public class NewProfileContext {

    private LocalProfile mProfile;

    public void createNew(){
        mProfile = new LocalProfile();
    }

    public LocalProfile getProfile(){
        return mProfile;
    }

    public void setProfile(LocalProfile profile){
        mProfile = profile;
    }

    public void reset() {
        mProfile = null;
    }

    public boolean isEmpty() {
        return mProfile == null;
    }

}
