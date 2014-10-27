/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 25.05.14 22:33.
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import butterknife.ButterKnife;

import com.mobeta.android.dslv.DragSortListView;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Notification;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.OnGetNotificationEvent;
import me.z_wave.android.ui.adapters.NotificationsListAdapter;

public class NotificationsFragment extends BaseListFragment implements DragSortListView.RemoveListener, AbsListView.OnScrollListener {

    @Inject
    ApiClient apiClient;

    private NotificationsListAdapter mAdapter;
    private View mFooterView;
    private boolean mLoading;

    private int mNotificationsCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_notification, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFooterView = LayoutInflater.from(getActivity())
                .inflate(R.layout.layout_list_footer, null, false);
        prepareListView();
    }

    @Override
    public void remove(int which) {
        if (which < mAdapter.getCount()) {
            final Notification deletedNotification = mAdapter.getItem(which);
            markNotificationAsRedeemed(deletedNotification);
            mAdapter.remove(deletedNotification);
        }
    }

    @Subscribe
    public void onGetNotification(OnGetNotificationEvent event) {
        mAdapter.clear();
        mAdapter.addAll(dataContext.getNotifications());
    }

    private void prepareListView() {
        getListView().addFooterView(mFooterView, null, false);


        mAdapter = new NotificationsListAdapter(getActivity(), dataContext.getNotifications());
        ((DragSortListView) getListView()).setRemoveListener(this);
        setListAdapter(mAdapter);

        getListView().removeFooterView(mFooterView);
        getListView().setOnScrollListener(this);
//        {
//            @Override
//            public void onScrollStateChanged(AbsListView arg0, int arg1)
//            {
//                // nothing here
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
//            {
//                boolean lastItem = (firstVisibleItem + visibleItemCount == totalItemCount);
//                boolean moreRows = getListAdapter().getCount() < datasource.getSize();
//
//                if (!loading &&  lastItem && moreRows)
//                {
//                    loading = true;
//                    getListView().addFooterView(footerView, null, false);
//                    (new LoadNextPage()).execute("");
//                }
//            }
//        });
    }

    private void markNotificationAsRedeemed(final Notification notification) {
        notification.redeemed = true;
        apiClient.updateNotifications(notification, new ApiClient.EmptyApiCallback<String>() {
            @Override
            public void onSuccess() {
//                mAdapter.remove(notification);
//                bus.post(new OnGetNotificationEvent());
            }

            @Override
            public void onFailure(String request, boolean isNetworkError) {

            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        boolean lastItem = (firstVisibleItem + visibleItemCount == totalItemCount);
//        boolean moreRows = getListAdapter().getCount() < datasource.getSize();
//
//        if (!mLoading && lastItem && moreRows) {
//            mLoading = true;
//            getListView().addFooterView(mFooterView, null, false);
//            (new LoadNextPage()).execute("");
//        }
    }
}
