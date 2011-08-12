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

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;

public class Comment extends Entity {
    private String comment;
    private Person commentor; /* poster of the comment */
    private Date date;

    public Comment() {}

    public Comment(String comment, Person commentor, Date date) {
        this.comment = comment;
        this.commentor = commentor;
        this.date = date;
    }

    @XmlElement()
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @XmlElement()
    public Person getCommentor() {
        return commentor;
    }

    public void setCommentor(Person commentor) {
        this.commentor = commentor;
    }

    @XmlElement()
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
