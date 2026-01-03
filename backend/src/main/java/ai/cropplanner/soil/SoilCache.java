package ai.cropplanner.soil;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "soil_cache")
public class SoilCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;
    
    @Column
    private Double clay;
    
    @Column
    private Double sand;
    
    @Column
    private Double silt;
    
    @Column
    private Double ph;
    
    @Column(length = 50)
    private String source;
    
    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;
    
    // Constructors
    public SoilCache() {
        this.fetchedAt = LocalDateTime.now();
    }
    
    public SoilCache(Double latitude, Double longitude, Double clay, Double sand, 
                     Double silt, Double ph, String source) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.clay = clay;
        this.sand = sand;
        this.silt = silt;
        this.ph = ph;
        this.source = source;
        this.fetchedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Double getClay() { return clay; }
    public void setClay(Double clay) { this.clay = clay; }
    
    public Double getSand() { return sand; }
    public void setSand(Double sand) { this.sand = sand; }
    
    public Double getSilt() { return silt; }
    public void setSilt(Double silt) { this.silt = silt; }
    
    public Double getPh() { return ph; }
    public void setPh(Double ph) { this.ph = ph; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
    
    // Convert to SoilData
    public SoilData toSoilData() {
        SoilData data = new SoilData();
        data.clay = this.clay;
        data.sand = this.sand;
        data.silt = this.silt;
        data.phh2o = this.ph;
        data.source = this.source + " (cached)";
        return data;
    }
}
