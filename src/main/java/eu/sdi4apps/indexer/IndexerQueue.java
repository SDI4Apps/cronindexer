/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sdi4apps.indexer;

import com.cedarsoftware.util.io.JsonReader;
import eu.sdi4apps.indexer.drivers.ShapefileDriver;
import eu.sdi4apps.openapi.utils.Settings;
import eu.sdi4apps.openapi.utils.Logger;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;

/**
 *
 * @author runarbe
 */
public class IndexerQueue {

    /**
     * Connection to enqueue database
     */
    private static Connection Conn;

    /**
     * create queue table if it doesn't already exist
     */
    public static void create() {

        IndexerQueue.init();

        String sqlString = "CREATE TABLE IF NOT EXISTS queue ("
                + " id SERIAL NOT NULL PRIMARY KEY,"
                + " layer VARCHAR(200) NOT NULL,"
                + " objtype VARCHAR(200) NOT NULL,"
                + " titlefields VARCHAR(50)[] NOT NULL,"
                + " titleformat VARCHAR(200) NOT NULL,"
                + " descriptionfields VARCHAR(50)[] NOT NULL,"
                + " descriptionformat VARCHAR(200) NOT NULL,"
                + " additionalfields VARCHAR(50)[] NULL,"
                + " jsondatafields VARCHAR(50)[] NULL,"
                + " srs INTEGER NOT NULL DEFAULT 4326,"
                + " enqueued TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + " indexed TIMESTAMP,"
                + " refresh INTEGER NOT NULL DEFAULT 1,"
                + " indexingstatus VARCHAR(100) NOT NULL DEFAULT '" + IndexingStatus.Enqueued + "',"
                + " datasettype VARCHAR(50) NOT NULL,"
                + " ogrdriver VARCHAR(4096)"
                + ")";

        try {
            Statement s = Conn.createStatement();
            int n = s.executeUpdate(sqlString);
            if (n == 1) {
                //Logger.Log("Indexer queue table created");
            } else {
                //Logger.Log("Indexer queue table already exists");
            }
            s.close();
        } catch (SQLException ex) {
            Logger.Log("An error occurred during creation of indexer queue table: " + ex.toString());
        }

    }

    public static ResultSet executeQuery(String sql) {
        Statement s = null;
        try {
            s = Conn.createStatement();
            return s.executeQuery(sql);
        } catch (Exception e) {
            Logger.Log("An error occurred during executeQuery operation: " + e.toString());
            return null;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException ex) {
                    Logger.Log("Could not close SQL statement: " + ex.toString());
                }
            }
        }

    }

    public static void executeUpdate(String sql) {
        Statement s = null;
        try {
            s = Conn.createStatement();
            s.executeUpdate(sql);
            s.close();
        } catch (Exception e) {
            Logger.Log("An error occurred during executeUpdate operation: " + e.toString());
        } finally {
            try {
                if (s != null) {
                    s.close();
                }
            } catch (SQLException ex) {
                Logger.Log("Could not close SQL statement: " + ex.toString());
            }
        }
    }

    /**
     * Initialize Derby database connection
     */
    public static void init() {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.Log("Could not load PostgreSQL JDBC driver: " + ex.toString());
            return;
        }

        try {
            if (IndexerQueue.Conn == null) {
                IndexerQueue.Conn = DriverManager.getConnection(Settings.INDEXDB.getJdbcUrl());
            }
        } catch (SQLException ex) {
            Logger.Log("Error while initializing JDBC driver connection: " + ex.toString());
        }

    }

    /**
     * Add an element to the queue
     *
     * @param entry
     */
    public static void enqueue(QueueItem entry) throws SQLException {
        entry.insert();
    }

    /**
     * Convert a List<String> to an SQL array
     *
     * @param stringArray
     * @return
     * @throws SQLException
     */
    public static Array createArrayOf(List<String> stringList) throws SQLException {
        return Conn.createArrayOf("VARCHAR", stringList.toArray(new String[0]));
    }

    /**
     * Create a new prepared statement
     *
     * @param sql
     * @return
     * @throws java.sql.SQLException
     */
    public static PreparedStatement prepare(String sql) throws SQLException {
        return Conn.prepareStatement(sql);
    }

    /**
     * Get 'n' entries from the queue ordered from oldest to newest
     *
     * @param numberOfEntriesToReturn
     * @return
     */
    public static List<QueueItem> top(int numberOfEntriesToReturn) {

        IndexerQueue.init();

        ResultSet r;
        try {
            List<QueueItem> l = new ArrayList<>();
            String sql = String.format("SELECT * FROM queue WHERE indexingstatus = '%s' ORDER BY enqueued ASC", IndexingStatus.Enqueued);
            Statement s = Conn.createStatement();
            s.setMaxRows(numberOfEntriesToReturn);
            r = s.executeQuery(sql);
            while (r.next()) {
                QueueItem qi = new QueueItem();
                qi.id = r.getInt("id");
                qi.layer = r.getString("layer");
                qi.srs = r.getInt("srs");
                qi.objtype = r.getString("objtype");
                qi.datasettype = DatasetType.valueOf(r.getString("datasettype"));
                qi.status = IndexingStatus.valueOf(r.getString("indexingstatus"));
                qi.enqueued = new DateTime(r.getTimestamp("enqueued"));
                switch (qi.datasettype) {
                    case Shapefile:
                        qi.ogrdriver = (ShapefileDriver) JsonReader.jsonToJava(r.getString("ogrdriver"));
                        break;
                    default:
                        qi.ogrdriver = null;
                }
                qi.titlefields = Arrays.asList((String[]) r.getArray("titlefields").getArray());;
                qi.titleformat = r.getString("titleformat");
                qi.descriptionfields = Arrays.asList((String[]) r.getArray("descriptionfields").getArray());
                qi.descriptionformat = r.getString("descriptionformat");
                if (r.getArray("additionalfields") != null) {
                    qi.additionalfields = Arrays.asList((String[]) r.getArray("additionalfields").getArray());
                }
                if (r.getArray("jsondatafields") != null) {
                    qi.jsondatafields = Arrays.asList((String[]) r.getArray("jsondatafields").getArray());
                }
                l.add(qi);
            }

            r.close();
            s.close();

            return l;
        } catch (Exception ex) {
            Logger.Log(ex.toString());
            return null;
        }
    }

}
