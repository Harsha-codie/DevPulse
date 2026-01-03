package ai.cropplanner.fertilizer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FertilizerRequest {
    @NotBlank(message = "Crop name is required")
    private String crop;
    
    @NotBlank(message = "District is required")
    private String district;
    
    private String season;
    
    // Soil NPK levels (0-100 scale)
    private Double nitrogen;
    private Double phosphorus;
    private Double potassium;
    
    // Soil pH
    private Double soilPh;
    
    // Rainfall data (mm)
    private Double annualRainfall;
    
    // Farm size
    private String farmSize;
    
    public FertilizerRequest() {}
    
    // Getters and setters
    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    
    public Double getNitrogen() { return nitrogen; }
    public void setNitrogen(Double nitrogen) { this.nitrogen = nitrogen; }
    
    public Double getPhosphorus() { return phosphorus; }
    public void setPhosphorus(Double phosphorus) { this.phosphorus = phosphorus; }
    
    public Double getPotassium() { return potassium; }
    public void setPotassium(Double potassium) { this.potassium = potassium; }
    
    public Double getSoilPh() { return soilPh; }
    public void setSoilPh(Double soilPh) { this.soilPh = soilPh; }
    
    public Double getAnnualRainfall() { return annualRainfall; }
    public void setAnnualRainfall(Double annualRainfall) { this.annualRainfall = annualRainfall; }
    
    public String getFarmSize() { return farmSize; }
    public void setFarmSize(String farmSize) { this.farmSize = farmSize; }
}
