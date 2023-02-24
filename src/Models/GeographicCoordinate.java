package Models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class GeographicCoordinate {
    public static final int EARTH_RADIUS_KM = 6371;

    private final double lat;
    private final double lon;

    public GeographicCoordinate(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    private double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    public double distanceInKmBetweenEarthCoordinates(GeographicCoordinate geoCordi) {

        double dLat = degreesToRadians(geoCordi.getLat() - this.lat);
        double dLon = degreesToRadians(geoCordi.getLon() - this.lon);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(degreesToRadians(this.lat)) * Math.cos(degreesToRadians(geoCordi.getLat()));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Double.parseDouble(BigDecimal.valueOf(EARTH_RADIUS_KM * c).setScale(3, RoundingMode.HALF_UP).toString());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeographicCoordinate that = (GeographicCoordinate) o;
        return Double.compare(that.lat, lat) == 0 && Double.compare(that.lon, lon) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
    }

    @Override
    public String toString() {
        return lat + " - " + lon;
    }
}
