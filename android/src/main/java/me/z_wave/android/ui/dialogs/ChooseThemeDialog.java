/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 07.12.14 20:47.
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Theme;
import me.z_wave.android.ui.adapters.ThemeListAdapter;

/**
 * Created by Ivan Pl on 07.12.2014.
 */
public class ChooseThemeDialog extends BaseDialogFragment implements AdapterView.OnItemClickListener {

    public static final String KEY_URLS = "key_urls";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_select_app_theme);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setView(createThemeList());
        return builder.create();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Theme theme = (Theme) parent.getAdapter().getItem(position);
        onThemeSelected(theme);
        dismiss();
    }

    public void setUrlList(ArrayList<String> urls) {
        Bundle args = getArguments();
        if(args == null) {
            args = new Bundle();
            setArguments(args);
        }
        args.putStringArrayList(KEY_URLS, urls);
    }

    public void onThemeSelected(Theme theme) {

    }

    private View createThemeList() {
        final ListView themesList = new ListView(getActivity());
        themesList.setDrawSelectorOnTop(true);
        final ThemeListAdapter adapter = new ThemeListAdapter(getActivity());
        themesList.setAdapter(adapter);
        themesList.setOnItemClickListener(this);
        return themesList;
    }
}
