/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 10.07.14 14:45.
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

package me.z_wave.android.otto.events;

import me.z_wave.android.R;
import me.z_wave.android.ui.dialogs.BaseDialogFragment;

public class ShowAlertDialogEvent {

    public int iconId = R.drawable.ic_launcher;
    public String title = "";
    private boolean isPositiveButtonEnabled = false;
    private String positiveBtnTitle = "";
    private BaseDialogFragment.DialogOnClickListener positiveButtonOnClickListener;
    private boolean isNegativeButtonEnabled = false;
    private String negativeBtnTitle = "";
    private BaseDialogFragment.DialogOnClickListener negativeButtonOnClickListener;
    public final String message;

    public ShowAlertDialogEvent(String message) {
        this.message = message;
    }

    public void setPositiveBtn(String positiveBtnTitle, BaseDialogFragment.DialogOnClickListener positiveButtonOnClickListener){
        isPositiveButtonEnabled = true;
        this.positiveBtnTitle = positiveBtnTitle;
        this.positiveButtonOnClickListener = positiveButtonOnClickListener;
    }

    public void setNegativeBtn(String negativeBtnTitle, BaseDialogFragment.DialogOnClickListener negativeButtonOnClickListener){
        isNegativeButtonEnabled = true;
        this.negativeBtnTitle = negativeBtnTitle;
        this.negativeButtonOnClickListener = negativeButtonOnClickListener;
    }

    public boolean isPositiveButtonEnabled(){
        return isPositiveButtonEnabled;
    }

    public boolean isNegativeButtonEnabled(){
        return isNegativeButtonEnabled;
    }

    public String getPositiveBtnTitle(){
        return positiveBtnTitle;
    }

    public String getNegativeBtnTitle(){
        return negativeBtnTitle;
    }

    public BaseDialogFragment.DialogOnClickListener getPositiveButtonOnClickListener(){
        return positiveButtonOnClickListener;
    }

    public BaseDialogFragment.DialogOnClickListener getNegativeButtonOnClickListener(){
        return negativeButtonOnClickListener;
    }
}
