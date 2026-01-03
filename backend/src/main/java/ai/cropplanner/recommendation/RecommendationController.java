package ai.cropplanner.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
@Validated
public class RecommendationController {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    
    private final RecommendationService recommendationService;
    
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }
    
    /**
     * Generate crop recommendations based on location and preferences
     * 
     * Accepts either:
     * - latitude/longitude coordinates
     * - district name
     * Plus optional crop preferences, soil type, season, farm size
     */
    @PostMapping
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @Valid @RequestBody RecommendationRequest request) {
        
        logger.info("Received recommendation request: lat={}, lon={}, district={}", 
            request.getLatitude(), request.getLongitude(), request.getDistrict());
        
        // Validate that we have either coordinates or district
        if (!request.hasLocationData()) {
            RecommendationResponse errorResponse = RecommendationResponse.error(
                "Either latitude/longitude or district name must be provided"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            RecommendationResponse response = recommendationService.generateRecommendations(request);
            
            if (response.isSuccess()) {
                logger.info("Successfully generated {} recommendations for district: {}", 
                    response.getRecommendations().size(), response.getDistrict());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Failed to generate recommendations: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error generating recommendations", e);
            RecommendationResponse errorResponse = RecommendationResponse.error(
                "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint for recommendations service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Recommendations service is healthy");
    }
}