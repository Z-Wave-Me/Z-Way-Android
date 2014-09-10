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
import android.widget.AdapterView;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import me.z_wave.android.R;
import me.z_wave.android.dataModel.Notification;
import me.z_wave.android.network.ApiClient;
import me.z_wave.android.otto.events.OnGetNotificationEvent;
import me.z_wave.android.ui.adapters.NotificationsListAdapter;

public class NotificationsFragment extends BaseFragment implements DragSortListView.RemoveListener {

    //TODO replace BaseFragment to BaseListFragment!

    @InjectView(R.id.notification_list)
    DragSortListView notificationList;

    @InjectView(R.id.notification_msg_ok)
    View everythingOkMsg;

    @Inject
    ApiClient apiClient;

    private NotificationsListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_notification, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareListView();
        changeEmptyDashboardMsgVisibility();
    }

    @Override
    public void remove(int which) {
        if(which < mAdapter.getCount()) {
            final Notification deletedNotification = mAdapter.getItem(which);
            markNotificationAsRedeemed(deletedNotification);
            mAdapter.remove(deletedNotification);
            changeEmptyDashboardMsgVisibility();
        }
    }

    @Subscribe
    public void onGetNotification(OnGetNotificationEvent event){
        mAdapter.clear();
        mAdapter.addAll(dataContext.getNotifications());
        changeEmptyDashboardMsgVisibility();
    }

    private void prepareListView(){
        mAdapter = new NotificationsListAdapter(getActivity(), dataContext.getNotifications());
        notificationList.setAdapter(mAdapter);
        notificationList.setRemoveListener(this);
    }

    private void changeEmptyDashboardMsgVisibility(){
        final int msgVisibility = mAdapter != null && mAdapter.getCount() > 0 ? View.GONE : View.VISIBLE;
        if(everythingOkMsg.getVisibility() != msgVisibility){
            everythingOkMsg.setVisibility(msgVisibility);
        }
    }

    private void markNotificationAsRedeemed(final Notification notification) {
        notification.redeemed = true;
        apiClient.updateNotifications(notification, new ApiClient.EmptyApiCallback<String>() {
            @Override
            public void onSuccess() {
                mAdapter.remove(notification);
                bus.post(new OnGetNotificationEvent());
            }

            @Override
            public void onFailure(String request, boolean isNetworkError) {

            }
        });
    }
}
