package ai.cropplanner.recommendation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.util.List;

/**
 * Request DTO for crop recommendations
 */
public class RecommendationRequest {
    
    // Location inputs - either lat/lon OR district name
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
    
    private String district;
    
    // Fine-grained location inputs (village-level)
    private String village;    // e.g., "Malur"
    private String taluk;      // e.g., "Kolar"
    private String pincode;    // e.g., "563130" - auto-resolves to village/taluk/district
    
    // Language preference for response
    private String language; // "en" for English, "kn" for Kannada
    
    // Optional crop preferences
    private List<String> preferredCrops;
    private String soilType;
    private String season;
    private String farmSize;
    
    // Constructors
    public RecommendationRequest() {}
    
    public RecommendationRequest(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public RecommendationRequest(String district) {
        this.district = district;
    }
    
    // Validation helper
    public boolean hasLocationData() {
        return (latitude != null && longitude != null) || 
               (district != null && !district.trim().isEmpty()) ||
               (pincode != null && !pincode.trim().isEmpty()) ||
               (village != null && !village.trim().isEmpty());
    }
    
    // Getters and Setters
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public List<String> getPreferredCrops() { return preferredCrops; }
    public void setPreferredCrops(List<String> preferredCrops) { this.preferredCrops = preferredCrops; }
    
    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }
    
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    
    public String getFarmSize() { return farmSize; }
    public void setFarmSize(String farmSize) { this.farmSize = farmSize; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    
    public String getTaluk() { return taluk; }
    public void setTaluk(String taluk) { this.taluk = taluk; }
    
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
}