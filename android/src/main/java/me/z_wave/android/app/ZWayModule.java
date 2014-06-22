/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 14.06.14 18:23.
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

package me.z_wave.android.app;

import com.squareup.otto.Bus;
import dagger.Module;
import dagger.Provides;
import me.z_wave.android.data.DataContext;
import me.z_wave.android.otto.MainThreadBus;
import me.z_wave.android.servises.DataUpdateService;
import me.z_wave.android.servises.NotificationService;
import me.z_wave.android.ui.activity.MainActivity;
import me.z_wave.android.ui.fragments.*;

import javax.inject.Singleton;

@Module(
        injects = {
                MainActivity.class,
                DashboardFragment.class,
                FiltersFragment.class,
                NotificationsFragment.class,
                ProfilesFragment.class,
                EditProfilesFragment.class,
                ProfileFragment.class,
                DevicesFragment.class,
                DataUpdateService.class,
                NotificationService.class
        },
        library = true,
        complete = false
)

public class ZWayModule {

    @Provides
    @Singleton
    DataContext provideDataContext() {
        return new DataContext();
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new MainThreadBus(new Bus());
    }
}
