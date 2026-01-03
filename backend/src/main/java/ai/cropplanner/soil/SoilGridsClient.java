package ai.cropplanner.soil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import java.util.Random;
import java.util.Optional;

@Component
public class SoilGridsClient {
    private final RestTemplate restTemplate;
    private static final String SOILGRIDS_URL = "https://rest.isric.org/soilgrids/v2.0/properties/query";
    private final Random random = new Random();
    
    @Autowired
    private SoilCacheRepository soilCacheRepository;
    
    @Autowired
    private OpenLandMapClient openLandMapClient;
    
    @Value("${soil.cache.enabled:true}")
    private boolean cacheEnabled;

    public SoilGridsClient() {
        this.restTemplate = new RestTemplate();
    }

    public SoilData getSoilData(double lat, double lon) {
        System.out.println("=== HYBRID SOIL DATA FETCH STARTED ===");
        System.out.println("Location: lat=" + lat + ", lon=" + lon);
        
        // Step 1: Check cache first (if enabled)
        if (cacheEnabled) {
            try {
                Optional<SoilCache> cachedData = soilCacheRepository.findNearbyCache(lat, lon);
                if (cachedData.isPresent()) {
                    System.out.println("✓ Cache HIT - using cached data from " + cachedData.get().getSource());
                    return cachedData.get().toSoilData();
                } else {
                    System.out.println("✗ Cache MISS - proceeding to API fetch");
                }
            } catch (Exception e) {
                System.err.println("Cache check failed: " + e.getMessage());
            }
        } else {
            System.out.println("Cache disabled by configuration");
        }
        
        // Step 2: Try SoilGrids API with retries
        SoilData soilData = null;
        try {
            System.out.println("Attempting SoilGrids API (primary source)...");
            soilData = fetchFromSoilGridsWithRetry(lat, lon);
            soilData.source = "SoilGrids";
            System.out.println("✓ SoilGrids SUCCESS");
        } catch (Exception ex) {
            System.err.println("✗ SoilGrids FAILED: " + ex.getMessage());
            
            // Step 3: Try OpenLandMap fallback
            try {
                System.out.println("Attempting OpenLandMap API (fallback)...");
                soilData = openLandMapClient.getSoilData(lat, lon);
                System.out.println("✓ OpenLandMap SUCCESS");
            } catch (Exception ex2) {
                System.err.println("✗ OpenLandMap FAILED: " + ex2.getMessage());
                
                // Step 4: Use regional fallback as last resort
                System.out.println("Using regional fallback data (last resort)...");
                soilData = generateFallbackData(lat, lon);
                soilData.source = "Fallback";
                System.out.println("✓ Fallback data generated");
            }
        }
        
        // Step 5: Cache the result (if not from cache and cache is enabled)
        if (cacheEnabled && soilData != null && !"Fallback".equals(soilData.source)) {
            try {
                SoilCache cacheEntry = new SoilCache(lat, lon, soilData.clay, soilData.sand, 
                                                     soilData.silt, soilData.phh2o, soilData.source);
                soilCacheRepository.save(cacheEntry);
                System.out.println("✓ Data cached successfully");
            } catch (Exception e) {
                System.err.println("Failed to cache data: " + e.getMessage());
            }
        }
        
        System.out.println("=== FETCH COMPLETE - Source: " + soilData.source + " ===");
        return soilData;
    }
    
    /**
     * Fetch from SoilGrids with retry logic (3 attempts with shorter delays for faster response)
     */
    private SoilData fetchFromSoilGridsWithRetry(double lat, double lon) throws Exception {
        int maxRetries = 2; // Reduced from 3 to 2 for faster fallback
        long[] delays = {500, 1000}; // Shorter delays: 0.5s, 1s (instead of 1s, 2s, 3s)
        
        Exception lastException = null;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    System.out.println("Retry attempt " + (attempt + 1) + "/" + maxRetries + 
                                     " after " + delays[attempt - 1] + "ms delay");
                    Thread.sleep(delays[attempt - 1]);
                }
                
