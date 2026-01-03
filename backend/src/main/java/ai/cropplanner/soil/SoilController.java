package ai.cropplanner.soil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/soil")
@CrossOrigin(origins = "*")
public class SoilController {
    @Autowired
    private SoilGridsClient soilGridsClient;

    @Value("${ipapi.base-url:https://ipapi.co}")
    private String ipApiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public ResponseEntity<?> getSoil(@RequestParam(required = false) Double lat,
                                      @RequestParam(required = false) Double lon) {
        try {
            if (lat == null || lon == null) {
                // Fallback to IP-based geolocation
                try {
                    String ipApiUrl = ipApiBaseUrl + "/json";
                    ResponseEntity<IpApiResponse> ipResp = restTemplate.getForEntity(ipApiUrl, IpApiResponse.class);
                    if (ipResp.getStatusCode().is2xxSuccessful() && ipResp.getBody() != null) {
                        lat = ipResp.getBody().latitude;
                        lon = ipResp.getBody().longitude;
                    } else {
                        // Default to Bangalore coordinates if IP lookup fails
                        lat = 12.9716;
                        lon = 77.5946;
                    }
                } catch (Exception ipEx) {
                    System.err.println("IP API failed, using default location: " + ipEx.getMessage());
                    lat = 12.9716;
                    lon = 77.5946;
                }
            }
            
            // Get soil data using hybrid fetch system
            SoilData data = soilGridsClient.getSoilData(lat, lon);
            
            // Return with source information
            Map<String, Object> response = new HashMap<>();
            response.put("clay", data.clay);
            response.put("sand", data.sand);
            response.put("silt", data.silt);
            response.put("phh2o", data.phh2o);
            response.put("source", data.source);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            // Log the full error for debugging
            System.err.println("Soil API Error: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Soil service error: " + ex.getMessage());
        }
    }

    /**
     * Manual soil data input endpoint for when APIs fail
     */
    @PostMapping("/manual")
    public ResponseEntity<?> submitManualSoil(@RequestBody ManualSoilRequest request) {
        try {
            System.out.println("Manual soil data submitted: " + request.getSoilType() + ", pH=" + request.getPh());
            
            SoilData data = request.toSoilData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("clay", data.clay);
            response.put("sand", data.sand);
            response.put("silt", data.silt);
            response.put("phh2o", data.phh2o);
            response.put("source", data.source);
            response.put("message", "Manual soil data accepted");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            System.err.println("Manual soil submission error: " + ex.getMessage());
            return ResponseEntity.status(400).body("Invalid manual soil data: " + ex.getMessage());
        }
    }
    
    // Helper class for IP geolocation response
    static class IpApiResponse {
        public Double latitude;
        public Double longitude;
    }
}
