package ai.cropplanner.fertilizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fertilizer")
@CrossOrigin(origins = "*")
@Validated
public class FertilizerController {
    
    private static final Logger logger = LoggerFactory.getLogger(FertilizerController.class);
    
    private final FertilizerService fertilizerService;
    
    public FertilizerController(FertilizerService fertilizerService) {
        this.fertilizerService = fertilizerService;
    }
    
    /**
     * Get fertilizer recommendations based on crop, soil, and environmental conditions
     */
    @PostMapping("/recommendations")
    public ResponseEntity<FertilizerResponse> getRecommendations(
            @Valid @RequestBody FertilizerRequest request) {
        
        logger.info("Received fertilizer recommendation request for crop: {}, district: {}", 
            request.getCrop(), request.getDistrict());
        
        try {
            FertilizerResponse response = fertilizerService.generateRecommendations(request);
            
            if (response.isSuccess()) {
                logger.info("Successfully generated {} fertilizer recommendations", 
                    response.getRecommendations().size());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Failed to generate recommendations: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error generating fertilizer recommendations", e);
            FertilizerResponse errorResponse = FertilizerResponse.error(
                "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Fertilizer service is healthy");
    }
}
