/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 25.10.14 12:19.
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

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Device;
import me.z_wave.android.dataModel.DeviceRgbColor;
import me.z_wave.android.servises.UpdateDeviceService;

/**
 * Created by Ivan Pl on 25.10.2014.
 */
public class ColorPickerDialog extends BaseDialogFragment implements View.OnClickListener {

    public static final String KEY_COLOR = "key_color";

    private ColorPicker mColorPicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_color_picker, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewById(R.id.color_picker_cancel).setOnClickListener(this);
        findViewById(R.id.color_picker_ok).setOnClickListener(this);
        getDialog().setTitle(R.string.dialog_color_picker_select_color);

        prepareColorPicker();
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if(v.getId() == R.id.color_picker_ok) {
            final int color = mColorPicker.getColor();
            onColorPicked(color);
        }
    }

    public void setOldColor(int color) {
        Bundle args = getArguments();
        if(args == null) {
            args = new Bundle();
            setArguments(args);
        }
        args.putInt(KEY_COLOR, color);
    }

    public void onColorPicked(int color) {

    }

    private void prepareColorPicker() {
        mColorPicker = (ColorPicker) findViewById(R.id.color_picker);
        final SVBar svBar = (SVBar) findViewById(R.id.color_picker_svbar);
        mColorPicker.setShowOldCenterColor(true);
        mColorPicker.addSVBar(svBar);

        final int oldColor = getArguments() != null ? getArguments().getInt(KEY_COLOR) : Color.BLACK;
        mColorPicker.setOldCenterColor(oldColor);
    }
}
