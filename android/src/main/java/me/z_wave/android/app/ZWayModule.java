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
import me.z_wave.android.broadcastReceivers.NetworkStateChangeReceiver;
import me.z_wave.android.data.DataContext;
import me.z_wave.android.data.NewProfileContext;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.MainThreadBus;
import me.z_wave.android.servises.AuthService;
import me.z_wave.android.servises.DataUpdateService;
import me.z_wave.android.servises.LocationService;
import me.z_wave.android.servises.NotificationService;
import me.z_wave.android.servises.UpdateDeviceService;
import me.z_wave.android.ui.activity.CameraActivity;
import me.z_wave.android.ui.activity.MainActivity;
import me.z_wave.android.ui.activity.StartActivity;
import me.z_wave.android.ui.fragments.*;
import me.z_wave.android.ui.fragments.dashboard.DashboardFragment;
import me.z_wave.android.ui.fragments.dashboard.EditDashboardFragment;

import javax.inject.Singleton;

@Module(
        injects = {
                MainActivity.class,
                StartActivity.class,
                DashboardFragment.class,
                FiltersFragment.class,
                NotificationsFragment.class,
                ProfilesFragment.class,
                EditProfilesFragment.class,
                ProfileFragment.class,
                DevicesFragment.class,
                DataUpdateService.class,
                NotificationService.class,
                EditDashboardFragment.class,
                SplashFragment.class,
                ChooseLocationFragment.class,
                MainMenuFragment.class,
                EditDevicesFragment.class,
                LocationService.class,
                CameraActivity.class,
                BaseDeviceListFragment.class,
                NetworkStateChangeReceiver.class,
                AuthService.class,
                UpdateDeviceService.class,
                NetworkScanFragment.class
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
    MainThreadBus provideBus() {
        return new MainThreadBus(new Bus());
    }

    @Provides
    @Singleton
    ApiClient provideApiClient() {
        return new ApiClient();
    }

    @Provides
    @Singleton
    NewProfileContext provideNewProfileClient() {
        return new NewProfileContext();
    }

}
