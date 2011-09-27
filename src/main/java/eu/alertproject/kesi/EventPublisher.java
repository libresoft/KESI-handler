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

import de.fzi.alert.brokerClients.NotifyClient;

public class EventPublisher {
    /* ESB preference root node */
    public static final String PUB_NODE_ROOT = "publisher";

    private Preferences prefs;
    private String url;

    public EventPublisher() {
        prefs = Preferences.userNodeForPackage(KESI.class).node(PUB_NODE_ROOT);
        url = prefs.get(KESI.PREF_PUB_URL, KESI.DEF_PUB_URL);
    }

    public void publish(String topic, String message) {
        NotifyClient.sendMsg(topic, message, url);
    }

    public static void main(String[] args) {

        EventPublisher pub = new EventPublisher();

        pub.publish("internalns:rootTopic1", "Hello world");
        pub.publish("kesi:msg", "KESI test");
    }
}
