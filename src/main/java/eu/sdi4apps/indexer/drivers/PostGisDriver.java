/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sdi4apps.indexer.drivers;

import eu.sdi4apps.indexer.DatasetType;
import java.io.File;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

/**
 * Driver to read ESRI Shapefile
 *
 * @author runarbe
 */
public class PostGisDriver extends OGRDriver {

    /**
     * Host name of database server
     */
    public String host;

    /**
     * Port number of database server
     */
    public int port;

    /**
     * Name of database
     */
    public String database;

    /**
     * Name of database user
     */
    public String username;

    /**
     * Password of database user
     */
    public String password;

    /**
     * Constructor
     *
     */
    public PostGisDriver(String host, int port, String database, String username, String password) {
        this.datasetType = DatasetType.PostGIS;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public Layer getLayer() {

        DataSource ds = null;
        Layer lyr = null;

        return lyr;
    }

}
