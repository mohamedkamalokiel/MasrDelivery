package domain;

import java.util.HashMap;
import java.util.Map;

public class DistrictDistanceMatrix {
    private static final Map<String, Double> distances = new HashMap<>();

    static {
        setDistance("Maadi", "Faisal", 12.0);
        setDistance("Dokki", "Nasr City", 15.0);
        setDistance("Heliopolis", "Maadi", 18.0);
    }

    private static String makeKey(String d1, String d2) {
        return d1.toLowerCase().compareTo(d2.toLowerCase()) <= 0
                ? d1.toLowerCase() + "_" + d2.toLowerCase()
                : d2.toLowerCase() + "_" + d1.toLowerCase();
    }

    public static void setDistance(String d1, String d2, double distanceKm) {
        distances.put(makeKey(d1, d2), distanceKm);
    }

    public static double getDistance(String d1, String d2) {
        if (d1.equalsIgnoreCase(d2)) return 0.0;
        return distances.getOrDefault(makeKey(d1, d2), 10.0); // Default 10km if undefined
    }
}