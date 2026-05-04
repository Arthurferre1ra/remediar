package br.com.remediar.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class GeoDistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    public BigDecimal distanceKm(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double latitude1 = Math.toRadians(lat1.doubleValue());
        double latitude2 = Math.toRadians(lat2.doubleValue());
        double deltaLatitude = Math.toRadians(lat2.subtract(lat1).doubleValue());
        double deltaLongitude = Math.toRadians(lon2.subtract(lon1).doubleValue());

        double a = Math.sin(deltaLatitude / 2) * Math.sin(deltaLatitude / 2)
                + Math.cos(latitude1) * Math.cos(latitude2)
                * Math.sin(deltaLongitude / 2) * Math.sin(deltaLongitude / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(EARTH_RADIUS_KM * c).setScale(3, RoundingMode.HALF_UP);
    }
}
