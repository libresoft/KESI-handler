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

import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.xml.bind.JAXBException;

import eu.alertproject.kesi.model.Issue;

public class ITSExtractor extends KnowledgeExtractor {
    /* Tool for extracting data from ITSs */
    private static final String ITS_EXTRACTOR = "bicho";

    private Preferences prefs;

    public ITSExtractor(JobsQueue queue) {
        super(ITS_EXTRACTOR, queue);
        prefs = Preferences.userNodeForPackage(KESI.class).node(
                DatabaseExtractor.DATABASE_NODE_ROOT);
    }

    @Override
    public String[] getCommandExtractor(String url, String type) {
        /*
         * FIXME: check ITS type, the value of "type" is always ITS
         * but must be either "bg" or "jira".
         * TODO: support for JIRA
         */
        String username = prefs.get(KESI.PREF_DB_USERNAME, "");
        String password = prefs.get(KESI.PREF_DB_PASSWORD, "");
        String host = prefs.get(KESI.PREF_DB_HOST, KESI.DEF_DB_HOSTNAME);
        String port = prefs.get(KESI.PREF_DB_PORT, KESI.DEF_DB_PORT);
        String database = prefs.get(KESI.PREF_DB_DATABASE_ITS, "");

        String[] cmd = {ITS_EXTRACTOR, "--db-user-out", username,
                        "--db-password-out", password, "--db-hostname-out", host,
                        "--db-port-out", port, "--db-database-out", database,
                        "bg", url };
        return cmd;
    }

    @Override
    public String getExtractedData(String url, String type, String id) {
        try {
            ITSDatabaseExtractor extractor = createExtractor(url, type);
            return extractor.getIssue(id).toXML();
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

    @Override
    public String[] getFullExtractedData(String url, String type) {
        try {
            int size;
            String[] issuesXML;

            ITSDatabaseExtractor extractor = createExtractor(url, type);
            ArrayList<Issue> issues = extractor.getIssues();

            size = issues.size();
            issuesXML = new String[size];

            for (int i = 0; i < size; i++) {
                Issue issue = issues.get(i);

                if (issue == null) {
                    System.err.println("Error. Issue not extracted.");
                    continue;
                }

                issuesXML[i] = issue.toXML();
            }

            return issuesXML;
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

    private ITSDatabaseExtractor createExtractor(String ulr, String type)
            throws DriverNotSupportedError, DatabaseConnectionError {
          ITSDatabaseExtractor extractor;

          String username = prefs.get(KESI.PREF_DB_USERNAME, "");
          String password = prefs.get(KESI.PREF_DB_PASSWORD, "");
          String host = prefs.get(KESI.PREF_DB_HOST, KESI.DEF_DB_HOSTNAME);
          String port = prefs.get(KESI.PREF_DB_PORT, KESI.DEF_DB_PORT);
          String database = prefs.get(KESI.PREF_DB_DATABASE_ITS, "");

          extractor = new ITSDatabaseExtractor(KESI.DEF_DB_DBMS, username,
                          password, host, port, database);
          return extractor;
    }
}
