package eu.sdi4apps.openapi.types;

import java.io.*;
import java.sql.*;
import com.google.gson.*;
import com.google.gson.stream.*;

public class ResultSetAdapter extends TypeAdapter<ResultSet> {
    public static class NotImplemented extends RuntimeException {}
    private static final Gson gson = new Gson();
    public ResultSet read(JsonReader reader)
        throws IOException {
        throw new NotImplemented();
    }

    public void write(JsonWriter writer, ResultSet rs)
        throws IOException {
        try {
            ResultSetMetaData meta = rs.getMetaData();
            int cc = meta.getColumnCount();

            writer.beginArray();
            while (rs.next()) {
                writer.beginObject();
                for (int i = 1; i <= cc; ++i) {
                    writer.name(meta.getColumnName(i));
                    Class<?> type = Class.forName(meta.getColumnClassName(i));
                    gson.toJson(rs.getObject(i), type, writer);
                    //writer.value(rs.getString(i));
                }
                writer.endObject();
            }
            writer.endArray();
        } catch (SQLException e) {
            throw new RuntimeException(e.getClass().getName(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getClass().getName(), e);
        }
    }
}