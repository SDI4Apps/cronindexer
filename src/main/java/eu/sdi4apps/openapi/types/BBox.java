package eu.sdi4apps.openapi.types;

import com.google.gson.Gson;
import eu.sdi4apps.openapi.exceptions.GeneralException;
import eu.sdi4apps.openapi.utils.Logger;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class BBox {

    public double minX = 180;
    public double minY = 90;
    public double maxX = -180;
    public double maxY = -90;

    public BBox() {

    }

    public BBox addPoint(double lon, double lat) {

        if (lon > this.maxX) {
            this.maxX = lon;
        }

        if (lon < this.minX) {
            this.minX = lon;
        }

        if (lat < this.minY) {
            this.minY = lat;
        }

        if (lat > this.maxY) {
            this.maxY = lat;
        }

        return this.normalize();

    }

    public BBox addRect(double minLon, double minLat, double maxLon, double maxLat) {
        if (maxLon > this.maxX) {
            this.maxX = maxLon;
        }

        if (minLon < this.minX) {
            this.minX = minLon;
        }

        if (minLat < this.minY) {
            this.minY = minLat;
        }

        if (maxLat > this.maxY) {
            this.maxY = maxLat;
        }

        return this.normalize();

    }

    public double height() {
        return this.maxY - this.minY;
    }

    public double width() {
        return this.maxX - this.minX;
    }

    public BBox extendBy(double factor) {
        factor = factor / 2;
        double dX = this.width() * factor;
        double dY = this.height() * factor;
        this.minX -= dX;
        this.maxX += dX;
        this.maxY += dY;
        this.minY -= dY;
        return this.normalize();
    }

    public BBox bufferBy(double buffer) {
        this.minX -= buffer;
        this.minY -= buffer;
        this.maxX += buffer;
        this.maxY += buffer;
        return this.normalize();
    }

    public static BBox parseJsonBBox(String jsonBBox) throws GeneralException {
        Gson gson = new Gson();
        return gson.fromJson(jsonBBox, BBox.class);
    }

    public BBox minSize(double meters) {
        double cLon = (this.minX + this.minX) / 2;
        double cLat = (this.minY + this.maxY) / 2;

        double minSize = LonLat.metersToDegrees(cLon, cLat, meters);
        double dXY = minSize / 2;

        if (this.width() < minSize) {
            this.minX = cLon - dXY;
            this.maxX = cLon + dXY;
        }

        if (this.height() < minSize) {
            this.minY = cLat - dXY;
            this.maxY = cLat + dXY;
        }

        return this.normalize();
    }

    public BBox normalize() {

        if (this.minX < -180) {
            this.minX = -180;
        }

        if (this.maxX > 180) {
            this.maxX = 180;
        }

        if (this.maxY > 90) {
            this.maxY = 90;
        }

        if (this.minY < -90) {
            this.minY = -90;
        }
        return this;
    }

    public static BBox createFromPoints(double lon1, double lat1, double lon2, double lat2) {

        BBox bbox = new BBox();

        if (lat2 > lat1) {
            bbox.maxY = lat2;
            bbox.minY = lat1;
        } else {
            bbox.maxY = lat1;
            bbox.minY = lat2;
        }

        if (lon2 > lon1) {
            bbox.maxX = lon2;
            bbox.minX = lon1;
        } else {
            bbox.maxX = lon1;
            bbox.minX = lon2;
        }

        return bbox.normalize();

    }

    public static BBox createFromString(String extent) {

        if (extent == null) {
            return null;
        }

        String[] stringParts = StringUtils.split(extent, ",");
        if (stringParts.length != 4) {
            return null;
        }

        Double[] doubleParts = new Double[4];

        int i = 0;
        for (String s : stringParts) {
            doubleParts[i] = NumberUtils.toDouble(s);
            i++;
        }

        BBox bbox = new BBox();

        bbox.minX = doubleParts[0];
        bbox.minY = doubleParts[1];
        bbox.maxX = doubleParts[2];
        bbox.maxY = doubleParts[3];

        return bbox.normalize();

    }

    public String wktEnvelope() {
        return String.format("ST_MakeEnvelope(%s, %s, %s, %s, 4326)", this.minX, this.minY, this.maxX, this.maxY);
    }

    public String jsArray() {
        return String.format("[%s,%s,%s,%s]", this.minX, this.minY, this.maxX, this.maxY);
    }

    public static BBox createFromPoint(double lon, double lat, double buffer) {
        BBox bbox = new BBox();
        bbox.addPoint(lon, lat);
        bbox.bufferBy(buffer);
        return bbox;
    }

    public static BBox createFromPointMeters(double lon, double lat, double bufferInMeters) {
        BBox bbox = new BBox();
        bbox.addPoint(lon, lat);
        bbox.bufferBy(LonLat.metersToDegrees(lon, lat, bufferInMeters));
        return bbox;
    }

}
