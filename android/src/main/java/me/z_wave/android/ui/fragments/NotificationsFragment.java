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
import android.widget.Toast;

import butterknife.ButterKnife;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.mobeta.android.dslv.DragSortListView;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Notification;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.network.notification.NotificationDataWrapper;
import me.z_wave.android.otto.events.OnGetNotificationEvent;
import me.z_wave.android.ui.adapters.NotificationsListAdapter;
import timber.log.Timber;

public class NotificationsFragment extends BaseListFragment
        implements DragSortListView.RemoveListener, AbsListView.OnScrollListener
         {

    @Inject
    ApiClient apiClient;

    private NotificationsListAdapter mAdapter;
    private View mFooterView;
    private boolean mLoading;

    private int mNotificationsCount = -1;
    private int pageNum = 1;

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
                .inflate(R.layout.layout_list_footer, getListView(), false);
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

    private void prepareListView() {
        getListView().addFooterView(mFooterView, null, false);


        mAdapter = new NotificationsListAdapter(getActivity(), dataContext.getNotifications());
        ((SwipeListView) getListView()).setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onDismiss(int[] positions) {
                if (positions.length > 0 && positions[0] < mAdapter.getCount()) {
                    final Notification deletedNotification = mAdapter.getItem(positions[0]);
                    markNotificationAsRedeemed(deletedNotification);
                    mAdapter.remove(deletedNotification);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
        setListAdapter(mAdapter);

        getListView().removeFooterView(mFooterView);
        getListView().setOnScrollListener(this);
    }

    private void markNotificationAsRedeemed(final Notification notification) {
        notification.redeemed = true;
        apiClient.updateNotifications(notification, new ApiClient.EmptyApiCallback<String>() {
            @Override
            public void onSuccess() {
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
        boolean loadNext = mNotificationsCount < 0 || (totalItemCount - (firstVisibleItem + visibleItemCount) <= 3);
        boolean moreRows = mNotificationsCount < 0 || getListAdapter().getCount() < mNotificationsCount;

        if (!mLoading && moreRows && loadNext) {
            mLoading = true;
            getListView().addFooterView(mFooterView, null, false);
            apiClient.getNotificationPage(pageNum, new ApiClient.ApiCallback<NotificationDataWrapper,
                    String>() {
                @Override
                public void onSuccess(NotificationDataWrapper result) {
                    if(isAdded() && isVisible()) {
                        getListView().removeFooterView(mFooterView);
                        mLoading = false;

                        if (result.pager != null) {
                            mNotificationsCount = result.notificationsCount;
                            mAdapter.addAll(result.notifications);
                            pageNum++;
                        }
                    }
                }

                @Override
                public void onFailure(String request, boolean isNetworkError) {
                    getListView().removeFooterView(mFooterView);
                    mLoading = false;
                }
            });
        }
    }
}
