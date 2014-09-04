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

import android.app.DialogFragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import me.z_wave.android.R;
import me.z_wave.android.app.ZWayApplication;

public class BaseDialogFragment extends DialogFragment implements View.OnClickListener {

    public interface DialogOnClickListener{
        public void onClick(View view, BaseDialogFragment dialog);
    }

    private View mContentView;
    private View mBaseView;

    private DialogOnClickListener mPositiveButtonOnClickListener;
    private DialogOnClickListener mNegativeButtonOnClickListener;

//    @Inject
//    Bus bus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setContentView(initBaseDialogView(inflater));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((ZWayApplication) getActivity().getApplication()).inject(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //workaround for http://code.google.com/p/android/issues/detail?id=19917
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
    }

    @Override
    public void onResume() {
        super.onResume();
//        bus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
//        bus.unregister(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.dialog_positive_button:
                if(mPositiveButtonOnClickListener != null){
                    mPositiveButtonOnClickListener.onClick(view, this);
                }
                break;
            case R.id.dialog_negative_button:
                if(mNegativeButtonOnClickListener != null){
                    mNegativeButtonOnClickListener.onClick(view, this);
                }
                break;
        }

    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    private View initBaseDialogView(LayoutInflater layoutInflater){
        mBaseView = layoutInflater.inflate(R.layout.layout_dialog_base, null);
        setTitle(R.string.app_name);
        setIcon(R.drawable.ic_launcher);
        return mBaseView;
    }

    public void setTitle(int resId){
        setTitle(getString(resId));
    }

    public void setTitle(String title){
        final TextView titleView = (TextView) mBaseView.findViewById(R.id.dialog_title);
        titleView.setText(title);
    }

    public void setIcon(int iconResourceId){
        ((ImageView) mBaseView.findViewById(R.id.dialog_icon)).setImageResource(iconResourceId);
    }

    public void setIcon(Drawable drawable){
        ImageView icon = (ImageView) mBaseView.findViewById(R.id.dialog_icon);
        if(drawable != null){
            icon.setImageDrawable(drawable);
        } else {
            icon.setVisibility(View.GONE);
        }
    }

    public void setPositiveButton(int textId, DialogOnClickListener listener){
        setButton(R.id.dialog_positive_button, textId);
        mPositiveButtonOnClickListener = listener;
    }

    public void setNegativeButton(int textId, DialogOnClickListener listener){
        setButton(R.id.dialog_negative_button, textId);
        mNegativeButtonOnClickListener = listener;
    }

    public void setPositiveButton(String title, DialogOnClickListener listener){
        setButton(R.id.dialog_positive_button, title);
        mPositiveButtonOnClickListener = listener;
    }

    public void setNegativeButton(String title, DialogOnClickListener listener){
        setButton(R.id.dialog_negative_button, title);
        mNegativeButtonOnClickListener = listener;
    }

    public void setDialogBody(View body){
        mContentView = body;
        ((ViewGroup) mBaseView.findViewById(R.id.dialog_content)).addView(mContentView);
    }

    public void setDialogBody(int layoutId){
        mContentView = LayoutInflater.from(getActivity()).inflate(layoutId, null);
        ((ViewGroup) mBaseView.findViewById(R.id.dialog_content)).addView(mContentView);
    }

    public void showTitle(boolean show){
        int visibility = show ? View.VISIBLE : View.GONE;
        mBaseView.findViewById(R.id.dialog_title_container).setVisibility(visibility);
        mBaseView.findViewById(R.id.dialog_title_delimiter).setVisibility(visibility);
    }

    @Override
    public View getView() {
        return mContentView;
    }

    private void setButton(int id, int textId){
        setButton(id, getString(textId));
    }

    private void setButton(int id, String text){
        TextView button = (TextView) mBaseView.findViewById(id);
        button.setVisibility(View.VISIBLE);
        showButtonContainer();
        showButtonsDivider();
        button.setText(text);
        button.setOnClickListener(this);
    }

    private void showButtonContainer(){
        mBaseView.findViewById(R.id.dialog_buttons_container).setVisibility(View.VISIBLE);
    }

    private void showButtonsDivider(){
        if(mBaseView.findViewById(R.id.dialog_positive_button).getVisibility() == View.VISIBLE &&
                mBaseView.findViewById(R.id.dialog_negative_button).getVisibility() == View.VISIBLE){
            mBaseView.findViewById(R.id.dialog_button_divider).setVisibility(View.VISIBLE);
        }
    }

    public View findViewById(int viewId){
        return getView().findViewById(viewId);
    }

    public void setEnabledDialogButton(int id, boolean enabled) {
        mBaseView.findViewById(id).setEnabled(enabled);
    }
}
