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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import eu.alertproject.kesi.model.IssueTracker;
import eu.alertproject.kesi.model.Repository;
import eu.alertproject.kesi.model.StructuredKnowledgeSource;

/**
 * Singleton implementation based on enum types. See Joshua Bloch's
 * conference in the Google I/O 2008 <a href=
 * "http://sites.google.com/site/io/effective-java-reloaded/effective_java_reloaded.pdf"
 * ></a>
 *
 * TODO: addNodeChangeListener for changes in the repositories
 */
public enum KnowledgeSourceManager {
    INSTANCE;

    /* Knowledge manager reference root node */
    public static final String KNOWLEDGE_SOURCES_NODE_ROOT = "sources";
    public static final String SCM_SOURCES_NODE = "scm";
    public static final String ITS_SOURCES_NODE = "its";

    /* Knowledge Sources keys */
    public static final String SOURCE_URI_KEY = "uri";
    public static final String SOURCE_TYPE_KEY = "type";
    public static final String SOURCE_IMPORT_KEY = "import";

    private String[] sourceTypes = { SCM_SOURCES_NODE, ITS_SOURCES_NODE };

    private Preferences prefs;
    private HashMap<URI, Repository> scmSources = new HashMap<URI, Repository>();
    private HashMap<URI, IssueTracker> itsSources = new HashMap<URI, IssueTracker>();
    private ArrayList<StructuredKnowledgeSource> sourcesToImport = new ArrayList<StructuredKnowledgeSource>();

    public void loadKnowledgeSources() throws PreferencesError {
        prefs = Preferences.userNodeForPackage(KESI.class).node(
                KNOWLEDGE_SOURCES_NODE_ROOT);

        for (String sourceType : sourceTypes) {
            try {
                String[] sourceIDList;
                Preferences srcList;

                if (prefs.nodeExists(sourceType)) {
                    srcList = prefs.node(sourceType);
                } else {
                    continue;
                }

                System.out.printf("Loading %s sources\n", sourceType);

                sourceIDList = srcList.childrenNames();

                for (String srcID : sourceIDList) {
                    String rawURI;
                    URI uri;
                    String type;
                    boolean toImport;

                    rawURI = srcList.node(srcID).get(SOURCE_URI_KEY, "");
                    if (rawURI.equals("")) {
                        System.err.printf("Missing value for %s key in %s source. Ignoring source.\n",
                                          SOURCE_URI_KEY, srcID);
                        continue;
                    }

                    try {
                        uri = new URI(rawURI);
                    } catch (URISyntaxException e) {
                        System.err.printf("Invalid uri %s for source %s. Ignoring source.\n",
                                          rawURI, srcID);
                        continue;
                    }

                    type = srcList.node(srcID).get(SOURCE_TYPE_KEY, "");
                    toImport = srcList.node(srcID).getBoolean(
                               SOURCE_IMPORT_KEY, false);

                    if (type.equals("")) {
                        System.err.printf("Missing value for %s key in %s source. Ignoring source.\n",
                                          SOURCE_TYPE_KEY, srcID);
                        continue;
                    }

                    // FIXME: factory here??
                    if (sourceType.equals(SCM_SOURCES_NODE)) {
                        Repository repo = new Repository(uri, type);
                        scmSources.put(uri, repo);

                        if (toImport) {
                            sourcesToImport.add(repo);
                        }
                    } else if (sourceType.equals(ITS_SOURCES_NODE)) {
                        IssueTracker tracker = new IssueTracker(uri, type);
                        itsSources.put(uri, tracker);

                        if (toImport) {
                            sourcesToImport.add(tracker);
                        }
                    } else {
                        System.err.printf("Bad %s source type. Ignoring.\n",
                                          sourceType);
                        continue;
                    }

                    System.out.printf("Source %s (%s) loaded.\n", srcID, type);
                }

                System.out.printf("%s sources loaded\n",
                                  sourceType.toUpperCase());
            } catch (BackingStoreException e) {
                throw new PreferencesError("Error parsing knowledge sources. "
                                           + e.getMessage());
            }
        }
    }

    public void startUp(JobsQueue scmJobs, JobsQueue itsJobs) {
        System.out.println("Scheduling initial import for sources");

        for (StructuredKnowledgeSource source : sourcesToImport) {
            int id = 0;
            Job job = null;

            try {
                if (source instanceof Repository) {
                    job = new Job(Job.SCM_JOB_TYPE, source.getURI()
                                  .toASCIIString(), String.valueOf(id++));
                    job.setStartUp(true);
                    scmJobs.put(job);
                } else if (source instanceof IssueTracker) {
                    job = new Job(Job.ITS_JOB_TYPE, source.getURI()
                                  .toASCIIString(), String.valueOf(id++));
                    job.setStartUp(true);
                    itsJobs.put(job);
                } else {
                    System.err.printf("Invalid source type of class %s. Job not scheduled.\n",
                                      source.getClass().toString());
                    continue;
                }
            } catch (InterruptedException e) {
                System.err.println("Unexpected error in KnowledgeSourceManager thread");
                throw new RuntimeException(e);
            }
            System.out.printf("Job for source %s scheduled\n", job.getURL());
        }
        System.out.println("Schedule completed");
    }
}
