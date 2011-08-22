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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

public class KESI {
    /* Preference keys */
    public static final String PREF_SENSOR_USERNAME = "username";
    public static final String PREF_SENSOR_PASSWORD = "password";
    public static final String PREF_SENSOR_PROTOCOL = "protocol";
    public static final String PREF_SENSOR_HOST = "host";
    public static final String PREF_SENSOR_PORT = "port";
    public static final String PREF_SENSOR_FOLDER = "folder";
    public static final String PREF_SENSOR_TIMEOUT = "timeout";
    public static final String PREF_SENSOR_POLL_INTERVAL = "polling";
    public static final String PREF_DB_USERNAME = "username";
    public static final String PREF_DB_PASSWORD = "password";
    public static final String PREF_DB_HOST = "host";
    public static final String PREF_DB_PORT = "port";
    public static final String PREF_DB_DATABASE_ITS = "database.its";
    public static final String PREF_DB_DATABASE_SCM = "database.scm";

    /* Preference default values */
    public static final String DEF_SENSOR_PROTOCOL = "imaps";
    public static final String DEF_SENSOR_FOLDER = "Inbox";
    public static final int DEF_SENSOR_TIMEOUT = 5000;
    public static final int DEF_SENSOR_POLL_INTERVAL = 10000;

    public static final String DEF_DB_DBMS = "mysql";
    public static final String DEF_DB_HOSTNAME = "localhost";
    public static final String DEF_DB_PORT = "3306";

    private JobsQueue scmJobs;
    private JobsQueue itsJobs;

    public void run(String cfg) {
        SensorHandler handler;
        ITSExtractor its;
        SCMExtractor scm;

        /*
         * TODO: queues should be singleton instances and shared by all
         * the components.
         */
        scmJobs = new JobsQueue();
        itsJobs = new JobsQueue();

        try {
            KnowledgeSourceManager.INSTANCE.loadKnowledgeSources();
            KnowledgeSourceManager.INSTANCE.startUp(scmJobs, itsJobs);
        } catch (PreferencesError e) {
            throw new RuntimeException(e);
        }

        scm = new SCMExtractor(scmJobs);
        scm.start();

        its = new ITSExtractor(itsJobs);
        its.start();

        handler = new SensorHandler(scmJobs, itsJobs);
        handler.start();

        System.out.println("Running KESI component");

        try {
            handler.join();
            its.join();
            scm.join();
            /* Something went really wrong if all threads are dead */
            throw new RuntimeException("Unexpected KESI behaviour");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadPreferences(String filename) throws IOException,
            InvalidPreferencesFormatException {
        FileInputStream fis = new FileInputStream(filename);
        Preferences.importPreferences(fis);
        fis.close();

        /*
         * We load the preferences but we don't need the returned
         * value.
         */
        Preferences.userNodeForPackage(KESI.class);
    }

    public static void main(String[] args) {
        String cfg;
        KESI kesi;

        if (args.length != 1) {
            System.err
                    .println("Invalid number of arguments. Config file is required");
            System.exit(-1);
        }

        cfg = args[0];

        kesi = new KESI();

        try {
            kesi.loadPreferences(cfg);
        } catch (IOException e) {
            System.err.printf("Error opening %s config file. %s", cfg,
                              e.getMessage());
            System.exit(-1);
        } catch (InvalidPreferencesFormatException e) {
            System.err.printf("Error in config file format. %s",
                              e.getMessage());
            System.exit(-1);
        }

        kesi.run(cfg);
    }
}
