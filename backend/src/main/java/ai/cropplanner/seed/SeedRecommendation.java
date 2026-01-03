package ai.cropplanner.seed;

public class SeedRecommendation {
    private String seedType;
    private String variety;
    private String duration; // days to maturity
    private String yieldPotential;
    private String features;
    private String suitableFor; // soil/climate
    
    public SeedRecommendation() {}
    
    public SeedRecommendation(String seedType, String variety, String duration, 
                             String yieldPotential, String features, String suitableFor) {
        this.seedType = seedType;
        this.variety = variety;
        this.duration = duration;
        this.yieldPotential = yieldPotential;
        this.features = features;
        this.suitableFor = suitableFor;
    }
    
    // Getters and setters
    public String getSeedType() { return seedType; }
    public void setSeedType(String seedType) { this.seedType = seedType; }
    
    public String getVariety() { return variety; }
    public void setVariety(String variety) { this.variety = variety; }
    
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    
    public String getYieldPotential() { return yieldPotential; }
    public void setYieldPotential(String yieldPotential) { this.yieldPotential = yieldPotential; }
    
    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }
    
    public String getSuitableFor() { return suitableFor; }
    public void setSuitableFor(String suitableFor) { this.suitableFor = suitableFor; }
}
