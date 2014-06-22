/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 22.06.14 19:47.
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

package me.z_wave.android.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Profile;

/**
 * Created by Ivan PL on 22.06.2014.
 */
public class ProfilesListAdapter extends ArrayAdapter<Profile> {

    private boolean mIsEditMode;

    public ProfilesListAdapter(Context context, List<Profile> profiles, boolean isEditMode) {
        super(context, 0, profiles);
        mIsEditMode = isEditMode;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = View.inflate(getContext(), R.layout.layaut_profiles_list_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }

        final Profile profile = getItem(position);
        final ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.name.setText(profile.name);

        if(mIsEditMode){
            holder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.right_arrow, 0);
        } else {
            if(profile.active)
                holder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_checked, 0);
        }

        return convertView;
    }

    private class ViewHolder{
        public TextView name;

        private ViewHolder(View parent) {
            name = (TextView) parent.findViewById(R.id.profile_name);
        }
    }
}
