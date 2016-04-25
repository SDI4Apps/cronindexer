/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sdi4apps.openapi.utils;

import com.google.gson.GsonBuilder;
import eu.sdi4apps.openapi.types.ResultSetAdapter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author runarbe
 */
public class ResultSetUtil {

    /**
     * Convert a result set object into a json string
     *
     * @param rs
     * @return
     */
    public static String ToJson(ResultSet rs) {

        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(ResultSet.class, new ResultSetAdapter());
        return gson.create().toJson(rs, ResultSet.class);
    }

    /**
     * Convert a result set object into a list of hashtables
     *
     * @param resultSet
     * @return
     * @throws SQLException
     */
    public static List<HashMap> ToHashMap(ResultSet resultSet) throws SQLException {
        List<HashMap> records = new ArrayList<>();
        ResultSetMetaData m = resultSet.getMetaData();
        int numCols = m.getColumnCount();

        while (resultSet.next()) {
            HashMap h = new HashMap(numCols);
            for (int i = 1; i <= numCols; i++) {
                h.put(m.getColumnName(i), resultSet.getObject(i));
            }
            records.add(h);
        };

        return records;
    }

}
