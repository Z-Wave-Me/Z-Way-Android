/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 16.11.14 0:54.
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

import java.util.ArrayList;
import java.util.List;

import me.z_wave.android.R;

/**
 * Created by Ivan Pl on 16.11.2014.
 */
public class SelectUrlDialog extends BaseDialogFragment {

    public static final String KEY_URLS = "key_urls";

    public static SelectUrlDialog newInstance(ArrayList<String> urls) {
        final SelectUrlDialog dialog = new SelectUrlDialog();
        final Bundle args = new Bundle();
        args.putStringArrayList(KEY_URLS, urls);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final List<String> urls = getArguments().getStringArrayList(KEY_URLS);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_select_url);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setItems(urls.toArray(new String[urls.size()]), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
                onUrlSelected(urls.get(which));
            }
        });
        return builder.create();
    }

    public void setUrlList(ArrayList<String> urls) {
        Bundle args = getArguments();
        if(args == null) {
            args = new Bundle();
            setArguments(args);
        }
        args.putStringArrayList(KEY_URLS, urls);
    }

    public void onUrlSelected(String url) {

    }

}
