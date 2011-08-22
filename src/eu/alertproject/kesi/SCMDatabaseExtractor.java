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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import eu.alertproject.kesi.model.Action;
import eu.alertproject.kesi.model.Add;
import eu.alertproject.kesi.model.Commit;
import eu.alertproject.kesi.model.Copy;
import eu.alertproject.kesi.model.Delete;
import eu.alertproject.kesi.model.File;
import eu.alertproject.kesi.model.Function;
import eu.alertproject.kesi.model.Metrics;
import eu.alertproject.kesi.model.Modify;
import eu.alertproject.kesi.model.Module;
import eu.alertproject.kesi.model.Move;
import eu.alertproject.kesi.model.Person;
import eu.alertproject.kesi.model.Rename;
import eu.alertproject.kesi.model.Replace;

public class SCMDatabaseExtractor extends DatabaseExtractor {
    /* SCM queries */
    private static final String SCM_QUERY_ALL_COMMITS = "SELECT rev "
            + "FROM scmlog";
    private static final String SCM_QUERY_COMMIT = "SELECT id, rev, date, message,"
            + "author_id, committer_id FROM scmlog WHERE rev = ?";
    private static final String SCM_QUERY_PERSON = "SELECT name, email "
            + "FROM people WHERE id = ?";
    private static final String SCM_QUERY_FILE = "SELECT file_name "
            + "FROM files WHERE id = ?";
    private static final String SCM_QUERY_ACTIONS = "SELECT id, file_id, branch_id, type "
            + "FROM actions WHERE commit_id = ?";
    private static final String SCM_QUERY_COPIES = "SELECT to_id, from_id, new_file_name "
            + "FROM file_copies WHERE action_id = ?";
    private static final String SCM_QUERY_METRICS = "SELECT lang, sloc, loc, ncomment, lcomment, lblank "
            + "FROM metrics WHERE file_id = ? AND commit_id = ?";
    private static final String SCM_QUERY_MODULES = "SELECT id, name, start_line, end_line "
            + "FROM modules_src WHERE file_id = ? AND commit_id = ?";
    private static final String SCM_QUERY_FUNCTIONS = "SELECT header, start_line, end_line "
            + "FROM functions_src WHERE module_id= ?";

    /* SCM Row fields */
    private static final String SCM_COMMIT_ID = "id";
    private static final String SCM_COMMIT_MSG = "message";
    private static final String SCM_COMMIT_DATE = "date";
    private static final String SCM_COMMIT_REVISION = "rev";
    private static final String SCM_AUTHOR_ID = "author_id";
    private static final String SCM_COMMITTER_ID = "committer_id";
    private static final String SCM_ACTION_ID = "id";
    private static final String SCM_ACTION_TYPE = "type";
    private static final String SCM_ACTION_FILE_ID = "file_id";
    private static final String SCM_PERSON_NAME = "name";
    private static final String SCM_PERSON_EMAIL = "email";
    private static final String SCM_FILE_NAME = "file_name";
    private static final String SCM_FILE_COPIES_FROM = "from_id";
    private static final String SCM_FILE_COPIES_TO = "to_id";
    private static final String SCM_FILE_COPIES_NEW_NAME = "new_file_name";
    private static final String SCM_METRIC_LANG = "lang";
    private static final String SCM_METRIC_SLOC = "sloc";
    private static final String SCM_METRIC_LOC = "loc";
    private static final String SCM_METRIC_NCOMMENT = "ncomment";
    private static final String SCM_METRIC_LCOMMENT = "lcomment";
    private static final String SCM_METRIC_LBLANK = "lblank";
    private static final String SCM_MODULE_ID = "id";
    private static final String SCM_MODULE_NAME = "name";
    private static final String SCM_FUNCTION_HEADER = "header";
    private static final String SCM_SRC_START_LINE = "start_line";
    private static final String SCM_SRC_END_LINE = "end_line";

    /* Keys for the distinct actions */
    private static final String SCM_ADD_ACTION = "A";
    private static final String SCM_DELETE_ACTION = "D";
    private static final String SCM_MODIFY_ACTION = "M";
    private static final String SCM_COPY_ACTION = "C";
    private static final String SCM_MOVE_ACTION = "V";
    private static final String SCM_REPLACE_ACTION = "R";

    /* Person cache */
    private HashMap<Integer, Person> people;

    public SCMDatabaseExtractor(String driver, String username,
           String password, String host, String port, String database)
           throws DriverNotSupportedError, DatabaseConnectionError {
        super(driver, username, password, host, port, database);
        people = new HashMap<Integer, Person>();
    }

