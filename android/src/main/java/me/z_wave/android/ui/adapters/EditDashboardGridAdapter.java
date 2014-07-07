/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 02.07.14 15:42.
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;

public class EditDashboardGridAdapter extends ArrayAdapter<Device> {

    public interface EditDashboardListener{
        void onDeleteDevice(Device device);
        void onRearrangeStarted(View item, int position);
    }

    private EditDashboardListener mListener;

    public EditDashboardGridAdapter(Context context, List<Device> objects, EditDashboardListener listener) {
        super(context, 0, objects);
        mListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_edit_dashboard_fragment, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }

        final ViewHolder holder = (ViewHolder) convertView.getTag();
        final Device device = getItem(position);

        holder.name.setText(device.metrics.title);
        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDeleteDevice(device);
            }
        });

        holder.btnRearrange.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mListener.onRearrangeStarted(holder.parent, position);
                return false;
            }
        });

        if(device.isIconLink()){
            Picasso.with(getContext()).load(device.metrics.icon).into(holder.icon);
        } else {
            if(device.getIconId() == 0){
                holder.icon.setImageDrawable(null);
            } else {
                holder.icon.setImageResource(device.getIconId());
            }
        }

        return convertView;
    }

    private class ViewHolder{

        public View parent;

        public View btnRemove;
        public View btnRearrange;
        public ImageView icon;
        public TextView name;

        public ViewHolder(View parent){
            this.parent = parent;
            btnRemove = parent.findViewById(R.id.edit_device_grid_item_remove);
            btnRearrange = parent.findViewById(R.id.edit_device_grid_item_rearrange);
            icon = (ImageView) parent.findViewById(R.id.edit_device_grid_item_icon);
            name = (TextView) parent.findViewById(R.id.edit_device_grid_item_name);
        }

    }
}
