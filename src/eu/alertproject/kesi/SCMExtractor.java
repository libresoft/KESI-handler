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

import java.util.prefs.Preferences;

import javax.xml.bind.JAXBException;

public class SCMExtractor extends KnowledgeExtractor {
    /* Tool for extracting data from SCMs */
    private static final String SCM_EXTRACTOR = "cvsanaly2";

    private Preferences prefs;

    public SCMExtractor(JobsQueue queue) {
        super(SCM_EXTRACTOR, queue);
        prefs = Preferences.userNodeForPackage(KESI.class).node(
                DatabaseExtractor.DATABASE_NODE_ROOT);
    }

    @Override
    public String[] getCommandExtractor(String url, String type) {
        String username = prefs.get(KESI.PREF_DB_USERNAME, "");
        String password = prefs.get(KESI.PREF_DB_PASSWORD, "");
        String host = prefs.get(KESI.PREF_DB_HOST, KESI.DEF_DB_HOSTNAME);
        String database = prefs.get(KESI.PREF_DB_DATABASE_SCM, "");

        String[] cmd = {SCM_EXTRACTOR, "-u", username, "-p", password, "-d",
                        database, "-H", host, "--extensions", "Metrics",
                        "--metrics-all", url };
        return cmd;
    }

    @Override
    public String getExtractedData(String url, String type, String id) {
        SCMDatabaseExtractor extractor;

        String username = prefs.get(KESI.PREF_DB_USERNAME, "");
        String password = prefs.get(KESI.PREF_DB_PASSWORD, "");
        String host = prefs.get(KESI.PREF_DB_HOST, KESI.DEF_DB_HOSTNAME);
        String port = prefs.get(KESI.PREF_DB_PORT, KESI.DEF_DB_PORT);
        String database = prefs.get(KESI.PREF_DB_DATABASE_SCM, "");

        try {
            extractor = new SCMDatabaseExtractor(KESI.DEF_DB_DBMS, username,
                        password, host, port, database);
            return extractor.getCommit(id).toXML();
        } catch (DriverNotSupportedError e) {
            System.err.println(e.getMessage());
            return null;
        } catch (DatabaseConnectionError e) {
            System.err.println(e.getMessage());
            return null;
        } catch (JAXBException e) {
            System.err.println("Error marshaling data to XML. "
                               + e.getMessage());
            return null;
        }
    }
}
