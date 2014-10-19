/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 14.06.14 20:57.
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

import com.squareup.otto.Subscribe;

import me.z_wave.android.network.devices.DevicesStateResponse;
import me.z_wave.android.otto.events.AccountChangedEvent;
import me.z_wave.android.otto.events.OnDataUpdatedEvent;
import retrofit.RetrofitError;

public class DataUpdateService extends BaseUpdateDataService {

    private long mLastUpdateTime;

    @Override
    public void onUpdateData() {
        try{
            //TODO remove profiles and locations request in another plase
            dataContext.addProfiles(apiClient.getProfiles().data);
            dataContext.addLocations(apiClient.getLocations().data);

            final DevicesStateResponse devicesStateResponse = apiClient.getDevices(mLastUpdateTime);
            mLastUpdateTime = devicesStateResponse.data.updateTime;
            dataContext.addDevices(devicesStateResponse.data.devices);
            if(devicesStateResponse.data.devices.size() > 0)
                bus.post(new OnDataUpdatedEvent(devicesStateResponse.data.devices));
        } catch (RetrofitError e){
            e.printStackTrace();
        }
    }

    @Override
    @Subscribe
    public void onAccountChanged(AccountChangedEvent event) {
        mLastUpdateTime = 0;
        onRestart();
    }

}
