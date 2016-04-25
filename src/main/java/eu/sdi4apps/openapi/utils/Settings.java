package eu.sdi4apps.openapi.utils;

import eu.sdi4apps.indexer.IndexerQueue;
import eu.sdi4apps.openapi.types.DbParams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.commons.lang.math.NumberUtils;
import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;

/**
 * Configuration parameters
 *
 * @author runarbe
 */
public class Settings {

    /**
     * Instantiate static variables
     */
    static {

        InputStream inputStream = null;

        try {

            Properties prop = new Properties();
            String propFileName = "openapi.properties";

            inputStream = Settings.class.getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new Exception("Configuration file: '" + propFileName + "' not found in the classpath");
            }

            // Load indexdb properties
            String indexdb = prop.getProperty("indexdb");
            String indexdb_usr = prop.getProperty("indexdb_usr");
            String indexdb_pwd = prop.getProperty("indexdb_pwd");
            String indexdb_host = prop.getProperty("indexdb_host");
            Integer indexdb_port = NumberUtils.createInteger(prop.getProperty("indexdb_port"));
            INDEXDB = new DbParams(indexdb, indexdb_usr, indexdb_pwd, indexdb_host, indexdb_port);


            // Load index directory configuration
            String indexdir = prop.getProperty("indexdir");
            INDEXDIR = indexdir;
            if (!Files.exists(Paths.get(indexdir))) {
                Files.createDirectories(Paths.get(indexdir));
            }


            // Load shape directory configuration
            String shapedir = prop.getProperty("shapedir");
            SHAPEDIR = shapedir;

            if (!Files.exists(Paths.get(shapedir))) {
                Files.createDirectories(Paths.get(shapedir));
            }
            
            // Load log directory configuration
            String logdir = prop.getProperty("logdir");
            LOGDIR = logdir;
            if (!Files.exists(Paths.get(logdir))) {
                Files.createDirectories(Paths.get(logdir));
            }
            
            // Load GDAL/OGR 
            try {
                gdal.AllRegister();
                ogr.RegisterAll();
            } catch (Exception ex) {
                System.out.println("Failed to register GDAL/OGR: " + ex.toString());
            }

            // Initialize queue database
            IndexerQueue.create();

            // Set ready flag
            Settings.isReady = true;

        } catch (Exception e) {
            // If exception occurs, print error message to std out and set isReady flag to false
            System.out.println("Error during loading of configuration: " + e);
            Settings.isReady = false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ex) {
                // If exception occurs during close of inputStream, print error message to std out and set isReady flag to false
                System.out.println("Unable to close configuration file: " + ex.toString());
                Settings.isReady = false;
            }
        }
    }

    /**
     * Default number of results to return from a search
     */
    public static int NUMRESULTS = 25;

    /**
     * Default boost to apply to terms in title (at index time)
     */
    public static final float TITLEBOOST = (float) 1.3;

    /**
     * Default boost to apply to terms in description (at index time)
     */
    public static final float DESCRIPTIONBOOST = (float) 1.2;

    /**
     * Default boost to apply to matches within bounding box (at query time)
     */
    public static final float SPATIALBOOST = (float) 3;

    /**
     * A flag to indicate whether the configuration file has been read properly
     */
    public static Boolean isReady = false;

    /**
     * Connection parameters for indexer db
     */
    public static DbParams INDEXDB; //platform.sdi4apps.eu

    /**
     * The directory path to where the Lucene index should be stored
     */
    public static String INDEXDIR;

    /**
     * The directory where shape files to be indexed are located
     */
    public static String SHAPEDIR;

    /**
     * The directory where log files are to be written
     */
    public static String LOGDIR;

}
