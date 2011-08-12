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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import eu.alertproject.kesi.model.Attachment;
import eu.alertproject.kesi.model.Comment;
import eu.alertproject.kesi.model.Issue;
import eu.alertproject.kesi.model.Person;

public class ITSDatabaseExtractor extends DatabaseExtractor {
    /* ITS QUERIES */
    private static final String ITS_QUERY_ISSUE = "SELECT id, summary, description,"
            + "status, resolution, priority, submitted_by, submitted_on, assigned_to "
            + "FROM issues WHERE issue = ?";
    private static final String ITS_QUERY_PERSON = "SELECT name, email, user_id "
            + "FROM people WHERE id = ?";
    private static final String ITS_QUERY_COMMENTS = "SELECT text, submitted_by, submitted_on "
            + "FROM comments WHERE issue_id = ?";
    private static final String ITS_QUERY_ATTACHMENTS = "SELECT name, description, url, "
            + "submitted_by, submitted_on FROM attachments WHERE issue_id = ?";

    /* ITS Row fields */
    private static final String ITS_ISSUE_ID = "id";
    private static final String ITS_ISSUE_SUMMARY = "summary";
    private static final String ITS_ISSUE_DESCRIPTION = "description";
    private static final String ITS_ISSUE_STATUS = "status";
    private static final String ITS_ISSUE_RESOLUTION = "resolution";
    private static final String ITS_ISSUE_PRIORITY = "priority";
    private static final String ITS_ISSUE_ASSIGNED_TO = "assigned_to";
    private static final String ITS_SUBMITTED_BY = "submitted_by";
    private static final String ITS_SUBMITTED_ON = "submitted_on";
    private static final String ITS_PERSON_NAME = "name";
    private static final String ITS_PERSON_EMAIL = "email";
    private static final String ITS_PERSON_USER_ID = "user_id";
    private static final String ITS_COMMENT_TEXT = "text";
    private static final String ITS_ATTACHMENT_FILE = "name";
    private static final String ITS_ATTACHMENT_DESCRIPTION = "description";
    private static final String ITS_ATTACHMENT_URL = "url";

    /* Person cache */
    private HashMap<Integer, Person> people;

    public ITSDatabaseExtractor(String driver, String username,
           String password, String host, String port, String database)
           throws DriverNotSupportedError, DatabaseConnectionError {
        super(driver, username, password, host, port, database);
        people = new HashMap<Integer, Person>();
    }

    public Issue getIssue(String issueID) {
        PreparedStatement stmt;
        ResultSet rs;

        try {
            /* Variables for storing temporal issue's info */
            int id; /* Issue id used in the database */
            int submitterID;
            int assignedID;
            String summary;
            String desc;
            String status;
            String resolution;
            String priority;
            Date submittedOn;

            Issue issue;
            Person submittedBy;
            Person assignedTo;
            ArrayList<Comment> comments;
            ArrayList<Attachment> attachments;

            stmt = conn.prepareStatement(ITS_QUERY_ISSUE);
            stmt.setString(1, issueID);
            rs = executeQuery(stmt);

            rs.first();

            id = rs.getInt(ITS_ISSUE_ID);
            summary = rs.getString(ITS_ISSUE_SUMMARY);
            desc = rs.getString(ITS_ISSUE_DESCRIPTION);
            status = rs.getString(ITS_ISSUE_STATUS);
            resolution = rs.getString(ITS_ISSUE_RESOLUTION);
            priority = rs.getString(ITS_ISSUE_PRIORITY);
            submitterID = rs.getInt(ITS_SUBMITTED_BY);
            submittedOn = rs.getDate(ITS_SUBMITTED_ON);
            assignedID = rs.getInt(ITS_ISSUE_ASSIGNED_TO);

            submittedBy = getPerson(submitterID);
            assignedTo = getPerson(assignedID);

            issue = new Issue(issueID, summary, desc, submittedOn, submittedBy);
            issue.setAssignedTo(assignedTo);

            /*
             * FIXME: check values for resolution, priority and status
             */
            issue.setResolution(resolution);
            issue.setSeverity(priority);
            issue.setState(status);

            comments = getComments(id);
            for (Comment comment : comments) {
                issue.addComment(comment);
            }

            attachments = getAttachments(id);
            for (Attachment attachment : attachments) {
                issue.addAttachment(attachment);
            }

            stmt.close();

            return issue;
        } catch (SQLException e) {
            System.err.println("Error getting data. " + e.getMessage());
            return null;
        }
    }

    private Person getPerson(int userID) throws SQLException {
        /* Variables for storing temporal person's info */
        String name;
        String email;
        String userITS;

        PreparedStatement stmt;
        ResultSet rs;
        Person person;

        if (people.containsKey(userID)) {
            return people.get(userID);
        }

        stmt = conn.prepareStatement(ITS_QUERY_PERSON);
        stmt.setInt(1, userID);
        rs = executeQuery(stmt);

        rs.first();

        name = rs.getString(ITS_PERSON_NAME);
        email = rs.getString(ITS_PERSON_EMAIL);
        userITS = rs.getString(ITS_PERSON_USER_ID);
        person = new Person(name, email, userITS);

        people.put(userID, person);

        stmt.close();

        return person;
    }

    private ArrayList<Comment> getComments(int issueID) throws SQLException {
        PreparedStatement stmt;
        ResultSet rs;
        ArrayList<Comment> comments;

        stmt = conn.prepareStatement(ITS_QUERY_COMMENTS);
        stmt.setInt(1, issueID);
        rs = executeQuery(stmt);

        comments = new ArrayList<Comment>();

        while (rs.next()) {
            String text = rs.getString(ITS_COMMENT_TEXT);
            int submitterID = rs.getInt(ITS_SUBMITTED_BY);
            Date date = rs.getDate(ITS_SUBMITTED_ON);

            Person person = getPerson(submitterID);
            Comment comment = new Comment(text, person, date);
            comments.add(comment);
        }

        stmt.close();

        return comments;
    }

    private ArrayList<Attachment> getAttachments(int issueID)
            throws SQLException {
        PreparedStatement stmt;
        ResultSet rs;
        ArrayList<Attachment> attachments;

        stmt = conn.prepareStatement(ITS_QUERY_ATTACHMENTS);
        stmt.setInt(1, issueID);
        rs = executeQuery(stmt);

        attachments = new ArrayList<Attachment>();

        while (rs.next()) {
            int submitterID;
            String filename;
            String description;
            URI url;
            Date date;

            Person person;
            Attachment attachment;

            filename = rs.getString(ITS_ATTACHMENT_FILE);
            description = rs.getString(ITS_ATTACHMENT_DESCRIPTION);

            try {
                url = new URI(rs.getString(ITS_ATTACHMENT_URL));
            } catch (URISyntaxException e) {
                System.err.println("Error parsing attachement url. "
                                   + e.getMessage());
                url = null;
            }
            submitterID = rs.getInt(ITS_SUBMITTED_BY);
            date = rs.getDate(ITS_SUBMITTED_ON);

            person = getPerson(submitterID);

            attachment = new Attachment(filename, description, person, date,
                                        url);
            attachments.add(attachment);
        }

        stmt.close();

        return attachments;
    }
}
