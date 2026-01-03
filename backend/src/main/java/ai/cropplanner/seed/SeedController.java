package ai.cropplanner.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seeds")
@CrossOrigin(origins = "*")
public class SeedController {
    
    private static final Logger logger = LoggerFactory.getLogger(SeedController.class);
    
    private final SeedService seedService;
    private final DealerService dealerService;
    
    public SeedController(SeedService seedService, DealerService dealerService) {
        this.seedService = seedService;
        this.dealerService = dealerService;
    }
    
    /**
     * Get seed recommendations for a crop
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getSeedRecommendations(
            @RequestParam String crop) {
        
        logger.info("Received seed recommendation request for crop: {}", crop);
        
        try {
            List<SeedRecommendation> recommendations = seedService.recommendSeeds(crop);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("crop", crop);
            response.put("recommendations", recommendations);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get seed recommendations", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to get recommendations: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get list of seed dealers with filtering and pagination
     */
    @GetMapping("/dealers")
    public ResponseEntity<Map<String, Object>> getDealers(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String crop,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.info("Fetching dealers - district: {}, crop: {}, city: {}", district, crop, city);
        
        try {
            List<Dealer> dealers = dealerService.findDealers(district, crop, city, page, size);
            long total = dealerService.countDealers(district, crop, city);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("dealers", dealers);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) total / size));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get dealers", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to get dealers: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Seed service is healthy");
    }
}
