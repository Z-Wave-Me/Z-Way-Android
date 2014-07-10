/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 10.07.14 14:44.
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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import me.z_wave.android.R;
import me.z_wave.android.otto.events.ShowAlertDialogEvent;

public class AlertDialog extends BaseDialogFragment{

    private AlertDialogBuilder mBuilder;

    public static class AlertDialogBuilder{
        private Context mContext;

        private Drawable mIcon;
        private String mTitle;
        private boolean mIsPositiveButtonEnabled = false;
        private String mPositiveBtnTitle;
        private DialogOnClickListener mPositiveButtonOnClickListener;
        private boolean mIsNegativeButtonEnabled = false;
        private String mNegativeBtnTitle;
        private DialogOnClickListener mNegativeButtonOnClickListener;
        private String mMessage;

        public AlertDialogBuilder(Context context){
            mContext = context;
        }

        public AlertDialogBuilder setTitle(String title){
            mTitle = TextUtils.isEmpty(title) ? null : title;
            return this;
        }

        public AlertDialogBuilder setTitle(int messageId){
            return setTitle(mContext.getString(messageId));
        }

        public AlertDialogBuilder setIcon(Drawable icon){
            mIcon = icon;
            return this;
        }

        public AlertDialogBuilder setIcon(int iconId){
            return setIcon(mContext.getResources().getDrawable(iconId));
        }

        public AlertDialogBuilder setPositiveButton(String btnTitle, DialogOnClickListener listener){
            mIsPositiveButtonEnabled = true;
            mPositiveBtnTitle = btnTitle;
            mPositiveButtonOnClickListener = listener;
            return this;
        }

        public AlertDialogBuilder setNegativeButton(String btnTitle, DialogOnClickListener listener){
            mIsNegativeButtonEnabled = true;
            mNegativeBtnTitle = btnTitle;
            mNegativeButtonOnClickListener = listener;
            return this;
        }

        public AlertDialogBuilder setPositiveButton(int titleId, DialogOnClickListener listener){
            return setPositiveButton(mContext.getString(titleId), listener);
        }

        public AlertDialogBuilder setNegativeButton(int titleId, DialogOnClickListener listener){
            return setNegativeButton(mContext.getString(titleId), listener);
        }

        public AlertDialogBuilder setMessage(String message){
            mMessage = message;
            return this;
        }

        public AlertDialogBuilder setMessage(int messageId){
            return setMessage(mContext.getString(messageId));
        }

        public AlertDialog build(){
            return new AlertDialog(this);
        }

        public AlertDialog build(ShowAlertDialogEvent event){
            setIcon(event.iconId);
            setTitle(event.title);
            setMessage(event.message);
            if(event.isPositiveButtonEnabled()){
                setPositiveButton(event.getPositiveBtnTitle(), event.getPositiveButtonOnClickListener());
            }
            if(event.isNegativeButtonEnabled()){
                setNegativeButton(event.getNegativeBtnTitle(), event.getNegativeButtonOnClickListener());
            }
            return new AlertDialog(this);
        }
    }

    public AlertDialog(AlertDialogBuilder builder){
        mBuilder = builder;
    }

    public AlertDialog(){

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mBuilder != null){
            setIcon(mBuilder.mIcon);
            setTitle(mBuilder.mTitle);
            if(mBuilder.mIsPositiveButtonEnabled){
                setPositiveButton(mBuilder.mPositiveBtnTitle, mBuilder.mPositiveButtonOnClickListener);
            }
            if(mBuilder.mIsNegativeButtonEnabled){
                setNegativeButton(mBuilder.mNegativeBtnTitle, mBuilder.mNegativeButtonOnClickListener);
            }

            setDialogBody(initAlertDialogBody());
        } else {
            dismiss();
        }
    }

    private View initAlertDialogBody(){
        View body = getActivity().getLayoutInflater().inflate(R.layout.layout_alert_dialog_body, null);
        if(mBuilder.mMessage != null){
            TextView message = (TextView) body.findViewById(R.id.alert_dialog_message);
            final String messageHTML = mBuilder.mMessage.replaceAll("\n","<br>");
            message.setText(Html.fromHtml(messageHTML));
            message. setMovementMethod(LinkMovementMethod.getInstance());
        }
        return body;
    }
}
