package ai.cropplanner.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Client for fetching live market prices from AGMARKNET API
 * Falls back to CSV data if API fails
 * 
 * Top 12 Karnataka crops supported for live pricing
 */
@Service
public class AgmarknetClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AgmarknetClient.class);
    
    private final RestTemplate restTemplate;
    private final PriceCacheRepository priceCacheRepository;
    private final MarketTrendService marketTrendService;
    
    // Top 12 crops in Karnataka (for live pricing)
    private static final Set<String> SUPPORTED_CROPS = Set.of(
        "RICE", "PADDY",
        "MAIZE", "CORN",
        "COTTON",
        "SUGARCANE",
        "GROUNDNUT", "PEANUT",
        "SOYBEAN",
        "WHEAT",
        "TOMATO",
        "ONION",
        "CHILI", "PEPPER",
        "CHICKPEA", "GRAM", "BENGAL GRAM",
        "MUSTARD"
    );
    
    // data.gov.in API configuration
    // Using the RECORDS endpoint for actual data, not the resource metadata endpoint
    private static final String DATA_GOV_API_BASE = "https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070";
    private static final String API_KEY = "579b464db66ec23bdd0000014fdf0ed1cda94c5f734d65319f6e1891";
    private static final String STATE_FILTER = "Karnataka";
    
    // Map our crop names to data.gov.in commodity names
    private static final Map<String, String> CROP_TO_COMMODITY_MAP = Map.ofEntries(
        Map.entry("RICE", "Paddy"),
        Map.entry("PADDY", "Paddy"),
        Map.entry("WHEAT", "Wheat"),
        Map.entry("MAIZE", "Maize"),
        Map.entry("CORN", "Maize"),
        Map.entry("CHICKPEA", "Gram"),
        Map.entry("GRAM", "Gram"),
        Map.entry("BENGAL GRAM", "Gram"),
        Map.entry("GROUNDNUT", "Groundnut"),
        Map.entry("PEANUT", "Groundnut"),
        Map.entry("SOYBEAN", "Soyabean"),
        Map.entry("COTTON", "Cotton"),
        Map.entry("SUGARCANE", "Sugarcane"),
        Map.entry("TOMATO", "Tomato"),
        Map.entry("ONION", "Onion"),
        Map.entry("CHILI", "Chili"),
        Map.entry("PEPPER", "Pepper"),
        Map.entry("MUSTARD", "Mustard")
    );
    
    public AgmarknetClient(PriceCacheRepository priceCacheRepository, 
                          MarketTrendService marketTrendService) {
        this.restTemplate = new RestTemplate();
        this.priceCacheRepository = priceCacheRepository;
        this.marketTrendService = marketTrendService;
    }
    
    /**
     * Get live price for a crop in a district (with cache and CSV fallback)
     */
    public LivePriceResponse getLivePrice(String crop, String district) {
        String normalizedCrop = crop.toUpperCase().trim();
        String normalizedDistrict = district.toUpperCase().trim();
        
        logger.info("Fetching live price for crop: {}, district: {}", normalizedCrop, normalizedDistrict);
        
        try {
            // Step 1: Check if crop is supported for live pricing
            if (!isCropSupported(normalizedCrop)) {
                logger.debug("Crop '{}' not in top 12, using CSV fallback", normalizedCrop);
                return getFallbackPrice(normalizedCrop, normalizedDistrict);
            }
            
            // Step 2: Check cache (7 days)
            LocalDateTime expiryDate = LocalDateTime.now().minusDays(7);
            Optional<PriceCache> cached = priceCacheRepository.findValidPrice(
                normalizedCrop, normalizedDistrict, expiryDate
            );
            
            if (cached.isPresent()) {
                logger.info("Cache HIT for {}/{} (age: {} days)", 
                    normalizedCrop, normalizedDistrict,
                    java.time.Duration.between(cached.get().getFetchedAt(), LocalDateTime.now()).toDays()
                );
                return LivePriceResponse.success(cached.get());
            }
            
            // Step 3: Try AGMARKNET API
            logger.info("Cache MISS - attempting AGMARKNET API fetch");
            try {
                PriceCache freshPrice = fetchFromAgmarknet(normalizedCrop, normalizedDistrict);
                if (freshPrice != null) {
                    // Save to cache
                    priceCacheRepository.save(freshPrice);
                    logger.info("Successfully fetched from AGMARKNET and cached");
                    return LivePriceResponse.success(freshPrice);
                }
            } catch (Exception apiError) {
                logger.warn("AGMARKNET API failed: {}", apiError.getMessage());
            }
            
            // Step 4: Fallback to CSV data
            logger.info("Using CSV fallback for {}/{}", normalizedCrop, normalizedDistrict);
            return getFallbackPrice(normalizedCrop, normalizedDistrict);
            
        } catch (Exception e) {
            logger.error("Error fetching live price: {}", e.getMessage(), e);
            return LivePriceResponse.error("Failed to fetch price: " + e.getMessage());
        }
    }
    
    /**
     * Fetch price from data.gov.in API for Karnataka
     */
    private PriceCache fetchFromAgmarknet(String crop, String district) {
        try {
            // Map our crop name to data.gov.in commodity name
            String commodityName = CROP_TO_COMMODITY_MAP.getOrDefault(crop, crop);
            
            // Build API URL with filters for Karnataka and crop
            // Remove state filter to get more results, then filter in code
            String url = String.format(
                "%s?api-key=%s&format=json&filters[commodity]=%s&limit=50",
                DATA_GOV_API_BASE,
                API_KEY,
                commodityName
            );
            
            logger.info("Fetching from data.gov.in: {} -> commodity: {}", crop, commodityName);
            logger.debug("API URL: {}", url.replace(API_KEY, "***"));
            
            // Make API call
            String response = restTemplate.getForObject(url, String.class);
            
            if (response == null || response.isEmpty()) {
                logger.warn("Empty response from data.gov.in API");
                return null;
            }
            
            // Parse JSON response
            return parseDataGovResponse(response, crop, district);
            
        } catch (Exception e) {
            logger.error("Failed to fetch from data.gov.in: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse data.gov.in JSON response
     */
    private PriceCache parseDataGovResponse(String jsonResponse, String crop, String district) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            
            JsonNode records = root.path("records");
            
            if (!records.isArray() || records.size() == 0) {
                logger.warn("No price records found for crop: {}", crop);
                return null;
            }
            
            // Filter for Karnataka records and calculate average
            double totalModalPrice = 0;
            double totalMinPrice = 0;
            double totalMaxPrice = 0;
            int count = 0;
            String marketName = "Karnataka Average";
            
            for (JsonNode record : records) {
                // Filter for Karnataka state
                String state = record.path("state").asText("");
                if (!state.equalsIgnoreCase("Karnataka")) {
                    continue;
                }
                
                // Parse prices (they come as strings in the API)
                String modalPriceStr = record.path("modal_price").asText("0");
                String minPriceStr = record.path("min_price").asText("0");
                String maxPriceStr = record.path("max_price").asText("0");
                
                try {
                    double modal = Double.parseDouble(modalPriceStr);
                    double min = Double.parseDouble(minPriceStr);
                    double max = Double.parseDouble(maxPriceStr);
                    
                    if (modal > 0) {
                        totalModalPrice += modal;
                        totalMinPrice += min;
                        totalMaxPrice += max;
                        count++;
                        
                        // Get market name from first Karnataka record
                        if (count == 1) {
                            String marketStr = record.path("market").asText("");
                            String districtStr = record.path("district").asText("");
                            marketName = districtStr.isEmpty() ? marketStr : districtStr + " - " + marketStr;
                            logger.debug("Using market: {} for {}", marketName, crop);
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.debug("Skipping record with invalid price format");
                }
            }
            
            if (count == 0) {
                logger.warn("No valid Karnataka price data found for crop: {}", crop);
                return null;
            }
            
            // Create price cache with averages
            PriceCache priceCache = new PriceCache(
                crop,
                district,
                marketName,
                totalModalPrice / count
            );
            priceCache.setMinPrice(totalMinPrice / count);
            priceCache.setMaxPrice(totalMaxPrice / count);
            priceCache.setSource("DATA_GOV_IN");
            priceCache.setPriceDate(LocalDateTime.now());
            
            logger.info("Successfully parsed price for {}: ₹{}/quintal (from {} Karnataka records)", 
                crop, String.format("%.2f", priceCache.getModalPrice()), count);
            
            return priceCache;
            
        } catch (Exception e) {
            logger.error("Failed to parse data.gov.in response: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Fallback to CSV data from MarketTrendService
     */
    private LivePriceResponse getFallbackPrice(String crop, String district) {
        try {
            // Get average price from CSV data
            List<MarketTrend> trends = marketTrendService.getTrendsByDistrict(district);
            
            if (trends != null && !trends.isEmpty()) {
                // Find matching crop
                OptionalDouble avgPrice = trends.stream()
                    .filter(t -> t.getCmdty() != null && 
                                 t.getCmdty().toUpperCase().contains(crop) &&
                                 t.getpModal() != null)
                    .mapToDouble(MarketTrend::getpModal)
                    .average();
                
                if (avgPrice.isPresent()) {
                    PriceCache fallback = new PriceCache(crop, district, "Average Market", avgPrice.getAsDouble());
                    fallback.setSource("CSV_FALLBACK");
                    fallback.setPriceDate(LocalDateTime.now().minusDays(7)); // Assume week-old data
                    
                    LivePriceResponse response = LivePriceResponse.success(fallback);
                    response.setMessage("Using average price from dataset (may be up to 7 days old)");
                    return response;
                }
            }
            
            // No data found - use default estimate
            return getDefaultPrice(crop, district);
            
        } catch (Exception e) {
            logger.warn("CSV fallback failed: {}", e.getMessage());
            return getDefaultPrice(crop, district);
        }
    }
    
    /**
     * Get default estimated price when no data available
     * Updated: November 2025 - Realistic Karnataka market prices for top 20+ crops
     */
    private LivePriceResponse getDefaultPrice(String crop, String district) {
        // Comprehensive price table for Karnataka's top crops (₹ per quintal)
        // Based on typical Karnataka APMC rates - November 2025
        Map<String, Double> defaultPrices = new HashMap<>();
        
        // === CEREALS ===
        defaultPrices.put("RICE", 2800.0);
        defaultPrices.put("PADDY", 2800.0);
        defaultPrices.put("WHEAT", 2500.0);
        defaultPrices.put("MAIZE", 2200.0);
        defaultPrices.put("CORN", 2200.0);
        defaultPrices.put("JOWAR", 3200.0);
        defaultPrices.put("SORGHUM", 3200.0);
        defaultPrices.put("RAGI", 3800.0);
        defaultPrices.put("FINGER MILLET", 3800.0);
        defaultPrices.put("BAJRA", 2400.0);
        defaultPrices.put("PEARL MILLET", 2400.0);
        
        // === PULSES (Higher prices - protein crops) ===
        defaultPrices.put("CHICKPEA", 7500.0);  // Your expected ₹7000
        defaultPrices.put("GRAM", 7500.0);
        defaultPrices.put("BENGAL GRAM", 7500.0);
        defaultPrices.put("CHANA", 7500.0);
        defaultPrices.put("ARHAR", 7200.0);
        defaultPrices.put("TUR", 7200.0);
        defaultPrices.put("PIGEON PEA", 7200.0);
        defaultPrices.put("MOONG", 8500.0);
        defaultPrices.put("GREEN GRAM", 8500.0);
        defaultPrices.put("URAD", 8000.0);
        defaultPrices.put("BLACK GRAM", 8000.0);
        defaultPrices.put("HORSE GRAM", 3500.0);
        defaultPrices.put("KULTHI", 3500.0);
        defaultPrices.put("MASOOR", 6500.0);
        defaultPrices.put("LENTIL", 6500.0);
        
        // === OILSEEDS ===
        defaultPrices.put("GROUNDNUT", 6500.0);
        defaultPrices.put("PEANUT", 6500.0);
        defaultPrices.put("SUNFLOWER", 6800.0);
        defaultPrices.put("SOYBEAN", 4500.0);
        defaultPrices.put("SAFFLOWER", 5800.0);
        defaultPrices.put("SESAME", 12000.0);
        defaultPrices.put("MUSTARD", 5500.0);
        defaultPrices.put("CASTOR", 7000.0);
        
        // === CASH CROPS ===
        defaultPrices.put("COTTON", 7800.0);
        defaultPrices.put("SUGARCANE", 3200.0);
        defaultPrices.put("TOBACCO", 18000.0);
        
        // === SPICES (Premium prices) ===
        defaultPrices.put("CHILI", 8500.0);
        defaultPrices.put("CHILLI", 8500.0);
        defaultPrices.put("PEPPER", 8500.0);
        defaultPrices.put("TURMERIC", 9500.0);
        defaultPrices.put("CORIANDER", 7500.0);
        defaultPrices.put("CUMIN", 15000.0);
        defaultPrices.put("CARDAMOM", 180000.0);
        defaultPrices.put("GINGER", 8000.0);
        defaultPrices.put("GARLIC", 7000.0);
        
        // === VEGETABLES (Fresh market rates) ===
        defaultPrices.put("TOMATO", 2800.0);
        defaultPrices.put("ONION", 3500.0);
        defaultPrices.put("POTATO", 2200.0);
        defaultPrices.put("CABBAGE", 1500.0);
        defaultPrices.put("CAULIFLOWER", 2500.0);
        defaultPrices.put("BRINJAL", 2000.0);
        defaultPrices.put("EGGPLANT", 2000.0);
        defaultPrices.put("BEANS", 3500.0);
        defaultPrices.put("CARROT", 2800.0);
        defaultPrices.put("BEETROOT", 2500.0);
        defaultPrices.put("CUCUMBER", 1800.0);
        defaultPrices.put("PUMPKIN", 1200.0);
        defaultPrices.put("BOTTLE GOURD", 1500.0);
        defaultPrices.put("RIDGE GOURD", 2000.0);
        
        // === FRUITS ===
        defaultPrices.put("BANANA", 1800.0);
        defaultPrices.put("MANGO", 4500.0);
        defaultPrices.put("PAPAYA", 1500.0);
        defaultPrices.put("POMEGRANATE", 8500.0);
        defaultPrices.put("GRAPES", 6000.0);
        defaultPrices.put("COCONUT", 2500.0);
        defaultPrices.put("WATERMELON", 800.0);
        
        Double price = defaultPrices.getOrDefault(crop, 3000.0); // Generic fallback ₹3000
        
        PriceCache estimate = new PriceCache(crop, district, "Karnataka Market Estimate", price);
        estimate.setSource("DEFAULT_ESTIMATE");
        estimate.setPriceDate(LocalDateTime.now());
        
        LivePriceResponse response = LivePriceResponse.success(estimate);
        response.setMessage("Using Karnataka market estimate (data.gov.in API unavailable)");
        
        logger.info("Using default price for {}: ₹{}/quintal", crop, price);
        
        return response;
    }
    
    /**
     * Check if crop is in top 12 for live pricing
     */
    private boolean isCropSupported(String crop) {
        return SUPPORTED_CROPS.stream()
            .anyMatch(supported -> crop.contains(supported) || supported.contains(crop));
    }
    
    /**
     * Get all supported crops
     */
    public Set<String> getSupportedCrops() {
        return SUPPORTED_CROPS;
    }
    
    /**
     * Clean up expired cache entries (called periodically)
     */
    public void cleanupExpiredCache() {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(7);
        priceCacheRepository.deleteExpiredPrices(expiryDate);
        logger.info("Cleaned up price cache older than 7 days");
    }
}
