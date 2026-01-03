package ai.cropplanner.recommendation;

import java.util.List;

/**
 * Response DTO for crop recommendations
 */
public class RecommendationResponse {
    
    private List<CropRecommendation> recommendations;
    private String district;
    private String message;
    private boolean success;
    
    // Constructors
    public RecommendationResponse() {}
    
    public RecommendationResponse(List<CropRecommendation> recommendations, String district) {
        this.recommendations = recommendations;
        this.district = district;
        this.success = true;
        this.message = "Recommendations generated successfully";
    }
    
    public static RecommendationResponse error(String message) {
        RecommendationResponse response = new RecommendationResponse();
        response.success = false;
        response.message = message;
        return response;
    }
    
    // Getters and Setters
    public List<CropRecommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<CropRecommendation> recommendations) { 
        this.recommendations = recommendations; 
    }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}