/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 26.07.14 13:50.
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

package me.z_wave.android.ui.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import me.z_wave.android.R;
import me.z_wave.android.data.NewProfileContext;
import me.z_wave.android.dataModel.LocalProfile;
import me.z_wave.android.otto.events.ShowProgressEvent;

public class ChooseLocationFragment extends BaseFragment implements GoogleMap.OnMapClickListener {

    private GoogleMap googleMap;
    private Marker mSelectedPositionMarker;

    @Inject
    NewProfileContext profileContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_choose_location, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (googleMap == null) {
            googleMap =  ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        }

        centerMapOnMyLocation();
        googleMap.setOnMapClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        final MapFragment f = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        if (f != null)
            getFragmentManager().beginTransaction().remove(f).commit();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(mSelectedPositionMarker == null){
            mSelectedPositionMarker = createMarker(latLng);
        } else {
            mSelectedPositionMarker.setPosition(latLng);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_choose_location, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_done){
            if(mSelectedPositionMarker != null){
                bus.post(new ShowProgressEvent(true));
                final LocalProfile profile = profileContext.getProfile();
                LatLng position = mSelectedPositionMarker.getPosition();
                profile.latitude = position.latitude;
                profile.longitude = position.longitude;
                profile.address = getAddress(position);
                bus.post(new ShowProgressEvent(false));
                goBack();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private Marker createMarker(LatLng latLng){
        return googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Selected location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    private void centerMapOnMyLocation() {
        googleMap.setMyLocationEnabled(true);
        final Location location = googleMap.getMyLocation();
        if (location != null) {
            final LatLng currentPoint = new LatLng(location.getLatitude(),
                    location.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPoint, 12f));
        }
    }

    private String getAddress(LatLng position){
        final Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            final List<Address> addresses = geocoder.getFromLocation(position.latitude,
                    position.longitude, 1);
            final Address address = addresses.get(0);
            if(address != null){
                final StringBuilder builder = new StringBuilder();
                if(!TextUtils.isEmpty(address.getThoroughfare())){
                    builder.append(address.getThoroughfare());
                    if(!TextUtils.isEmpty(address.getSubThoroughfare()))
                        builder.append(" " + address.getSubThoroughfare());
                }
                appendIfNotEmpty(builder, address.getLocality());
                appendIfNotEmpty(builder, address.getAdminArea());
                appendIfNotEmpty(builder, address.getCountryName());
                return builder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void appendIfNotEmpty(StringBuilder builder, String substr) {
        if(!TextUtils.isEmpty(substr)){
            if(builder.length() > 0)
                builder.append(", ");
            builder.append(substr);
        }
    }
}
