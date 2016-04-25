/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sdi4apps.indexer;

import eu.sdi4apps.indexer.drivers.ShapefileDriver;
import eu.sdi4apps.openapi.utils.Logger;
import java.io.File;
import java.util.List;
import org.apache.lucene.index.IndexWriter;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
 *
 * @author runarbe
 */
public class CronJob {

    public static int CheckCount = 0;

    public static DateTime StartTime = DateTime.now();

    public static Boolean IsWorking = false;

    /**
     * Main runnable 
     * 
     * @param args
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                if (CronJob.IsWorking == true) {
                    // Logger.Log("Previous indexing job still in progress");
                    return;
                }

                IsWorking = true;

                try {

                    DateTime currentTime = DateTime.now();

                    int runningTime = Seconds.secondsBetween(StartTime, currentTime).getSeconds();

                    CheckCount++;

                    List<QueueItem> pickedItems = IndexerQueue.top(1);
                    if (pickedItems.size() == 0) {
                        Logger.Log("No items in queue");
                    }

                    for (QueueItem qi : pickedItems) {

                        Logger.Log("Processing entry: " + qi.layer + " added " + qi.enqueued);

                        IndexWriter w = Indexer.getWriter();

                        switch (qi.datasettype) {
                            case Shapefile:
                                ShapefileDriver drv = (ShapefileDriver) qi.ogrdriver;
                                if (drv == null) {
                                    throw new Exception("Could not deserialize queue item into ogr driver: " + qi.ogrdriver);
                                }
                                File f = new File(drv.filenameOfShpFile);

                                if (!f.exists()) {
                                    throw new Exception("Enqueued source file '" + drv.filenameOfShpFile + "' does not exist");
                                }

                                if (!f.canRead()) {
                                    throw new Exception("No file read access to '" + drv.filenameOfShpFile + "'");
                                }

                                DataSource ds = ogr.Open(drv.filenameOfShpFile, 0);
                                if (ds == null) {
                                    throw new Exception("Could not open data source'" + drv.filenameOfShpFile + "' using gdal/ogr");
                                }

                                Layer lyr = ds.GetLayer(0);
                                if (lyr == null) {
                                    throw new Exception("Could not get layer from source file '" + drv.filenameOfShpFile + "' using gdal/ogr");
                                }

                                Indexer.indexLayer(lyr, qi, w);

                                break;
                            default:
                                Logger.Log("Currently only supports ESRI Shapefiles: " + qi.datasettype);
                                drv = null;
                                break;
                        }

                        // Set layer status to indexed
                        qi.updateIndexingStatus(IndexingStatus.Indexed);
                    }

                } catch (Exception e) {
                    Logger.Log("An indexing error occurred: " + e.toString());
                } finally {
                    Indexer.closeWriter();
                    IsWorking = false;
                }

            }
        });
    }
}
