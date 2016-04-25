/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sdi4apps.indexer.drivers;

import eu.sdi4apps.indexer.DatasetType;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gdal.gdal.gdal;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

/**
 * Abstract class that defines shared functions and interface for file format
 * drivers
 *
 * @author runarbe
 */
public abstract class OGRDriver {

    public DatasetType datasetType = null;

    /**
     * Ensures that the OGR drivers are loaded Depends on org.gdal.Loader
     */
    public OGRDriver() {
    }

    /**
     * Get the current layer
     *
     * @return
     */
    public abstract Layer getLayer();

}
