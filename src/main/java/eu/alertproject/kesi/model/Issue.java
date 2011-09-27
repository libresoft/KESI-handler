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

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({StructuredKnowledgeSource.class, IssueTracker.class})
@XmlRootElement(name = "issue")
public class Issue extends Entity {
    /* Severity values */
    public static final String BLOCKER = "Blocker";
    public static final String CRITICAL = "Critical";
    public static final String MAJOR = "Major";
    public static final String MINOR = "Minor";
    public static final String TRIVIAL = "Trivial";
    public static final String FEATURE = "Feature";
    /* State values */
    public static final String ASSIGNED = "Assigned";
    public static final String OPEN = "Open";
    public static final String RESOLVED = "Resolved";
    public static final String VERIFIED = "Verified";
    public static final String CLOSED = "Closed";
    /* Resolution values */
    public static final String DUPLICATED = "Duplicated";
    public static final String FIXED = "Fixed";
    public static final String INVALID = "Invalid";
    public static final String LATER = "Later";
    public static final String REMIND = "Remind";
    public static final String THIRD_PARTY = "Third Party";
    public static final String WONT_FIX = "Wont Fix";
    public static final String WORKS_FOR_ME = "Works For Me";

    private IssueTracker issueTracker;
    private String issueID;
    private URI issueURL;
    private String shortDesc;
    private String description;
    private Date dateOpened;
    private Date lastModified;
    private Person assignedTo;
    private Person reporter;
    private String severity;
    private String state;
    private String resolution;
    private ArrayList<Comment> comments;
    private ArrayList<Attachment> attachments;

    public Issue() {}

    public Issue(String issueID, String shortDesc, String description,
                 Date opened, Person reporter) {
        this.issueID = issueID;
        this.shortDesc = shortDesc;
        this.description = description;
        this.dateOpened = opened;
        this.reporter = reporter;
        this.comments = new ArrayList<Comment>();
        this.attachments = new ArrayList<Attachment>();
    }

    public IssueTracker getIssueTracker() {
        return issueTracker;
    }

    public void setIssueTracker(IssueTracker issueTracker) {
        this.issueTracker = issueTracker;
    }

    @XmlElement()
    public String getIssueID() {
        return issueID;
    }

    public void setIssueId(String issueID) {
        this.issueID = issueID;
    }

    @XmlElement()
    public URI getIssueURL() {
        return issueURL;
    }

    public void setIssueURL(URI issueURL) {
        this.issueURL = issueURL;
    }

    @XmlElement()
    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    @XmlElement()
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement()
    public Date getDateOpened() {
        return dateOpened;
    }

    public void setDateOpened(Date dateOpened) {
        this.dateOpened = dateOpened;
    }

    @XmlElement()
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @XmlElement()
    public Person getReporter() {
        return reporter;
    }

    public void setReporter(Person reporter) {
        this.reporter = reporter;
    }

    public boolean isEnhancement() {
        return severity.equals(FEATURE);
    }

    public boolean isBug() {
        return !isEnhancement();
    }

    @XmlElement()
    public Person getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Person assignedTo) {
        this.assignedTo = assignedTo;
    }

    @XmlElement()
    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @XmlElement()
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @XmlElement()
    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    @XmlElement()
    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    @XmlElement()
    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
    }

    public String toXML() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Issue.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        marshaller.marshal(this, xml);
        return xml.toString();
    }
}
