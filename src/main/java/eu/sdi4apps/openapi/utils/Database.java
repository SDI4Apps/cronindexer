package eu.sdi4apps.openapi.utils;

import eu.sdi4apps.openapi.types.DbParams;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author runarbe
 */
public class Database {

    /**
     * Database connection
     */
    private Connection connection;

    /**
     * Database statement
     */
    private Statement statement;

    /**
     * Constructor
     *
     * @param connectionUrl
     */
    public Database(String connectionUrl) throws SQLException {
        connection = DriverManager.getConnection(connectionUrl);
        statement = connection.createStatement();
    }

    /**
     * Open the database connection
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static Database Open(DbParams dbParams) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        String url = dbParams.getJdbcUrl();
        return new Database(url);
    }

    /**
     * Close the database connection
     *
     */
    public void Close() throws SQLException {
        try {
            connection.close();
        } catch (Exception ex) {

        }
        return;
    }

    /**
     * Executes an SQL query and returns the result
     *
     * @param sql
     * @return ResultSet on success, null on failure
     * @throws SQLException
     */
    public ResultSet Query(String sql) throws SQLException {
        return statement.executeQuery(sql);
    }

    /**
     * Executes an SQL insert/update/delete query and returns the resulting keys
     * or an empty result set if none
     *
     * @param sql
     * @return
     * @throws SQLException
     */
    public ResultSet Execute(String sql) throws SQLException {
        statement.executeUpdate(sql);
        return statement.getGeneratedKeys();
    }
    
    public ResultSet Delete(String sql) throws SQLException {
        statement.execute(sql);
        return statement.getGeneratedKeys();
    }

}