    public ArrayList<Commit> getCommits() {
        try {
            PreparedStatement stmt;
            ResultSet rs;
            ArrayList<Commit> commits = new ArrayList<Commit>();

            stmt = conn.prepareStatement(SCM_QUERY_ALL_COMMITS);
            rs = executeQuery(stmt);

            while (rs.next()) {
                String commitKey; /* Commit id used in the database */
                Commit commit;

                commitKey = rs.getString(SCM_COMMIT_REVISION);
                commit = getCommit(commitKey);

                if (commit == null) {
                    System.err.println("Error getting commit " + commitKey);
                    return null;
                }

                commits.add(commit);
            }

            return commits;
        } catch (SQLException e) {
            System.err.println("Error getting data. " + e.getMessage());
            return null;
        }
    }

    public Commit getCommit(String commitKey) {
        try {
            PreparedStatement stmt;
            ResultSet rs;

            /* Variables for storing temporal commit's info */
            int commitID; /* Commit id used in the database */
            int authorID;
            String commitMessage;
            String commitRev;
            Date commitDate;

            Person committer;
            Person author; /* Author of the commit, if any */
            Commit commit;
            ArrayList<Action> actions;

            stmt = conn.prepareStatement(SCM_QUERY_COMMIT);
            stmt.setString(1, commitKey);
            rs = executeQuery(stmt);

            rs.first();

            commitID = rs.getInt(SCM_COMMIT_ID);
            commitMessage = rs.getString(SCM_COMMIT_MSG);
            commitDate = rs.getDate(SCM_COMMIT_DATE);
            commitRev = rs.getString(SCM_COMMIT_REVISION);
            authorID = rs.getInt(SCM_AUTHOR_ID);

            author = (rs.wasNull() ? null : getPerson(authorID));
            committer = getPerson(rs.getInt(SCM_COMMITTER_ID));

            commit = new Commit(commitMessage, commitDate, commitRev, author,
                     committer);

            actions = getActions(commitID);
            for (Action action : actions) {
                commit.addAction(action);
            }

            stmt.close();

            return commit;
        } catch (SQLException e) {
            System.err.println("Error getting data. " + e.getMessage());
            return null;
        }
    }

    private ArrayList<Action> getActions(int commitID) throws SQLException {
        PreparedStatement stmt;
        ResultSet rs;
        ArrayList<Action> actions;

        stmt = conn.prepareStatement(SCM_QUERY_ACTIONS);
        stmt.setInt(1, commitID);
        rs = executeQuery(stmt);

        actions = new ArrayList<Action>();

        while (rs.next()) {
            /*
             * Variables for storing temporal action's info FIXME:
             * branch info should be useful int branch =
             * rs.getInt("branch_id");
             */
            int actionID;
            int fileID;
            String actionType;

            File file;
            Action action;

            actionID = rs.getInt(SCM_ACTION_ID);
            actionType = rs.getString(SCM_ACTION_TYPE);
            fileID = rs.getInt(SCM_ACTION_FILE_ID);

            file = getFile(fileID, commitID);

            /*
             * TODO: Factory pattern here??? Switch statements with
             * Strings have been implemented in JAVA 7 SE. More info
             * here:
             * http://stackoverflow.com/questions/338206/switch-statement
             * -with-strings-in-java
             */
            if (actionType.equals(SCM_COPY_ACTION)
                || actionType.equals(SCM_MOVE_ACTION)
                || actionType.equals(SCM_REPLACE_ACTION)) {
                int fromFileID;
                int toFileID;
                String newName;

                PreparedStatement cpStmt;
                ResultSet cpRs;
                File source;
                File destination;

                cpStmt = conn.prepareStatement(SCM_QUERY_COPIES);
                cpStmt.setInt(1, actionID);
                cpRs = executeQuery(cpStmt);

                cpRs.first();

                fromFileID = cpRs.getInt(SCM_FILE_COPIES_FROM);
                toFileID = cpRs.getInt(SCM_FILE_COPIES_TO);
                newName = cpRs.getString(SCM_FILE_COPIES_NEW_NAME);

                if (actionType.equals(SCM_COPY_ACTION)) {
                    source = getFile(fromFileID, commitID);
                    destination = file;

                    /* Change the name of the file to the new one */
                    if (!cpRs.wasNull()) {
                        destination.setFileID(newName);
                    }

                    action = new Copy(source, destination);
                } else if (actionType.equals(SCM_REPLACE_ACTION)) {
                    source = file;
                    destination = getFile(toFileID, commitID);

                    action = new Replace(source, destination);
                } else {
                    /* Move or Rename cases */
                    source = getFile(fromFileID, commitID);
                    destination = file;

                    if (!cpRs.wasNull()) {
                        destination.setFileID(newName);
                        action = new Rename(source, destination);
                    } else {
                        action = new Move(source, destination);
                    }
                }

                cpStmt.close();
            } else if (actionType.equals(SCM_ADD_ACTION)
                       || actionType.equals(SCM_MODIFY_ACTION)) {
                Metrics metrics = getMetrics(fileID, commitID);
                ArrayList<Module> modules = getModules(fileID, commitID);

                file.setMetrics(metrics);

                for (Module module : modules) {
                    file.addModule(module);
                }

                if (actionType.equals(SCM_ADD_ACTION)) {
                    action = new Add(file);
                } else {
                    action = new Modify(file);
                }
            } else if (actionType.equals(SCM_DELETE_ACTION)) {
                action = new Delete(file);
            } else {
                System.err.printf("Type %s unknown in commit %d. Ignoring",
                                  actionType, commitID);
                continue;
            }

            actions.add(action);
        }

        stmt.close();

        return actions;
    }

