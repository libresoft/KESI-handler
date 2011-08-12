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
import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({Action.class, Add.class, Copy.class, Delete.class, Modify.class,
             Move.class, Rename.class, Replace.class, Module.class, Function.class})
@XmlRootElement(name = "commit")
public class Commit extends Entity {
    private Repository repository;
    private String commitMessage;
    private Date commitDate;
    private String revisionTag;
    private Person author;
    private Person committer;
    private ArrayList<Action> actions;

    public Commit() {}

    public Commit(String commitMessage, Date commitDate, String revisionTag,
                  Person author, Person committer) {
        this.commitMessage = commitMessage;
        this.commitDate = commitDate;
        this.revisionTag = revisionTag;
        this.author = author;
        this.committer = committer;
        this.repository = null;
        this.actions = new ArrayList<Action>();
    }

    @XmlElement()
    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    @XmlElement()
    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    @XmlElement()
    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    @XmlElement()
    public String getRevisionTag() {
        return revisionTag;
    }

    public void setRevisionTag(String revisionTag) {
        this.revisionTag = revisionTag;
    }

    @XmlElement()
    public Person getAuthor() {
        return author;
    }

    public void setAuthor(Person author) {
        this.author = author;
    }

    @XmlElement()
    public Person getCommitter() {
        return committer;
    }

    public void setCommitter(Person committer) {
        this.committer = committer;
    }

    @XmlElement(name = "action")
    public ArrayList<Action> getActions() {
        return actions;
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public String toXML() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Commit.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        marshaller.marshal(this, xml);
        return xml.toString();
    }
}
