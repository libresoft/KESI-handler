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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

public class SensorHandler extends Thread {
    /* Mail preference root node */
    public static final String SENSOR_NODE_ROOT = "sensor";

    /* Mail preferences keys */
    private static final String MAIL_PROTOCOL_KEY = "mail.store.protocol";
    private static final String IMAP_HOST_KEY = "mail.imap.host";
    private static final String IMAP_PORT_KEY = "mail.imap.port";
    private static final String IMAP_CONN_TIMEOUT_KEY = "mail.imap.connectiontimeout";
    private static final String IMAP_TIMEOUT_KEY = "mail.imap.timeout";

    /*
     * Patterns for checking whether an event is received from SCM or
     * BTS repositories
     *
     * TODO: add support for JIRA
     */
    private static final String BUGZILLA_PATTERN = "\\[Bug ([0-9]+)\\] .+";
    private static final String SCM_PATTERN = "([0-9]+) (.+)";

    private Preferences prefs;
    private JobsQueue scmJobs;
    private JobsQueue itsJobs;

    public SensorHandler(JobsQueue scmJobs, JobsQueue itsJobs) {
        this.scmJobs = scmJobs;
        this.itsJobs = itsJobs;
        prefs = Preferences.userNodeForPackage(KESI.class).node(SENSOR_NODE_ROOT);
    }

    private Store connect() throws MessagingException {
        Properties props;
        String username;
        String password;
        String host;
        Session session;
        Store store;

        props = new Properties();
        props.put(MAIL_PROTOCOL_KEY,
                  prefs.get(KESI.PREF_SENSOR_PROTOCOL, KESI.DEF_SENSOR_PROTOCOL));
        props.put(IMAP_CONN_TIMEOUT_KEY,
                  prefs.getInt(KESI.PREF_SENSOR_TIMEOUT, KESI.DEF_SENSOR_TIMEOUT));
        props.put(IMAP_TIMEOUT_KEY,
                  prefs.getInt(KESI.PREF_SENSOR_TIMEOUT, KESI.DEF_SENSOR_TIMEOUT));

        username = prefs.get(KESI.PREF_SENSOR_USERNAME, "");
        password = prefs.get(KESI.PREF_SENSOR_PASSWORD, "");
        host = prefs.get(KESI.PREF_SENSOR_HOST, "");

        session = Session.getDefaultInstance(props, null);
        store = session.getStore("imaps");
        store.connect(host, username, password);

        return store;
    }

    public Job getJobFromMessage(Message message) throws SensorHandlerError {
        try {
            Job job = null;
            String subject;
            Pattern pattern;
            Matcher matcher;

            subject = message.getSubject();
            pattern = Pattern.compile(BUGZILLA_PATTERN);
            matcher = pattern.matcher(subject);

            if (matcher.find()) {
                /* Extracts data from bugzilla mail */
                try {
                    String body = (String) message.getContent();
                    URL url = new URL(body.substring(0, body.indexOf('\n')));

                    job = new Job(Job.ITS_JOB_TYPE, url.toString(),
                                  matcher.group(1));
                } catch (MalformedURLException e) {
                    System.err.println("Error parsing the URL. "
                                       + e.getMessage());
                    e.printStackTrace();
                    return null;
                } catch (IOException e) {
                    System.err.println("Error parsing the message. "
                                       + e.getMessage());
                    return null;
                }
            } else {
                pattern = Pattern.compile(SCM_PATTERN);
                matcher = pattern.matcher(subject);

                if (matcher.find()) {
                    /* Extracts data from SCMs */
                    job = new Job(Job.SCM_JOB_TYPE, matcher.group(2),
                                  matcher.group(1));
                }
            }

            if (job != null) {
                System.out.println("Message " + message.getMessageNumber()
                                   + " matchs.");
                return job;
            } else {
                String msg = "Error guessing the type of repository for message "
                             + message.getMessageNumber()
                             + ".Subject didn't match any pattern.";
                throw new SensorHandlerError(msg);
            }
        } catch (MessagingException e) {
            String msg = "Error parsing message. " + e.getMessage();
            throw new SensorHandlerError(msg);
        }
    }

    public void run() {
        Store store;

        try {
            store = connect();
        } catch (MessagingException e) {
            System.err.println("Unable to connect with sensor's provider");
            throw new RuntimeException(e);
        }

        try {
            int wait;
            Folder folder;

            folder = store.getFolder(prefs.get(KESI.PREF_SENSOR_FOLDER,
                     KESI.DEF_SENSOR_FOLDER));
            folder.open(Folder.READ_WRITE);

            wait = prefs.getInt(KESI.PREF_SENSOR_POLL_INTERVAL,
                                KESI.DEF_SENSOR_POLL_INTERVAL);

            while (true) {
                int nmsg;

                /*
                 * Due to a weird behavior we need to get first the
                 * total number of messages and after that, we can
                 * retrieve the real number of new or unread
                 * messages.
                 */
                folder.getMessageCount();
                nmsg = folder.getNewMessageCount()
                       + folder.getUnreadMessageCount();

                if (nmsg == 0) {
                    System.out
                            .println("No messages. Waiting for new messages.");
                    sleep(wait);
                    continue;
                }

                System.out.println(nmsg + " new messages. Handling...");

                Message messages[] = folder.search(new FlagTerm(new Flags(
                        Flags.Flag.SEEN), false));

                for (Message message : messages) {
                    Job job = null;

                    try {
                        job = getJobFromMessage(message);
                    } catch (SensorHandlerError e) {
                        System.err.print(e);
                        System.err.println("Ignoring message.");
                    }

                    if (job != null) {
                        if (job.getType().equals("SCM")) {
                            scmJobs.put(job);
                            System.out.println("New SCM job created");
                        } else {
                            itsJobs.put(job);
                            System.out.println("New ITS job created");
                        }
                    } else {
                        /* Something went really wrong, again */
                        throw new NullPointerException("Job cannnot be null");
                    }

                    message.setFlag(Flags.Flag.SEEN, true);
                }

                sleep(wait);
            }
        } catch (MessagingException e) {
            System.err.println("Error in sensor's provider connection");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.err.println("Unexpected error in sensor thread");
            throw new RuntimeException(e);
        }
    }
}