    private Person getPerson(int userID) throws SQLException {
        /* Variables for storing temporal person's info */
        String name;
        String email;

        PreparedStatement stmt;
        ResultSet rs;
        Person person;

        if (people.containsKey(userID)) {
            return people.get(userID);
        }

        stmt = conn.prepareStatement(SCM_QUERY_PERSON);
        stmt.setInt(1, userID);
        rs = executeQuery(stmt);

        rs.first();

        name = rs.getString(SCM_PERSON_NAME);
        email = rs.getString(SCM_PERSON_EMAIL);
        person = new Person(name, email, Integer.toString(userID));

        people.put(userID, person);

        stmt.close();

        return person;
    }

    private File getFile(int fileID, int commitID) throws SQLException {
        /* Variables for storing temporal file's info */
        String filename;

        PreparedStatement stmt;
        ResultSet rs;
        File file;

        stmt = conn.prepareStatement(SCM_QUERY_FILE);
        stmt.setInt(1, fileID);
        rs = executeQuery(stmt);

        rs.first();

        /*
         * FIXME: we use the filename as file identifier, this should
         * be changed to the real id on the database and assign the
         * filename with setFilePath method.
         */
        filename = rs.getString(SCM_FILE_NAME);
        file = new File(filename, commitID);

        stmt.close();

        return file;
    }

    private Metrics getMetrics(int fileID, int commitID) throws SQLException {
        /* Variables for storing temporal metrics' info */
        int im;
        String sm;

        PreparedStatement stmt;
        ResultSet rs;
        Metrics metrics;

        stmt = conn.prepareStatement(SCM_QUERY_METRICS);
        stmt.setInt(1, fileID);
        stmt.setInt(2, commitID);
        rs = executeQuery(stmt);

        if (!rs.first()) {
            return null;
        }

        metrics = new Metrics();

        sm = rs.getString(SCM_METRIC_LANG);
        metrics.setLang(rs.wasNull() ? null : sm);

        im = rs.getInt(SCM_METRIC_SLOC);
        metrics.setSLOC(rs.wasNull() ? -1 : im);

        im = rs.getInt(SCM_METRIC_LOC);
        metrics.setLOC(rs.wasNull() ? -1 : im);

        im = rs.getInt(SCM_METRIC_NCOMMENT);
        metrics.setNumOfComments(rs.wasNull() ? -1 : im);

        im = rs.getInt(SCM_METRIC_LCOMMENT);
        metrics.setLinesOfComments(rs.wasNull() ? -1 : im);

        im = rs.getInt(SCM_METRIC_LBLANK);
        metrics.setBlankLines(rs.wasNull() ? -1 : im);

        stmt.close();

        return metrics;
    }

    private ArrayList<Module> getModules(int fileID, int commitID)
            throws SQLException {
        PreparedStatement stmt;
        ResultSet rs;
        ArrayList<Module> modules;

        stmt = conn.prepareStatement(SCM_QUERY_MODULES);
        stmt.setInt(1, fileID);
        stmt.setInt(2, commitID);
        rs = executeQuery(stmt);

        modules = new ArrayList<Module>();

        while (rs.next()) {
            int moduleID = rs.getInt(SCM_MODULE_ID);
            String name = rs.getString(SCM_MODULE_NAME);
            int startLine = rs.getInt(SCM_SRC_START_LINE);
            int endLine = rs.getInt(SCM_SRC_END_LINE);
            ArrayList<Function> functions = getFunctions(moduleID);
            Module module = new Module(name, startLine, endLine);

            for (Function function : functions) {
                module.addFunction(function);
            }

            modules.add(module);
        }

        stmt.close();

        return modules;
    }

    private ArrayList<Function> getFunctions(int moduleID) throws SQLException {
        PreparedStatement stmt;
        ResultSet rs;
        ArrayList<Function> functions;

        stmt = conn.prepareStatement(SCM_QUERY_FUNCTIONS);
        stmt.setInt(1, moduleID);
        rs = executeQuery(stmt);

        functions = new ArrayList<Function>();

        while (rs.next()) {
            String header = rs.getString(SCM_FUNCTION_HEADER);
            int startLine = rs.getInt(SCM_SRC_START_LINE);
            int endLine = rs.getInt(SCM_SRC_END_LINE);

            functions.add(new Function(header, startLine, endLine));
        }

        stmt.close();

        return functions;
    }
}
