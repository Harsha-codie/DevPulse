package ai.cropplanner.crop;

import jakarta.persistence.*;

/**
 * District-level crop suitability constraints
 * Based on Karnataka's agro-climatic zones
 */
@Entity
@Table(name = "district_crop_suitability")
public class DistrictCropSuitability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String district; // e.g., "UTTARA KANNADA"
    
    @Column(name = "agro_climatic_zone")
    private String agroClimaticZone; // e.g., "COASTAL"
    
    @Column(name = "suitable_crops", length = 1000)
    private String suitableCrops; // Comma-separated: "Rice,Coconut,Areca,Cashew,Mango"
    
    @Column(name = "avoid_crops", length = 1000)
    private String avoidCrops; // Comma-separated: "Cabbage,Wheat,Potato,Barley"
    
    @Column(name = "season")
    private String season; // Monsoon/Summer/Winter/ALL
    
    @Column(length = 500)
    private String remarks; // Additional notes
    
    // Constructors
    public DistrictCropSuitability() {}
    
    public DistrictCropSuitability(String district, String agroClimaticZone, 
                                   String suitableCrops, String avoidCrops, 
                                   String season, String remarks) {
        this.district = district;
        this.agroClimaticZone = agroClimaticZone;
        this.suitableCrops = suitableCrops;
        this.avoidCrops = avoidCrops;
        this.season = season;
        this.remarks = remarks;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public String getAgroClimaticZone() { return agroClimaticZone; }
    public void setAgroClimaticZone(String agroClimaticZone) { this.agroClimaticZone = agroClimaticZone; }
    
    public String getSuitableCrops() { return suitableCrops; }
    public void setSuitableCrops(String suitableCrops) { this.suitableCrops = suitableCrops; }
    
    public String getAvoidCrops() { return avoidCrops; }
    public void setAvoidCrops(String avoidCrops) { this.avoidCrops = avoidCrops; }
    
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
