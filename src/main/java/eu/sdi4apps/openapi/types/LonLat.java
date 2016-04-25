package eu.sdi4apps.openapi.types;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author runarbe
 */
public class LonLat {

    public static double metersPerDegree(double lon, double latMid) {

        double m_per_deg_lat, m_per_deg_lon;

        m_per_deg_lat = 111132.954 - 559.822 * Math.cos(2.0 * latMid) + 1.175 * Math.cos(4.0 * latMid);
        m_per_deg_lon = (3.14159265359 / 180) * 6367449 * Math.cos(latMid);

        return m_per_deg_lat;
    }

    public static double metersToDegrees(double lon, double lat, double meters) {
        return meters / LonLat.metersPerDegree(lon, lat);
    }

    public static double degreesToMeters(double lon, double lat, double degrees) {
        return LonLat.metersPerDegree(lon, lat) * degrees;
    }

}
