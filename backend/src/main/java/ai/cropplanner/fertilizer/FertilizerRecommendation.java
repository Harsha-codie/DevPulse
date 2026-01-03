package ai.cropplanner.fertilizer;

public class FertilizerRecommendation {
    private String type;
    private String quantity;
    private String timing;
    private String schedule;
    private String npkRatio;
    private String justification;
    
    public FertilizerRecommendation() {}
    
    public FertilizerRecommendation(String type, String quantity, String timing, String schedule, 
                                   String npkRatio, String justification) {
        this.type = type;
        this.quantity = quantity;
        this.timing = timing;
        this.schedule = schedule;
        this.npkRatio = npkRatio;
        this.justification = justification;
    }
    
    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    
    public String getTiming() { return timing; }
    public void setTiming(String timing) { this.timing = timing; }
    
    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
    
    public String getNpkRatio() { return npkRatio; }
    public void setNpkRatio(String npkRatio) { this.npkRatio = npkRatio; }
    
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
}
