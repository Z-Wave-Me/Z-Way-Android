/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 15.11.14 12:36.
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

package me.z_wave.android.network.portScan;

import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import me.z_wave.android.ui.fragments.NetworkScanFragment;

public class NetworkScanTask extends AsyncTask<Void, String, Void> {


    private final String TAG = "DefaultDiscovery";
    private final static int TIMEOUT_SCAN = 3600; // seconds
    private final static int TIMEOUT_SHUTDOWN = 10; // seconds
    private final static int THREADS = 10;

    final protected WeakReference<NetworkScanFragment> mDiscover;

    private int hosts_done = 0;
    private ExecutorService mPool;
    private int pt_move = 2;
    private int mPort;

    protected long ip;
    protected long start = 0;
    protected long end = 0;
    protected long size = 0;

    public NetworkScanTask(NetworkScanFragment discover, int port) {
        this.mDiscover = new WeakReference<NetworkScanFragment>(discover);
        mPort = port;
    }

    public void setNetwork(long ip, long start, long end) {
        this.ip = ip;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void onPreExecute() {
        size = (int) (end - start + 1);
        if (mDiscover != null) {
            final NetworkScanFragment discover = mDiscover.get();
            if (discover != null) {
//                discover.lockScanBtn(true);
            }
        }
    }

    @Override
    protected void onProgressUpdate(String... host) {
        if (mDiscover != null) {
            final NetworkScanFragment discover = mDiscover.get();
            if (discover != null) {
                if (!isCancelled()) {
                    if (host[0] != null) {
                        discover.addHost(host[0]);
                    }
                    discover.updateScanProgress(hosts_done, size);
                }

            }
        }
    }

    @Override
    protected void onPostExecute(Void unused) {
        if (mDiscover != null) {
            final NetworkScanFragment discover = mDiscover.get();
            if (discover != null) {
                discover.stopDiscovering();
            }
        }
    }

    @Override
    protected void onCancelled() {
        if (mPool != null) {
            synchronized (mPool) {
                mPool.shutdownNow();
            }
        }
        if (mDiscover != null) {
            final NetworkScanFragment discover = mDiscover.get();
            if (discover != null) {
                discover.stopDiscovering();
            }
        }
        super.onCancelled();
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (mDiscover != null) {
            final NetworkScanFragment discover = mDiscover.get();
            if (discover != null) {
                Log.v(TAG, "start=" + NetInfo.getIpFromLongUnsigned(start) + " (" + start
                        + "), end=" + NetInfo.getIpFromLongUnsigned(end) + " (" + end
                        + "), length=" + size);
                mPool = Executors.newFixedThreadPool(THREADS);
                if (ip <= end && ip >= start) {
                    Log.i(TAG, "Back and forth scanning");
                    // gateway
                    launch(start);

                    // hosts
                    long pt_backward = ip;
                    long pt_forward = ip + 1;
                    long size_hosts = size - 1;

                    for (int i = 0; i < size_hosts; i++) {
                        // Set pointer if of limits
                        if (pt_backward <= start) {
                            pt_move = 2;
                        } else if (pt_forward > end) {
                            pt_move = 1;
                        }
                        // Move back and forth
                        if (pt_move == 1) {
                            launch(pt_backward);
                            pt_backward--;
                            pt_move = 2;
                        } else if (pt_move == 2) {
                            launch(pt_forward);
                            pt_forward++;
                            pt_move = 1;
                        }
                    }
                } else {
                    Log.i(TAG, "Sequencial scanning");
                    for (long i = start; i <= end; i++) {
                        launch(i);
                    }
                }
                mPool.shutdown();
                try {
                    if(!mPool.awaitTermination(TIMEOUT_SCAN, TimeUnit.SECONDS)){
                        mPool.shutdownNow();
                        Log.e(TAG, "Shutting down pool");
                        if(!mPool.awaitTermination(TIMEOUT_SHUTDOWN, TimeUnit.SECONDS)){
                            Log.e(TAG, "Pool did not terminate");
                        }
                    }
                } catch (InterruptedException e){
                    Log.e(TAG, e.getMessage());
                    mPool.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
        return null;
    }

    private void launch(long i) {
        if(!mPool.isShutdown()) {
            mPool.execute(new CheckRunnable(NetInfo.getIpFromLongUnsigned(i)));
        }
    }

    private class CheckRunnable implements Runnable {
        private String addr;

        CheckRunnable(String addr) {
            this.addr = addr;
        }

        public void run() {
            if(isCancelled()) {
                publish(null);
            }
            Log.e(TAG, "run=" + addr);
            final String host = addr;

            try {
//                if((new InetSocketAddress(String.valueOf(addr), mPort)).getAddress().isReachable(100)) {
//                    Log.v(NetworkScanFragment.class.getSimpleName(), addr + ".get() connect success!!!");
//                    publish(host);
//                } else {
//                    Log.v(NetworkScanFragment.class.getSimpleName(), addr + " connect filed");
//                    publish(null);
//                }
                final Socket socket = new Socket();
                socket.connect(new InetSocketAddress(String.valueOf(addr), mPort), 100);
                socket.close();
                Log.v(NetworkScanFragment.class.getSimpleName(), addr + " connect success!!!");
                publish(host);
            } catch (Exception ex) {
                Log.v(NetworkScanFragment.class.getSimpleName(), addr + " connect filed");
                publish(null);
            }
        }
    }

    private void publish(final String host) {
        hosts_done++;
        publishProgress(host);

    }
}
