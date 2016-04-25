package eu.sdi4apps.indexer;

import com.spatial4j.core.context.SpatialContext;
import eu.sdi4apps.openapi.utils.Settings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;

/**
 * Indexer
 *
 * @author runarbe
 */
public class Indexer {

    public static StandardAnalyzer analyzer = null;

    public static IndexWriter indexWriter = null;

    public static Directory directory = null;

    public static IndexWriterConfig indexWriterConfig = null;

    public static SpatialContext spatialCtx = null;

    public static SpatialStrategy spatialStrategy = null;

    public static SpatialPrefixTree spatialPrefixTree = null;

    public static int maxSpatialIndexLevels = 11;

    public static int errorCount = 0;

    /**
     * Get the IndexWriter or null
     *
     * @return
     */
    public static IndexWriter getWriter() {
        try {
            if (indexWriter == null || indexWriter.isOpen() == false) {
                spatialCtx = SpatialContext.GEO;

                spatialPrefixTree = new GeohashPrefixTree(spatialCtx, maxSpatialIndexLevels);

                spatialStrategy = new RecursivePrefixTreeStrategy(spatialPrefixTree, "GeoField");

                analyzer = new StandardAnalyzer();
                directory = FSDirectory.open(Paths.get(Settings.INDEXDIR));
                indexWriterConfig = new IndexWriterConfig(analyzer);
                indexWriter = new IndexWriter(directory, indexWriterConfig);
            }
        } catch (Exception e) {
            System.out.println("Error opening IndexWriter: " + e.toString());
        }
        return indexWriter;
    }

    public static void dropIndex() {
        try {
            Indexer.unlockIndex();
            IndexWriter w = Indexer.getWriter();
            w.deleteAll();
            w.commit();
            w.close();
        } catch (Exception e) {
            System.out.println("An error occurred while attempting to drop index: " + e.toString());
        }
    }

    /**
     * Unlock the index directory by deleting the write.lock file
     */
    public static void unlockIndex() {
        try {
            Files.deleteIfExists(Paths.get(Settings.INDEXDIR.toString(), "write.lock"));
        } catch (IOException ex) {
            System.out.println("An error occurred while attempting to delete lock file: " + ex.toString());
        }
    }

    /**
     * Index a layer
     *
     * @param lyr
     * @param qi
     * @param w
     */
    public static void indexLayer(Layer lyr, QueueItem qi, IndexWriter w) {

        try {

            // Reset the error count at start of each run
            Indexer.errorCount = 0;

            // Set the indexing staus to under indexing
            qi.updateIndexingStatus(IndexingStatus.Indexing);

            // Declare the feature
            Feature f = null;

            // Count the total number of features in a data source
            int totalFeatures = lyr.GetFeatureCount(1);

            // Determine the batch size
            int batch = (int) Math.ceil((double)totalFeatures / (double)10);

            // Set a counter for number of features
            int counter = 1;

            // Set up configuration for a title fields, description fields, index fields and json data fields
            LinkedHashMap<String, Boolean> titleFieldMap = GdalExt.getFieldMap(lyr, qi.titlefields);
            LinkedHashMap<String, Boolean> descriptionFieldMap = GdalExt.getFieldMap(lyr, qi.descriptionfields);
            LinkedHashMap<String, Boolean> indexAdditionalFieldMap = GdalExt.getFieldMap(lyr, qi.additionalfields);
            LinkedHashMap<String, Boolean> jsonDataFieldMap = GdalExt.getFieldMap(lyr, qi.jsondatafields);

            // Loop through all the fatures
            while (null != (f = lyr.GetNextFeature())) {

                // Call the function to index individual features
                indexFeature(w, qi, f, titleFieldMap, descriptionFieldMap, indexAdditionalFieldMap, jsonDataFieldMap);

                // Check for errors and abort if too many
                if (Indexer.errorCount >= 50) {
                    throw new Exception("More than 50 errors occurred, aborting indexing operation");
                }

                // Check if percentage progress step reached or complete - output message
                if (counter % batch == 0 || counter == totalFeatures) {
                    System.out.println("Processed: " + (counter * 100 / totalFeatures) + "% (" + counter + " items)...");
                }
                
                counter++;
                
            }

            qi.updateIndexingStatus(IndexingStatus.Indexed);

            System.out.println("Done");

        } catch (Exception ex) {
            try {
                System.out.println("An error occurred while indexing layer: " + ex.toString());
                qi.updateIndexingStatus(IndexingStatus.Error);
            } catch (SQLException ex1) {
                System.out.println("Failed to set status of queue item to 'Error': " + ex.toString());
            }
        }
    }

    public static void indexFeature(IndexWriter w,
            QueueItem qi,
            Feature f,
            LinkedHashMap titleFieldMap,
            LinkedHashMap descriptionFieldMap,
            LinkedHashMap indexAdditionalFieldMap,
            LinkedHashMap jsonDataFieldMap) {

        try {
            //Read the geometry
            Geometry g = f.GetGeometryRef();

            String[] titleData = GdalExt.getFormattedAndIndexValuesAsString(f, titleFieldMap, qi.titleformat);
            String[] descData = GdalExt.getFormattedAndIndexValuesAsString(f, descriptionFieldMap, qi.descriptionformat);
            String additionalData = GdalExt.getFieldValuesAsString(f, indexAdditionalFieldMap);
            Object jsonData = GdalExt.getAsMapObject(f, jsonDataFieldMap);

            String compositeId = qi.layer + "-" + f.GetFID();

            String pointWkt = g.PointOnSurface().ExportToWkt();

            double[] shapeEnvelope = new double[4];
            g.GetEnvelope(shapeEnvelope);

            GeoDoc gd = GeoDoc.create(
                    compositeId,
                    qi.layer,
                    qi.objtype,
                    g.ExportToWkt(),
                    pointWkt,
                    titleData[0],
                    descData[0],
                    titleData[1],
                    descData[1],
                    additionalData,
                    jsonData);

            if (gd != null) {

                /**
                 * Retrieve Lucene document
                 */
                Document luceneGeoDoc = gd.asLuceneDoc();

                /**
                 * Add spatial indexing for bounding box of objects
                 */
                for (IndexableField geoField
                        : spatialStrategy.createIndexableFields(
                                spatialCtx.makeRectangle(shapeEnvelope[0],
                                        shapeEnvelope[1],
                                        shapeEnvelope[2],
                                        shapeEnvelope[3]))) {
                    luceneGeoDoc.add(geoField);
                }

                /**
                 * Write the document to the index
                 */
                w.updateDocument(new Term("Id", compositeId), luceneGeoDoc);
            }

        } catch (Exception e) {
            Indexer.errorCount++;
            System.out.println("An error occurred while writing feature to Lucene index: " + e.toString() + " (#" + Indexer.errorCount + ")");
        }

    }

    /**
     * Close the index writer
     */
    public static void closeWriter() {

        try {

            if (indexWriter != null) {
                indexWriter.close();
                indexWriter = null;
            }

            if (indexWriterConfig != null) {
                indexWriterConfig = null;
            }

            if (directory != null) {
                directory.close();
                directory = null;
            }

            if (analyzer != null) {
                analyzer.close();
                analyzer = null;
            }
        } catch (Exception e) {
            System.out.println("Error during closing of writer: " + e.toString());
        }
    }
}
