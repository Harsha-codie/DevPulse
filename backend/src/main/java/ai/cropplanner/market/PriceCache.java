package ai.cropplanner.market;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Cache for market prices from AGMARKNET API
 * Stores prices for 7 days to reduce API calls
 */
@Entity
@Table(name = "price_cache")
public class PriceCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String crop;
    
    @Column(nullable = false, length = 100)
    private String district;
    
    @Column(nullable = false, length = 100)
    private String market;
    
    @Column(name = "modal_price")
    private Double modalPrice;  // Most common trading price
    
    @Column(name = "min_price")
    private Double minPrice;
    
    @Column(name = "max_price")
    private Double maxPrice;
    
    @Column(name = "price_unit", length = 20)
    private String priceUnit = "per quintal";  // Default unit
    
    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;
    
    @Column(name = "price_date")
    private LocalDateTime priceDate;  // Date when price was recorded at market
    
    @Column(length = 50)
    private String source = "AGMARKNET";
    
    // Constructors
    public PriceCache() {
        this.fetchedAt = LocalDateTime.now();
    }
    
    public PriceCache(String crop, String district, String market, Double modalPrice) {
        this();
        this.crop = crop;
        this.district = district;
        this.market = market;
        this.modalPrice = modalPrice;
    }
    
    // Check if cache is expired (older than 7 days)
    public boolean isExpired() {
        return fetchedAt.isBefore(LocalDateTime.now().minusDays(7));
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCrop() {
        return crop;
    }
    
    public void setCrop(String crop) {
        this.crop = crop;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public void setDistrict(String district) {
        this.district = district;
    }
    
    public String getMarket() {
        return market;
    }
    
    public void setMarket(String market) {
        this.market = market;
    }
    
    public Double getModalPrice() {
        return modalPrice;
    }
    
    public void setModalPrice(Double modalPrice) {
        this.modalPrice = modalPrice;
    }
    
    public Double getMinPrice() {
        return minPrice;
    }
    
    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }
    
    public Double getMaxPrice() {
        return maxPrice;
    }
    
    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    public String getPriceUnit() {
        return priceUnit;
    }
    
    public void setPriceUnit(String priceUnit) {
        this.priceUnit = priceUnit;
    }
    
    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }
    
    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
    
    public LocalDateTime getPriceDate() {
        return priceDate;
    }
    
    public void setPriceDate(LocalDateTime priceDate) {
        this.priceDate = priceDate;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
}
