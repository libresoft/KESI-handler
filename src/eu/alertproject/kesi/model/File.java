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

package eu.alertproject.kesi.model;

import java.net.URI;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

public class File extends Entity {

    private String fileID;
    private URI filePath;
    private int commitID;
    private Metrics metrics;
    private ArrayList<Module> modules;

    public File() {}

    public File(String fileID, int commitID) {
        this.fileID = fileID;
        this.commitID = commitID;
        this.metrics = null;
        this.modules = new ArrayList<Module>();
    }

    @XmlElement()
    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    @XmlElement()
    public URI getFilePath() {
        return filePath;
    }

    public void setFilePath(URI filePath) {
        this.filePath = filePath;
    }

    @XmlElement()
    public int getCommitID() {
        return commitID;
    }

    public void setRevisionID(int commitID) {
        this.commitID = commitID;
    }

    @XmlElement()
    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    @XmlElement()
    public ArrayList<Module> getModules() {
        return modules;
    }

    public void addModule(Module module) {
        modules.add(module);
    }
}
