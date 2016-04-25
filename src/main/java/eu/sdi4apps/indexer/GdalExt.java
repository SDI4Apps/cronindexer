/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sdi4apps.indexer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Layer;

/**
 *
 * @author runarbe
 */
public class GdalExt {

    public static LinkedHashMap getFieldMap(Layer lyr, List<String> fieldList) {

        LinkedHashMap<String, Boolean> m = new LinkedHashMap<>();

        if (lyr == null || fieldList == null || fieldList.size() == 0) {
            return m;
        }

        FeatureDefn lyrDefn = lyr.GetLayerDefn();
        int numberOfFields = lyrDefn.GetFieldCount();

        // Set all fields to false by default
        for (String fieldName
                : fieldList) {
            m.put(fieldName, false);
        }

        // Loop through available fields and set matches to true
        for (int i = 0; i < numberOfFields; i++) {
            FieldDefn fd = lyrDefn.GetFieldDefn(i);
            if (m.containsKey(fd.GetName())) {
                m.put(fd.GetName(), true);
            }
        }

        return m;
    }

    public static String[] getFormattedAndIndexValuesAsString(Feature feat, LinkedHashMap<String, Boolean> fieldList, String stringFormat) {

        String[] s = new String[2];

        if (feat == null || fieldList == null || stringFormat == null || fieldList.size() == 0) {
            return s;
        }

        Object[] values = new Object[fieldList.size()];
        List<String> indexValues = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Boolean> field : fieldList.entrySet()) {
            if (field.getValue() == false) {
                values[i] = "![missing field: " + field.getKey() + "]";
            } else {
                values[i] = feat.GetFieldAsString(field.getKey());
                indexValues.add(feat.GetFieldAsString(field.getKey()));
            }
            i++;
        }
        s[0] = String.format(stringFormat, values);
        s[1] = StringUtils.join(indexValues, " ");
        return s;
    }

    public static LinkedHashMap getAsMapObject(Feature feat, LinkedHashMap<String, Boolean> fieldList) {
        LinkedHashMap<String, Object> m = new LinkedHashMap<>();
        for (Map.Entry<String, Boolean> field : fieldList.entrySet()) {
            if (field.getValue() == true) {
                m.put(field.getKey(), feat.GetFieldAsString(field.getKey()));
            }
        }
        return m;
    }

    public static String getFieldValuesAsString(Feature feat, LinkedHashMap<String, Boolean> fieldList) {
        List<String> s = new ArrayList<>();
        for (Map.Entry<String, Boolean> field : fieldList.entrySet()) {
            if (field.getValue() != false) {
                s.add(feat.GetFieldAsString(field.getKey()));
            }
        }
        return StringUtils.join(s, " ");
    }

}
