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
public class ShapefileDriver extends OGRDriver {

    public String filenameOfShpFile;

    /**
     * Constructor
     *
     * @param filenameOfShpFile
     */
    public ShapefileDriver(String filenameOfShpFile) {
        this.datasetType = DatasetType.Shapefile;
        this.filenameOfShpFile = filenameOfShpFile;
    }

    @Override
    public Layer getLayer() {

        DataSource ds = null;
        Layer lyr = null;

        try {

            File file = new File(filenameOfShpFile);
            if (!file.exists()) {
                System.out.println("File does not exist");
            }
            file = null;

            ds = ogr.Open(filenameOfShpFile, false);
            if (ds == null) {
                System.out.println("Invalid datasource: " + filenameOfShpFile);
                return null;
            }

            lyr = ds.GetLayer(0);

            if (lyr == null) {
                System.out.println("Invalid layer");
                return null;
            }

        } catch (Exception e) {
            System.out.println("Error opening ESRI Shapefile");
            System.out.println(e.toString());
        } finally {
            if (ds != null) {
                ds.delete();
            }
        }

        return lyr;
    }

}
