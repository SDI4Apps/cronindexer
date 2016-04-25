/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sdi4apps.openapi.types;

/**
 *
 * @author runarbe
 */
public class DbParams {

    public String dbName = "routing";
    public String dbUser = "postgres";
    public String dbPassword = "postgres";
    public int dbPort = 5432;
    public String dbHost = "localhost";

    public DbParams(String dbName, String dbUser, String dbPassword, String dbHost, Integer dbPort) throws Exception {

        try {
            if (dbName == null) {
                throw new Exception("Parameter dbName is null");
            } else {
                this.dbName = dbName;
            }

            if (dbUser == null) {
                throw new Exception("Parameter dbUser is null");
            } else {
                this.dbUser = dbUser;
            }

            if (dbPassword == null) {
                throw new Exception("Parameter dbPassword is null");
            } else {
                this.dbPassword = dbPassword;
            }
            
            this.dbPort = dbPort;
            
            this.dbHost = dbHost;
            
        } catch (Exception exception) {
            throw new Exception("Failed to create database parameters: " + exception.toString());
        }
    }

    public DbParams(String dbName, String dbUser, String dbPassword, String dbHost) throws Exception {
        this(dbName, dbUser, dbPassword, dbHost, 5432);
    }

    public DbParams(String dbName, String dbUser, String dbPassword) throws Exception {
        this(dbName, dbUser, dbPassword, "localhost", 5432);
    }

    /**
     * Return the JDBC URL of the respective setting
     *
     * @return
     */
    public String getJdbcUrl() {
        return "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName + "?user=" + dbUser + "&password=" + dbPassword;
    }
}
