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

/**
 * This abstract class represents the different actions performed in
 * every commit.
 *
 * <p>In systems like CVS, where the commit is limited to a single
 * file, there will be only one action per commit. However, most of
 * the version control systems support atomic commits, where several
 * actions are carried out on several files.</p>
 *
 * <p>Keep in mind that the type of the actions are specific for each
 * SCM solution. Though some of them are common such as <em>add</em>,
 * <em>delete</em> or <em>modify</em>, others are just available on a
 * type of repository, like <em>replace</em> for the SVN solution.</p>
 *
 */
public abstract class Action extends Entity {
    protected File file;

    public Action() {}

    protected Action(File file) {
        this.file = file;
    }

    protected File getFile() {
        return file;
    }
}
