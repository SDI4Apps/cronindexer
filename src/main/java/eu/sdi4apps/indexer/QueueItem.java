package eu.sdi4apps.indexer;

import eu.sdi4apps.openapi.utils.Logger;
import eu.sdi4apps.indexer.drivers.OGRDriver;
import com.cedarsoftware.util.io.JsonWriter;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static java.sql.Types.NULL;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;

/**
 * Indexer queue item
 */
public class QueueItem {

    /**
     * Unique PK of the entry in the database
     */
    public int id = -1;

    /**
     * Name of dataset to be indexed
     */
    public String layer;

    /**
     * Time when the dataset was enqueued
     */
    public DateTime enqueued = null;

    /**
     * Time when the dataset was indexed
     */
    public DateTime indexed = null;

    /**
     * A list of of one or more fields to be used in the display title
     */
    public List<String> titlefields;

    /**
     * A format string to be used to generate a display title the fields listed
     * in title fields
     */
    public String titleformat = "%s";

    /**
     * A list of one or more fields to be used as description
     */
    public List<String> descriptionfields;

    /**
     * A format string to be used to generate a description using the fields
     * listed in description fields
     */
    public String descriptionformat = "%s";

    /**
     * Additional fields to be indexed
     */
    public List<String> additionalfields = null;

    /**
     * Fields to be included as a JSON object
     */
    public List<String> jsondatafields = null;
    
    /**
     * A list of fields to be excluded from indexing
     */
    public List<String> excludefields;

    /**
     * SRS id of the layer to be indexed
     */
    public int srs = 4326;

    /**
     * Object type contained within the layer to be indexed
     */
    public String objtype = "general";

    /**
     * An interval at which the indexed data should be re-indexed specified in
     * minutes.
     *
     * 0 = never re-index
     */
    public int refresh = 0;

    /**
     * The current status of the layer in the queue
     */
    public IndexingStatus status = IndexingStatus.Enqueued;

    /**
     * Whether the dataset is an ESRI Shapefile, a KML document, a GeoJSON file
     * etc.
     */
    public DatasetType datasettype;

    /**
     * An OGR driver object that is used to connect to the data source and
     * create a layer
     */
    public OGRDriver ogrdriver;

    /**
     * Constructor
     */
    QueueItem() {
    }

    /**
     * create a new queue item with a multiple title and description fields and
     * formats
     *
     * @param datasetname
     * @param objtype
     * @param datasettype
     * @param ogrdriver
     * @param titlefields
     * @param titleformat
     * @param descriptionfields
     * @param descriptionformat
     * @param additionalfields
     * @param jsondatafields
     * @return
     */
    public static QueueItem create(
            String datasetname,
            String objtype,
            DatasetType datasettype,
            OGRDriver ogrdriver,
            List<String> titlefields,
            String titleformat,
            List<String> descriptionfields,
            String descriptionformat,
            List<String> additionalfields,
            List<String> jsondatafields,
            Integer srs
    ) {
        QueueItem item = new QueueItem();
        item.layer = datasetname;
        item.datasettype = datasettype;
        item.objtype = objtype;
        item.ogrdriver = ogrdriver;
        item.titlefields = titlefields;
        item.titleformat = titleformat;
        item.descriptionfields = descriptionfields;
        item.descriptionformat = descriptionformat;
        item.additionalfields = additionalfields;
        item.jsondatafields = jsondatafields;
        item.srs = srs;
        return item;
    }

    /**
     * Index a layer with simple
     *
     * @param datasetname
     * @param objtype
     * @param datasettype
     * @param ogrdriver
     * @param titlefields
     * @param titleformat
     * @param descriptionfields
     * @param descriptionformat
     * @return
     */
    public static QueueItem create(String datasetname,
            String objtype,
            DatasetType datasettype,
            OGRDriver ogrdriver,
            List<String> titlefields,
            String titleformat,
            List<String> descriptionfields,
            String descriptionformat) {
        return create(
                datasetname,
                objtype,
                datasettype,
                ogrdriver,
                titlefields,
                titleformat,
                descriptionfields,
                descriptionformat,
                null,
                null,
                4326);
    }

    /**
     * Index a layer with single field mapping to title and description
     *
     * @param datasetname
     * @param objtype
     * @param datasettype
     * @param ogrdriver
     * @param titleField
     * @param descriptionField
     * @return
     */
    public static QueueItem create(
            String datasetname,
            String objtype,
            DatasetType datasettype,
            OGRDriver ogrdriver,
            String titleField,
            String descriptionField) {

        return create(datasetname,
                objtype,
                datasettype,
                ogrdriver,
                asList(titleField),
                "%s",
                asList(descriptionField),
                "%s",
                null,
                null,
                4326);
    }

    public void insert() throws SQLException {
        PreparedStatement s = IndexerQueue.prepare("INSERT INTO queue (layer, datasettype, ogrdriver, titlefields, titleformat, descriptionfields, descriptionformat, additionalfields, jsondatafields, srs, objtype)"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        s.setString(1, this.layer);
        s.setString(2, this.datasettype.toString());
        s.setString(3, JsonWriter.objectToJson(this.ogrdriver));
        s.setArray(4, IndexerQueue.createArrayOf(this.titlefields));
        s.setString(5, this.titleformat);
        s.setArray(6, IndexerQueue.createArrayOf(this.descriptionfields));
        s.setString(7, this.descriptionformat);
        if (this.additionalfields != null) {
            s.setArray(8, IndexerQueue.createArrayOf(this.additionalfields));
        } else {
            s.setNull(8, NULL);
        }

        if (this.jsondatafields != null) {
            s.setArray(9, IndexerQueue.createArrayOf(this.jsondatafields));
        } else {
            s.setNull(9, NULL);
        }
        s.setInt(10, this.srs);
        s.setString(11, this.objtype);

        s.execute();

        Logger.Log("Added layer '" + this.layer + "' to indxing queue");
        
    }

    public void updateIndexingStatus(IndexingStatus newIndexingStatus) throws SQLException {
        if (this.id != -1) {
            PreparedStatement ps = IndexerQueue.prepare("UPDATE queue SET indexingstatus = ? WHERE id=?");
            ps.setString(1, newIndexingStatus.name());
            ps.setInt(2, this.id);
            ps.execute();
        }
    }

    public void delete() throws SQLException {
        PreparedStatement ps = IndexerQueue.prepare("DELETE FROM queue WHERE id = ?");
        ps.setInt(1, this.id);
        ps.execute();
    }

}
