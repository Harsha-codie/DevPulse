package ai.cropplanner.soil;

/**
 * Request body for manual soil data input
 */
public class ManualSoilRequest {
    private String soilType; // Sandy, Clayey, Loamy
    private Double ph;
    private String moistureLevel; // Optional: Dry, Moderate, Wet
    
    // Getters and Setters
    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }
    
    public Double getPh() { return ph; }
    public void setPh(Double ph) { this.ph = ph; }
    
    public String getMoistureLevel() { return moistureLevel; }
    public void setMoistureLevel(String moistureLevel) { this.moistureLevel = moistureLevel; }
    
    /**
     * Convert manual input to SoilData with typical composition values
     */
    public SoilData toSoilData() {
        SoilData data = new SoilData();
        data.phh2o = (ph != null) ? ph : 6.5;
        data.source = "Manual";
        
        // Set typical values based on soil type
        switch (soilType != null ? soilType.toLowerCase() : "loamy") {
            case "sandy":
                data.sand = 80.0;
                data.clay = 10.0;
                data.silt = 10.0;
                break;
            case "clayey":
                data.clay = 60.0;
                data.sand = 20.0;
                data.silt = 20.0;
                break;
            case "loamy":
            default:
                data.sand = 40.0;
                data.clay = 30.0;
                data.silt = 30.0;
                break;
        }
        
        return data;
    }
}
