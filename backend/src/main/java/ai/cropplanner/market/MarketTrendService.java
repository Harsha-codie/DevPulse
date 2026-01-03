package ai.cropplanner.market;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarketTrendService {
    
    private final MarketTrendRepository repository;
    
    public MarketTrendService(MarketTrendRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Get all market trends for a specific district
     */
    public List<MarketTrend> getTrendsByDistrict(String districtName) {
        return repository.findByDistrictNameIgnoreCase(districtName);
    }
    
    /**
     * Get market trends for a specific district and crop
     */
    public List<MarketTrend> getTrendsByDistrictAndCrop(String districtName, String cropName) {
        return repository.findByDistrictNameIgnoreCaseAndCmdtyIgnoreCase(districtName, cropName);
    }
    
    /**
     * Get all available districts
     */
    public List<String> getAllDistricts() {
        return repository.findAllDistinctDistricts();
    }
    
    /**
     * Get all crops available in a district
     */
    public List<String> getCropsByDistrict(String districtName) {
        return repository.findDistinctCropsByDistrict(districtName);
    }
    
    /**
     * Get total record count in database
     */
    public long getTotalRecordCount() {
        return repository.count();
    }
    
    /**
     * Get district-level analytics summary
     */
    public DistrictMarketSummary getDistrictSummary(String districtName) {
        List<MarketTrend> trends = getTrendsByDistrict(districtName);
        
        if (trends.isEmpty()) {
            return null;
        }
        
        DistrictMarketSummary summary = new DistrictMarketSummary();
        summary.setDistrictName(districtName);
        summary.setTotalRecords(trends.size());
        
        // Get unique crops
        Set<String> uniqueCrops = trends.stream()
                .map(MarketTrend::getCmdty)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        summary.setAvailableCrops(new ArrayList<>(uniqueCrops));
        
        // Get unique markets
        Set<String> uniqueMarkets = trends.stream()
                .map(MarketTrend::getMarketName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        summary.setAvailableMarkets(new ArrayList<>(uniqueMarkets));
        
        // Calculate price statistics
        List<Double> allMinPrices = trends.stream()
                .map(MarketTrend::getpMin)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        List<Double> allMaxPrices = trends.stream()
                .map(MarketTrend::getpMax)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        List<Double> allModalPrices = trends.stream()
                .map(MarketTrend::getpModal)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (!allMinPrices.isEmpty()) {
            summary.setOverallMinPrice(Collections.min(allMinPrices));
        }
        
        if (!allMaxPrices.isEmpty()) {
            summary.setOverallMaxPrice(Collections.max(allMaxPrices));
        }
        
        if (!allModalPrices.isEmpty()) {
            double avgModal = allModalPrices.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            summary.setAverageModalPrice(avgModal);
        }
        
        return summary;
    }
    
    /**
     * Get crop-specific analytics for a district
     */
    public CropMarketAnalytics getCropAnalytics(String districtName, String cropName) {
        List<MarketTrend> trends = getTrendsByDistrictAndCrop(districtName, cropName);
        
        if (trends.isEmpty()) {
            return null;
        }
        
        CropMarketAnalytics analytics = new CropMarketAnalytics();
        analytics.setDistrictName(districtName);
        analytics.setCropName(cropName);
        analytics.setTotalRecords(trends.size());
        
        // Get varieties
        Set<String> varieties = trends.stream()
                .map(MarketTrend::getVariety)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        analytics.setAvailableVarieties(new ArrayList<>(varieties));
        
        // Calculate price ranges
        List<Double> minPrices = trends.stream()
                .map(MarketTrend::getpMin)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        List<Double> maxPrices = trends.stream()
                .map(MarketTrend::getpMax)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        List<Double> modalPrices = trends.stream()
                .map(MarketTrend::getpModal)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (!minPrices.isEmpty()) {
            analytics.setLowestMinPrice(Collections.min(minPrices));
            analytics.setHighestMinPrice(Collections.max(minPrices));
            analytics.setAverageMinPrice(minPrices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        }
        
        if (!maxPrices.isEmpty()) {
            analytics.setLowestMaxPrice(Collections.min(maxPrices));
            analytics.setHighestMaxPrice(Collections.max(maxPrices));
            analytics.setAverageMaxPrice(maxPrices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        }
        
        if (!modalPrices.isEmpty()) {
            analytics.setLowestModalPrice(Collections.min(modalPrices));
            analytics.setHighestModalPrice(Collections.max(modalPrices));
            analytics.setAverageModalPrice(modalPrices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        }
        
        // Include raw data for detailed analysis
        analytics.setMarketData(trends);
        
        return analytics;
    }
    
    /**
     * DTO for district-level summary
     */
    public static class DistrictMarketSummary {
        private String districtName;
        private int totalRecords;
        private List<String> availableCrops;
        private List<String> availableMarkets;
        private Double overallMinPrice;
        private Double overallMaxPrice;
        private Double averageModalPrice;
        
        // Getters and setters
        public String getDistrictName() { return districtName; }
        public void setDistrictName(String districtName) { this.districtName = districtName; }
        
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        
        public List<String> getAvailableCrops() { return availableCrops; }
        public void setAvailableCrops(List<String> availableCrops) { this.availableCrops = availableCrops; }
        
        public List<String> getAvailableMarkets() { return availableMarkets; }
        public void setAvailableMarkets(List<String> availableMarkets) { this.availableMarkets = availableMarkets; }
        
        public Double getOverallMinPrice() { return overallMinPrice; }
        public void setOverallMinPrice(Double overallMinPrice) { this.overallMinPrice = overallMinPrice; }
        
        public Double getOverallMaxPrice() { return overallMaxPrice; }
        public void setOverallMaxPrice(Double overallMaxPrice) { this.overallMaxPrice = overallMaxPrice; }
        
        public Double getAverageModalPrice() { return averageModalPrice; }
        public void setAverageModalPrice(Double averageModalPrice) { this.averageModalPrice = averageModalPrice; }
    }
    
    /**
     * DTO for crop-specific analytics
     */
    public static class CropMarketAnalytics {
        private String districtName;
        private String cropName;
        private int totalRecords;
        private List<String> availableVarieties;
        private Double lowestMinPrice;
        private Double highestMinPrice;
        private Double averageMinPrice;
        private Double lowestMaxPrice;
        private Double highestMaxPrice;
        private Double averageMaxPrice;
        private Double lowestModalPrice;
        private Double highestModalPrice;
        private Double averageModalPrice;
        private List<MarketTrend> marketData;
        
        // Getters and setters
        public String getDistrictName() { return districtName; }
        public void setDistrictName(String districtName) { this.districtName = districtName; }
        
        public String getCropName() { return cropName; }
        public void setCropName(String cropName) { this.cropName = cropName; }
        
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        
        public List<String> getAvailableVarieties() { return availableVarieties; }
        public void setAvailableVarieties(List<String> availableVarieties) { this.availableVarieties = availableVarieties; }
        
        public Double getLowestMinPrice() { return lowestMinPrice; }
        public void setLowestMinPrice(Double lowestMinPrice) { this.lowestMinPrice = lowestMinPrice; }
        
        public Double getHighestMinPrice() { return highestMinPrice; }
        public void setHighestMinPrice(Double highestMinPrice) { this.highestMinPrice = highestMinPrice; }
        
        public Double getAverageMinPrice() { return averageMinPrice; }
        public void setAverageMinPrice(Double averageMinPrice) { this.averageMinPrice = averageMinPrice; }
        
        public Double getLowestMaxPrice() { return lowestMaxPrice; }
        public void setLowestMaxPrice(Double lowestMaxPrice) { this.lowestMaxPrice = lowestMaxPrice; }
        
        public Double getHighestMaxPrice() { return highestMaxPrice; }
        public void setHighestMaxPrice(Double highestMaxPrice) { this.highestMaxPrice = highestMaxPrice; }
        
        public Double getAverageMaxPrice() { return averageMaxPrice; }
        public void setAverageMaxPrice(Double averageMaxPrice) { this.averageMaxPrice = averageMaxPrice; }
        
        public Double getLowestModalPrice() { return lowestModalPrice; }
        public void setLowestModalPrice(Double lowestModalPrice) { this.lowestModalPrice = lowestModalPrice; }
        
        public Double getHighestModalPrice() { return highestModalPrice; }
        public void setHighestModalPrice(Double highestModalPrice) { this.highestModalPrice = highestModalPrice; }
        
        public Double getAverageModalPrice() { return averageModalPrice; }
        public void setAverageModalPrice(Double averageModalPrice) { this.averageModalPrice = averageModalPrice; }
        
        public List<MarketTrend> getMarketData() { return marketData; }
        public void setMarketData(List<MarketTrend> marketData) { this.marketData = marketData; }
    }
}
