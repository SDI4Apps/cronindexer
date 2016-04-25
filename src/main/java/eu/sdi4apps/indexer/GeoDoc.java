/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sdi4apps.indexer;

import eu.sdi4apps.openapi.utils.Settings;
import java.util.UUID;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

/**
 * Describes a document to be indexed
 *
 * @author runarbe
 */
public class GeoDoc {

    /**
     * The relevance score of the search result Only populated upon searches
     */
    public float Score;

    /**
     * Unique id of document
     */
    public String Id;

    /**
     * The layer that the document belongs to
     */
    public String Layer;

    /**
     * The type of object represented by the document
     */
    public String ObjType;

    /**
     * Full WKT geometry
     */
    public String FullGeom;

    /**
     * Point WKT geometry (point on line, point inside for polygons)
     */
    public String PointGeom;

    /**
     * Title of document for display in search results
     */
    public String DisplayTitle;

    /**
     * Description of document for display in search results
     */
    public String DisplayDescription;

    /**
     * Title field to be indexed
     */
    private transient String IndexTitle;

    /**
     * Description to be indexed
     */
    private transient String IndexDescription;

    /**
     * Additional fields to be indexed
     */
    private transient String IndexAdditional = "";

    /**
     * A JSON object that can be stored as part of the document
     */
    public Object JsonData = null;

    /**
     * Parameterless constructor to create a new instance of a GeoDoc
     */
    public GeoDoc() {

    }

    /**
     * Create a new GeoDoc
     *
     * @param layer
     * @param fullGeom
     * @param pointGeom
     * @param title
     * @param description
     * @param indexTitle
     * @param jsonData
     * @param indexAdditional
     * @param indexDescription
     * @return
     */
    public static GeoDoc create(
            String id,
            String layer,
            String objType,
            String fullGeom,
            String pointGeom,
            String title,
            String description,
            String indexTitle,
            String indexDescription,
            String indexAdditional,
            Object jsonData) {
        GeoDoc gd = new GeoDoc();
        gd.Id = id;
        gd.Layer = layer;
        gd.ObjType = objType;
        gd.FullGeom = fullGeom;
        gd.PointGeom = pointGeom;
        gd.DisplayTitle = title;
        gd.DisplayDescription = description;
        gd.IndexTitle = indexTitle;
        gd.IndexDescription = indexDescription;
        if (indexAdditional != null) {
            gd.IndexAdditional = indexAdditional;
        }
        gd.JsonData = jsonData;
        return gd;
    }

    /**
     * Add additional values to be indexed but not displayed to the document
     *
     * @param values
     */
    public void indexValues(String values) {
        this.IndexAdditional += values;
    }

    /**
     * Add custom JSON data object to the document
     *
     * @param jsonData
     */
    public void setJsonData(String jsonData) {
        this.JsonData = jsonData;
    }

    public Document asLuceneDoc() {
        Document d = null;
        try {
            d = new Document();
            d.add(new Field("Id", this.Id, Store.YES, Index.NOT_ANALYZED));
            d.add(new Field("Layer", this.Layer, Store.YES, Index.NOT_ANALYZED));
            d.add(new Field("ObjType", this.ObjType, Store.YES, Index.NOT_ANALYZED));
            d.add(new Field("FullGeom", this.FullGeom, Store.YES, Index.NO));
            d.add(new Field("PointGeom", this.PointGeom, Store.YES, Index.NO));
            d.add(new Field("DisplayTitle", this.DisplayTitle, Store.YES, Index.NO));
            d.add(new Field("DisplayDescription", this.DisplayDescription, Store.YES, Index.NO));

            Field indexTitle = new Field("IndexTitle", this.IndexTitle, Store.NO, Index.ANALYZED);
            indexTitle.setBoost(Settings.TITLEBOOST);
            d.add(indexTitle);

            Field indexDescription = new Field("IndexDescription", this.IndexDescription, Store.NO, Index.ANALYZED);
            indexDescription.setBoost(Settings.DESCRIPTIONBOOST);
            d.add(indexDescription);

            d.add(new Field("IndexAdditional", this.IndexAdditional, Store.NO, Index.ANALYZED));
            if (this.JsonData != null) {
                d.add(new Field("JsonData", Serializer.Serialize(this.JsonData), Store.YES, Index.NO));
            }
        } catch (Exception e) {
            System.out.println("Error converting GeoDoc to LuceneDoc: " + e.toString());
        }
        return d;
    }

}
