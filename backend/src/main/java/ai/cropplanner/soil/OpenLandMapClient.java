package ai.cropplanner.soil;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OpenLandMapClient {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String OPENLANDMAP_URL = "https://rest.soilgrids.org/query";
    
    public OpenLandMapClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Fetch soil data from OpenLandMap API as fallback
     * Note: OpenLandMap API endpoint may vary - using SoilGrids REST endpoint as alternative
     */
    public SoilData getSoilData(double lat, double lon) throws Exception {
        System.out.println("Attempting OpenLandMap fallback for lat=" + lat + ", lon=" + lon);
        
        // Build URL for alternative SoilGrids REST endpoint
        String url = UriComponentsBuilder.fromHttpUrl("https://rest.soilgrids.org/query")
                .queryParam("lon", lon)
                .queryParam("lat", lat)
                .queryParam("attributes", "PHIHOX,CLYPPT,SNDPPT,SLTPPT")
                .toUriString();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "CropPlanner/1.0");
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseOpenLandMapResponse(response.getBody());
            } else {
                throw new Exception("OpenLandMap returned: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("OpenLandMap fetch failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Parse OpenLandMap/alternative SoilGrids response
     */
    private SoilData parseOpenLandMapResponse(String json) throws Exception {
        try {
            JsonNode root = objectMapper.readTree(json);
            SoilData data = new SoilData();
            
            // Try to extract from properties array
            JsonNode properties = root.path("properties");
            if (properties.isArray() && properties.size() > 0) {
                JsonNode firstLayer = properties.get(0);
                
                // Extract values (units may differ from SoilGrids)
                data.clay = firstLayer.path("CLYPPT").path("M").path("0-5cm").asDouble(30.0);
                data.sand = firstLayer.path("SNDPPT").path("M").path("0-5cm").asDouble(40.0);
                data.silt = firstLayer.path("SLTPPT").path("M").path("0-5cm").asDouble(30.0);
                data.phh2o = firstLayer.path("PHIHOX").path("M").path("0-5cm").asDouble(65.0) / 10.0; // pH scale conversion
            } else {
                throw new Exception("Unexpected OpenLandMap response format");
            }
            
            data.source = "OpenLandMap";
            return data;
            
        } catch (Exception e) {
            System.err.println("Failed to parse OpenLandMap response: " + e.getMessage());
            throw new Exception("OpenLandMap parsing failed: " + e.getMessage());
        }
    }
}
