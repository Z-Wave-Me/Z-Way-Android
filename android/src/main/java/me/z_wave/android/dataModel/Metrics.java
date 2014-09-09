/*
 * Z-Way for Android is a UI for Z-Way server
 *
 * Created by Ivan Platonov on 28.05.14 18:28.
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

package me.z_wave.android.dataModel;

import java.io.Serializable;

public class Metrics implements Serializable {

    public String probeTitle;
    public String scaleTitle;
    public String level;
    public String title;
    public String iconBase;
    public String icon;
    public String mode;
    public DeviceRgbColor color;

    //Camera metrics
    public String url;
    public Boolean hasZoomIn;
    public Boolean hasZoomOut;
    public Boolean hasLeft;
    public Boolean hasRight;
    public Boolean hasUp;
    public Boolean hasDown;
//    public int hasOpen;
//    public int hasClose;

}
