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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public abstract class DatabaseExtractor {
    /* Database preference root node */
    public static final String DATABASE_NODE_ROOT = "database";

    protected Connection conn;

    protected DatabaseExtractor(String driver, String userName, String password,
                                String host, String port, String database)
           throws DriverNotSupportedError, DatabaseConnectionError {
        Properties connectionProps;
        String url = null;

        connectionProps = new Properties();
        connectionProps.put("user", userName);
        connectionProps.put("password", password);

        try {
            if (driver.equals("mysql")) {
                url = "jdbc:" + driver + "://" + host + ":" + port + "/"
                      + database;
                conn = DriverManager.getConnection(url, connectionProps);
            } else {
                throw new DriverNotSupportedError(driver);
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionError(e.getMessage(), url);
        }

        System.out.println("Connected to database");
    }

    protected ResultSet executeQuery(String query) throws SQLException {
        Statement stmt;
        ResultSet rs;

        stmt = conn.createStatement();
        rs = stmt.executeQuery(query);

        return rs;
    }

    protected ResultSet executeQuery(PreparedStatement stmt)
              throws SQLException {
        ResultSet rs;

        rs = stmt.executeQuery();

        return rs;
    }
}
