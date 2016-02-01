/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 07.12.14 21:12.
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Theme;

/**
 * Created by Ivan Pl on 07.12.2014.
 */
public class ThemeListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;


    public ThemeListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public int getCount() {
        return Theme.values().length;
    }

    @Override
    public Theme getItem(int position) {
        return Theme.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.layout_theme_list_item, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }

        final ViewHolder holder = (ViewHolder) convertView.getTag();
        final Theme theme = getItem(position);
        final int themeColor = mContext.getResources().getColor(theme.getThemeColorId());
        holder.themeTitle.setText(theme.getThemeTitle(mContext));
        holder.themeColor.setBackgroundColor(themeColor);
        return convertView;
    }

    private class ViewHolder {
        public TextView themeTitle;
        public View themeColor;

        private ViewHolder(View parent) {
            themeTitle = (TextView) parent.findViewById(R.id.theme_title);
            themeColor = parent.findViewById(R.id.theme_color);
        }
    }
}
