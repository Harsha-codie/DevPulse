package ai.cropplanner.market;

import java.time.LocalDateTime;

/**
 * Response DTO for live market price API
 */
public class LivePriceResponse {
    
    private boolean success;
    private String crop;
    private String district;
    private String market;
    private Double modalPrice;
    private Double minPrice;
    private Double maxPrice;
    private String priceUnit = "per quintal";
    private LocalDateTime priceDate;
    private String source;
    private boolean cached;
    private String message;
    
    // Static factory methods
    public static LivePriceResponse success(PriceCache price) {
        LivePriceResponse response = new LivePriceResponse();
        response.success = true;
        response.crop = price.getCrop();
        response.district = price.getDistrict();
        response.market = price.getMarket();
        response.modalPrice = price.getModalPrice();
        response.minPrice = price.getMinPrice();
        response.maxPrice = price.getMaxPrice();
        response.priceUnit = price.getPriceUnit();
        response.priceDate = price.getPriceDate();
        response.source = price.getSource();
        response.cached = true;
        return response;
    }
    
    public static LivePriceResponse error(String message) {
        LivePriceResponse response = new LivePriceResponse();
        response.success = false;
        response.message = message;
        return response;
    }
    
    // Getters and setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
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
    
    public boolean isCached() {
        return cached;
    }
    
    public void setCached(boolean cached) {
        this.cached = cached;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
