package ai.cropplanner.recommendation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Individual crop recommendation DTO
 */
public class CropRecommendation {
    
    @NotBlank(message = "Crop name is required")
    private String crop;
    
    @Min(value = 0, message = "Score must be between 0 and 100")
    @Max(value = 100, message = "Score must be between 0 and 100")
    private Integer score;
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    // Constructors
    public CropRecommendation() {}
    
    public CropRecommendation(String crop, Integer score, String reason) {
        this.crop = crop;
        this.score = score;
        this.reason = reason;
    }
    
    // Getters and Setters
    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }
    
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    @Override
    public String toString() {
        return String.format("CropRecommendation{crop='%s', score=%d, reason='%s'}", 
            crop, score, reason);
    }
}