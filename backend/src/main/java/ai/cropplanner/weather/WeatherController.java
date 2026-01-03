package ai.cropplanner.weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
public class WeatherController {
    @Autowired
    private WeatherClient weatherClient;

    @Value("${ipapi.base-url:https://ipapi.co}")
    private String ipApiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public ResponseEntity<?> getWeather(@RequestParam(required = false) Double lat,
                                        @RequestParam(required = false) Double lon) {
        try {
            if (lat == null || lon == null) {
                // Fallback to IP-based geolocation
                String ipApiUrl = ipApiBaseUrl + "/json";
                ResponseEntity<IpApiResponse> ipResp = restTemplate.getForEntity(ipApiUrl, IpApiResponse.class);
                if (ipResp.getStatusCode().is2xxSuccessful() && ipResp.getBody() != null) {
                    lat = ipResp.getBody().latitude;
                    lon = ipResp.getBody().longitude;
                } else {
                    return ResponseEntity.badRequest().body("Could not determine location");
                }
            }
            String weatherJson = weatherClient.getWeather(lat, lon);
            return ResponseEntity.ok(weatherJson);
        } catch (Exception ex) {
            return ResponseEntity.status(502).body("Weather service error: " + ex.getMessage());
        }
    }

    // Helper class for IP geolocation response
    static class IpApiResponse {
        public Double latitude;
        public Double longitude;
        // For ipapi.co, the fields are 'latitude' and 'longitude'
    }
}
