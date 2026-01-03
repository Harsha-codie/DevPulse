package ai.cropplanner.soil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SoilData {
    public double clay;
    public double sand;
    public double silt;
    public double phh2o;
    public String source; // Track data source: SoilGrids, OpenLandMap, Cache, Manual, Fallback

    public static SoilData fromJson(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        SoilData data = new SoilData();
        // Parse mean values for top layer (0-5cm)
        data.clay = getMean(root, "clay");
        data.sand = getMean(root, "sand");
        data.silt = getMean(root, "silt");
        data.phh2o = getMean(root, "phh2o");
        return data;
    }

    private static double getMean(JsonNode root, String property) {
        try {
            JsonNode layers = root.path("properties").path("layers");
            for (JsonNode layer : layers) {
                String name = layer.path("name").asText("");
                if (name.equals(property)) {
                    JsonNode depths = layer.path("depths");
                    for (JsonNode depth : depths) {
                        String label = depth.path("label").asText("");
                        if (label.equals("0-5cm")) {
                            double mean = depth.path("values").path("mean").asDouble(Double.NaN);
                            // Apply d_factor conversion if needed
                            int dFactor = layer.path("unit_measure").path("d_factor").asInt(1);
                            return mean / dFactor;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return Double.NaN;
    }
}
