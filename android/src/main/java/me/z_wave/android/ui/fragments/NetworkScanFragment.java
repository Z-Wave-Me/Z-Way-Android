/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 15.11.14 12:37.
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

import android.os.Bundle;
import android.text.TextUtils;

import java.util.ArrayList;

import me.z_wave.android.R;
import me.z_wave.android.network.portScan.NetworkScanTask;
import me.z_wave.android.network.portScan.NetInfo;
import me.z_wave.android.otto.events.ShowAttentionDialogEvent;
import me.z_wave.android.otto.events.ShowDialogEvent;
import me.z_wave.android.ui.dialogs.NetworkScanProgressDialog;
import me.z_wave.android.ui.dialogs.SelectUrlDialog;

/**
 * Created by Ivan Pl on 15.11.2014.
 */
public class NetworkScanFragment extends BaseFragment {

    public static final int PORT = 8083;

    private NetworkScanTask mNetworkScanTask;
    private NetworkScanProgressDialog mProgressDialog;
    private ArrayList<String> mFoundUrls;
    private long mNetworkIp = 0;
    private long mNetworkStart = 0;
    private long mNetworkEnd = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareNetworkInfo();
    }

    public void startDiscovering() {
        mProgressDialog = NetworkScanProgressDialog.newInstance(mNetworkEnd - mNetworkStart);
        bus.post(new ShowDialogEvent(mProgressDialog));

        mFoundUrls = new ArrayList<String>();
        mNetworkScanTask = new NetworkScanTask(this, PORT);
        mNetworkScanTask.setNetwork(mNetworkIp, mNetworkStart, mNetworkEnd);
        mNetworkScanTask.execute();
    }

    public void stopDiscovering() {
        if (isAdded()) {
            mNetworkScanTask = null;
            if(mProgressDialog != null) {
                mProgressDialog.dismissAllowingStateLoss();
                mProgressDialog = null;
            }

            if (mFoundUrls != null && mFoundUrls.size() > 0) {
                final SelectUrlDialog dialog = prepareSelectUrlDialog();
                bus.post(new ShowDialogEvent(dialog));
            } else {
                bus.post(new ShowAttentionDialogEvent(getString(R.string.empty_msg)));
            }
        }
    }

    public void updateScanProgress(int done, long total) {
        if (mProgressDialog != null && mProgressDialog.isVisible()) {
            mProgressDialog.updateProgress(done);
        }
    }

    public void addHost(String host) {
        if (!TextUtils.isEmpty(host)) {
            mFoundUrls.add(host);
        }
    }

    private void prepareNetworkInfo() {
        final NetInfo net = new NetInfo(getActivity());
        mNetworkIp = NetInfo.getUnsignedLongFromIp(net.ip);

        // Detected IP
        int shift = (32 - net.cidr);
        if (net.cidr < 31) {
            mNetworkStart = (mNetworkIp >> shift << shift) + 1;
            mNetworkEnd = (mNetworkStart | ((1 << shift) - 1)) - 1;
        } else {
            mNetworkStart = (mNetworkIp >> shift << shift);
            mNetworkEnd = (mNetworkStart | ((1 << shift) - 1));
        }
    }

    protected void onUrlSelected(String url) {

    }

    private SelectUrlDialog prepareSelectUrlDialog() {
        final SelectUrlDialog dialog = new SelectUrlDialog() {
            @Override
            public void onUrlSelected(String url) {
                NetworkScanFragment.this.onUrlSelected(url);
            }
        };
        dialog.setUrlList(mFoundUrls);
        return dialog;
    }

}
