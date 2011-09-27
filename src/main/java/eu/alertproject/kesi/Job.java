/*
 * Copyright (C) 2011 GSyC/LibreSoft
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Authors: Santiago Dueñas <sduenas@libresoft.es>
 *
 */

package eu.alertproject.kesi;

public class Job {
    /* Job types */
    public static final String ITS_JOB_TYPE = "ITS";
    public static final String SCM_JOB_TYPE = "SCM";

    private String type;
    private String url;
    private String id;
    private boolean startUp;

    public Job(String type, String url, String id) {
        this.type = type;
        this.url = url;
        this.id = id;
        this.startUp = false;
    }

    public String getType() {
        return type;
    }

    public String getURL() {
        return url;
    }

    public String getID() {
        return id;
    }

    public boolean isSetToStartUp() {
        return startUp;
    }

    public void setStartUp(boolean startUp) {
        this.startUp = startUp;
    }
}
