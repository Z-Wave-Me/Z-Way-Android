/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 15.11.14 13:42.
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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import me.z_wave.android.R;
import me.z_wave.android.otto.MainThreadBus;
import me.z_wave.android.otto.events.CancelConnectionEvent;
import me.z_wave.android.otto.events.ShowReconnectionProgressEvent;

/**
 * Created by Ivan PL on 04.09.2014.
 */
public class NetworkScanProgressDialog extends BaseDialogFragment {

    public static final String KEY_MAX_VALUE = "progress_max_value";

    private ProgressBar mScanProgressBar;
    private TextView mScanProgressHint;
    private long mMaxValue;

    @Inject public MainThreadBus bus;

    public static NetworkScanProgressDialog newInstance(long max) {
        final NetworkScanProgressDialog dialog = new NetworkScanProgressDialog();
        final Bundle args = new Bundle();
        args.putLong(KEY_MAX_VALUE, max);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMaxValue = getArguments().getLong(KEY_MAX_VALUE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_network_scan_progress, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().setTitle(R.string.dialog_network_scan_scanning);
        mScanProgressBar = (ProgressBar) findViewById(R.id.scan_progress);
        mScanProgressHint = (TextView) findViewById(R.id.scan_progress_hint);
        setCancelable(false);
        updateProgress(0);

    }

    public void updateProgress(int progress) {
        final String progressHint = getString(R.string.dialog_network_scan_hint, progress, mMaxValue);
        final int progressInPercent = (int) (((double)progress / (double)mMaxValue) * 100);
        mScanProgressBar.setProgress(progressInPercent);
        mScanProgressHint.setText(progressHint);
    }

}
