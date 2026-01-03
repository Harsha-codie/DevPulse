package ai.cropplanner.market;

import jakarta.persistence.*;

@Entity
@Table(name = "market_trends", 
       indexes = {
           @Index(name = "idx_unique_record", 
                  columnList = "cmdty, district_name, market_name, variety")
       })
public class MarketTrend {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cmdty")
    private String cmdty;
    
    @Column(name = "market_name")
    private String marketName;
    
    @Column(name = "district_name")
    private String districtName;
    
    @Column(name = "variety")
    private String variety;
    
    @Column(name = "p_min")
    private Double pMin;
    
    @Column(name = "p_max")
    private Double pMax;
    
    @Column(name = "p_modal")
    private Double pModal;
    
    public MarketTrend() {
    }
    
    public MarketTrend(String cmdty, String marketName, String districtName, String variety, 
                       Double pMin, Double pMax, Double pModal) {
        this.cmdty = cmdty;
        this.marketName = marketName;
        this.districtName = districtName;
        this.variety = variety;
        this.pMin = pMin;
        this.pMax = pMax;
        this.pModal = pModal;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCmdty() {
        return cmdty;
    }
    
    public void setCmdty(String cmdty) {
        this.cmdty = cmdty;
    }
    
    public String getMarketName() {
        return marketName;
    }
    
    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }
    
    public String getDistrictName() {
        return districtName;
    }
    
    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }
    
    public String getVariety() {
        return variety;
    }
    
    public void setVariety(String variety) {
        this.variety = variety;
    }
    
    public Double getpMin() {
        return pMin;
    }
    
    public void setpMin(Double pMin) {
        this.pMin = pMin;
    }
    
    public Double getpMax() {
        return pMax;
    }
    
    public void setpMax(Double pMax) {
        this.pMax = pMax;
    }
    
    public Double getpModal() {
        return pModal;
    }
    
    public void setpModal(Double pModal) {
        this.pModal = pModal;
    }
}
