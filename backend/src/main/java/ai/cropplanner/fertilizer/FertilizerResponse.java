package ai.cropplanner.fertilizer;

import java.util.List;

public class FertilizerResponse {
    private boolean success;
    private String message;
    private String crop;
    private String district;
    private List<FertilizerRecommendation> recommendations;
    
    public FertilizerResponse() {}
    
    public FertilizerResponse(List<FertilizerRecommendation> recommendations, String crop, String district) {
        this.success = true;
        this.recommendations = recommendations;
        this.crop = crop;
        this.district = district;
    }
    
    public static FertilizerResponse error(String message) {
        FertilizerResponse response = new FertilizerResponse();
        response.success = false;
        response.message = message;
        return response;
    }
    
    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public List<FertilizerRecommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<FertilizerRecommendation> recommendations) { 
        this.recommendations = recommendations; 
    }
}