                SoilData data = fetchFromSoilGrids(lat, lon);
                
                // Validate data
                if (Double.isNaN(data.clay) || Double.isNaN(data.sand) || 
                    Double.isNaN(data.silt) || Double.isNaN(data.phh2o)) {
                    throw new Exception("SoilGrids returned invalid (NaN) values");
                }
                
                return data;
                
            } catch (Exception e) {
                lastException = e;
                System.err.println("Attempt " + (attempt + 1) + " failed: " + e.getMessage());
            }
        }
        
        throw new Exception("SoilGrids failed after " + maxRetries + " attempts: " + 
                          (lastException != null ? lastException.getMessage() : "Unknown error"));
    }

    private SoilData fetchFromSoilGrids(double lat, double lon) throws Exception {
        String url = UriComponentsBuilder.fromHttpUrl(SOILGRIDS_URL)
                .queryParam("lon", lon)
                .queryParam("lat", lat)
                .queryParam("property", "clay")
                .queryParam("property", "sand")
                .queryParam("property", "silt")
                .queryParam("property", "phh2o")
                .queryParam("depth", "0-5cm")
                .queryParam("value", "mean")
                .toUriString();

        // Add headers to avoid blocking
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "CropPlanner/1.0");
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, 
            HttpMethod.GET, 
            entity, 
            String.class
        );
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return SoilData.fromJson(response.getBody());
        } else {
            throw new Exception("SoilGrids returned: " + response.getStatusCode());
        }
    }

    private SoilData generateFallbackData(double lat, double lon) {
        System.out.println("Generating fallback soil data for lat=" + lat + ", lon=" + lon);
        
        // Generate realistic soil data based on Karnataka regions
        SoilData data = new SoilData();
        
        // Karnataka typical soil ranges:
        // Coastal: More clay (35-45%), pH 5.5-6.5
        // Malnad: Lateritic, clay (30-40%), pH 5.0-6.0
        // Maidan: Red loamy, clay (25-35%), pH 6.0-7.0
        
        // Determine region based on coordinates
        boolean isCoastal = lon < 75.5; // Western coast
        boolean isMalnad = lon >= 75.5 && lon < 76.5; // Hill region
        
        if (isCoastal) {
            data.clay = 38.0 + (random.nextDouble() * 7 - 3.5);
            data.sand = 30.0 + (random.nextDouble() * 10 - 5);
            data.phh2o = 5.8 + (random.nextDouble() * 0.8);
        } else if (isMalnad) {
            data.clay = 35.0 + (random.nextDouble() * 10 - 5);
            data.sand = 32.0 + (random.nextDouble() * 8 - 4);
            data.phh2o = 5.5 + (random.nextDouble() * 1.0);
        } else {
            // Maidan region (most of Karnataka)
            data.clay = 28.0 + (random.nextDouble() * 10 - 5);
            data.sand = 40.0 + (random.nextDouble() * 12 - 6);
            data.phh2o = 6.3 + (random.nextDouble() * 1.0);
        }
        
        data.silt = 100.0 - data.clay - data.sand;
        
        // Ensure valid ranges
        data.clay = Math.max(20, Math.min(50, data.clay));
        data.sand = Math.max(20, Math.min(60, data.sand));
        data.silt = Math.max(10, Math.min(40, data.silt));
        data.phh2o = Math.max(5.0, Math.min(8.0, data.phh2o));
        
        // Double-check no NaN values
        if (Double.isNaN(data.clay)) data.clay = 30.0;
        if (Double.isNaN(data.sand)) data.sand = 40.0;
        if (Double.isNaN(data.silt)) data.silt = 30.0;
        if (Double.isNaN(data.phh2o)) data.phh2o = 6.5;
        
        data.source = "Fallback";
        
        System.out.println("Generated fallback: clay=" + data.clay + ", sand=" + data.sand + 
                          ", silt=" + data.silt + ", pH=" + data.phh2o);
        
        return data;
    }
}
