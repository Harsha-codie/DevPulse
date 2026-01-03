package ai.cropplanner.recommendation;

import ai.cropplanner.crop.DistrictCropSuitability;
import ai.cropplanner.crop.DistrictCropSuitabilityRepository;
import ai.cropplanner.location.LocationDetails;
import ai.cropplanner.location.LocationService;
import ai.cropplanner.location.Pincode;
import ai.cropplanner.location.PincodeRepository;
import ai.cropplanner.market.MarketTrend;
import ai.cropplanner.market.MarketTrendService;
import ai.cropplanner.soil.SoilData;
import ai.cropplanner.soil.SoilGridsClient;
import ai.cropplanner.weather.WeatherClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    
    private final GeminiClient geminiClient;
    private final WeatherClient weatherClient;
    private final SoilGridsClient soilGridsClient;
    private final MarketTrendService marketTrendService;
    private final SeasonService seasonService;
    private final LocationService locationService;
    private final PincodeRepository pincodeRepository;
    private final DistrictCropSuitabilityRepository cropSuitabilityRepository;
    private final ObjectMapper objectMapper;
    
    public RecommendationService(GeminiClient geminiClient, 
                               WeatherClient weatherClient,
                               SoilGridsClient soilGridsClient,
                               MarketTrendService marketTrendService,
                               SeasonService seasonService,
                               LocationService locationService,
                               PincodeRepository pincodeRepository,
                               DistrictCropSuitabilityRepository cropSuitabilityRepository) {
        this.geminiClient = geminiClient;
        this.weatherClient = weatherClient;
        this.soilGridsClient = soilGridsClient;
        this.marketTrendService = marketTrendService;
        this.seasonService = seasonService;
        this.locationService = locationService;
        this.pincodeRepository = pincodeRepository;
        this.cropSuitabilityRepository = cropSuitabilityRepository;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Generate crop recommendations based on location and preferences
     */
    public RecommendationResponse generateRecommendations(RecommendationRequest request) {
        try {
            logger.info("Generating recommendations for request: {}", request);
            
            // 1. Resolve district from location if needed
            String district = resolveDistrict(request);
            if (district == null) {
                return RecommendationResponse.error("Unable to determine district from location");
            }
            
            // 2. Gather contextual data
            ContextData context = gatherContextData(request, district);
            
            // 3. Build comprehensive prompt
            String prompt = buildComprehensivePrompt(context, request);
            
            // 4. Get Gemini recommendations
            String geminiResponse = geminiClient.generateRecommendations(prompt);
            
            // 5. Parse and validate response
            List<CropRecommendation> recommendations = parseGeminiResponse(geminiResponse);
            
            logger.info("Successfully generated {} recommendations for district: {}", 
                recommendations.size(), district);
            
            return new RecommendationResponse(recommendations, district);
            
        } catch (Exception e) {
            logger.error("Failed to generate recommendations: {}", e.getMessage(), e);
            return RecommendationResponse.error("Failed to generate recommendations: " + e.getMessage());
        }
    }
    
    /**
     * Resolve district name and coordinates from request
     * Priority: pincode → village/taluk → district → coordinates
     */
    private String resolveDistrict(RecommendationRequest request) {
        LocationDetails location = null;
        
        // Priority 1: Pincode (easiest and most accurate)
        if (request.getPincode() != null && !request.getPincode().trim().isEmpty()) {
            logger.info("Resolving location from pincode: {}", request.getPincode());
            location = locationService.resolveFromPincode(request.getPincode());
            
            if (location != null) {
                // Auto-populate coordinates and district from pincode
                request.setLatitude(location.getLatitude());
                request.setLongitude(location.getLongitude());
                request.setVillage(location.getVillage());
                request.setTaluk(location.getTaluk());
                request.setDistrict(location.getDistrict());
                
                logger.info("Pincode resolved to: {}", location.getFullAddress());
                return location.getDistrict().toUpperCase();
            }
        }
        
        // Priority 2: Village + Taluk (manual entry with fuzzy matching)
        if (request.getVillage() != null && !request.getVillage().trim().isEmpty()) {
            logger.info("Resolving location from village: {}, taluk: {}, district: {}", 
                       request.getVillage(), request.getTaluk(), request.getDistrict());
            
            location = locationService.resolveFromVillage(
                request.getVillage(), 
                request.getTaluk(), 
                request.getDistrict()
            );
            
            if (location != null) {
                // Auto-populate coordinates if not already set
                if (request.getLatitude() == null || request.getLongitude() == null) {
                    request.setLatitude(location.getLatitude());
                    request.setLongitude(location.getLongitude());
                }
                
                // Use resolved district (might be corrected from typo)
                if (request.getDistrict() == null || request.getDistrict().trim().isEmpty()) {
                    request.setDistrict(location.getDistrict());
                }
                
                logger.info("Village resolved to: {} ({})", location.getFullAddress(), location.getSource());
                if (location.getMessage() != null) {
                    logger.info("Location message: {}", location.getMessage());
                }
                
                return location.getDistrict().toUpperCase();
            }
        }
        
        // Priority 3: District only (fallback)
        if (request.getDistrict() != null && !request.getDistrict().trim().isEmpty()) {
            return request.getDistrict().trim().toUpperCase();
        }
        
        // Priority 4: Coordinates only (GPS) - Find nearest district
        if (request.getLatitude() != null && request.getLongitude() != null) {
            logger.info("Attempting reverse geocoding for coordinates: {}, {}", 
                       request.getLatitude(), request.getLongitude());
            
            // Try to find nearest district from pincode database
            String nearestDistrict = findNearestDistrict(request.getLatitude(), request.getLongitude());
            
            if (nearestDistrict != null) {
                logger.info("Reverse geocoded to district: {}", nearestDistrict);
                request.setDistrict(nearestDistrict);
                return nearestDistrict.toUpperCase();
            }
            
            // Fallback to default Karnataka district if reverse geocoding fails
            logger.warn("Reverse geocoding failed - using default BENGALURU URBAN");
            return "BENGALURU URBAN";
        }
        
        return null;
    }
    
    /**
     * Find nearest district using simple distance calculation from pincode database
     * Uses Haversine formula to calculate distance between coordinates
     */
    private String findNearestDistrict(Double lat, Double lon) {
        try {
            // Get all pincodes with coordinates from database
            List<Pincode> allPincodes = pincodeRepository.findAll();
            
            String nearestDistrict = null;
            double minDistance = Double.MAX_VALUE;
            
            for (Pincode pincode : allPincodes) {
                if (pincode.getLatitude() != null && pincode.getLongitude() != null) {
                    double distance = calculateDistance(
                        lat, lon, 
                        pincode.getLatitude(), pincode.getLongitude()
                    );
                    
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestDistrict = pincode.getDistrict();
                    }
                }
            }
            
            if (nearestDistrict != null) {
                logger.info("Nearest district found: {} at {} km", nearestDistrict, String.format("%.2f", minDistance));
            }
            
            return nearestDistrict;
        } catch (Exception e) {
            logger.error("Error in reverse geocoding: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     * Returns distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Gather all contextual data for recommendations
     */
    private ContextData gatherContextData(RecommendationRequest request, String district) {
        ContextData context = new ContextData();
        context.district = district;
        
        // Gather weather data
        try {
            if (request.getLatitude() != null && request.getLongitude() != null) {
                context.weatherData = weatherClient.getWeather(
                    request.getLatitude(), request.getLongitude()
                );
            }
            // Note: No fallback method available in WeatherClient
        } catch (Exception e) {
            logger.warn("Failed to fetch weather data: {}", e.getMessage());
        }
        
        // Gather soil data
        try {
            if (request.getLatitude() != null && request.getLongitude() != null) {
                context.soilData = soilGridsClient.getSoilData(
                    request.getLatitude(), request.getLongitude()
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch soil data: {}", e.getMessage());
        }
        
        // Gather market trends
        try {
            context.marketTrends = marketTrendService.getTrendsByDistrict(district);
            context.marketSummary = marketTrendService.getDistrictSummary(district);
        } catch (Exception e) {
            logger.warn("Failed to fetch market data: {}", e.getMessage());
        }
        
        return context;
    }
    
    /**
     * Build comprehensive prompt with all contextual data
     */
    private String buildComprehensivePrompt(ContextData context, RecommendationRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert agricultural advisor. Analyze the following data and recommend 1-3 crops ");
        prompt.append("that would be most profitable and suitable for farming in this location.\n\n");
        
        // IMPORTANT: Add current season information FIRST
        prompt.append(seasonService.getSeasonInfoForPrompt());
        
        // District information
        prompt.append("LOCATION:\n");
        prompt.append("District: ").append(context.district).append("\n\n");
        
        // CRITICAL: Add crop constraints based on district and season
        addCropConstraints(prompt, context.district, request.getSeason());
        
        // Weather data
        if (context.weatherData != null) {
            try {
                JsonNode weatherJson = objectMapper.readTree(context.weatherData);
                JsonNode main = weatherJson.path("main");
                JsonNode weather = weatherJson.path("weather");
                
                prompt.append("CURRENT WEATHER CONDITIONS:\n");
                if (!main.isMissingNode()) {
                    double tempK = main.path("temp").asDouble();
                    double tempC = tempK - 273.15; // Convert from Kelvin to Celsius
                    prompt.append("Temperature: ").append(String.format("%.1f", tempC)).append("°C\n");
                    prompt.append("Humidity: ").append(main.path("humidity").asInt()).append("%\n");
                }
                if (weather.isArray() && weather.size() > 0) {
                    prompt.append("Weather: ").append(weather.get(0).path("description").asText()).append("\n");
                }
                prompt.append("\n");
            } catch (Exception e) {
                logger.warn("Failed to parse weather data: {}", e.getMessage());
                // Weather data unavailable - AI will use other factors
            }
        } else {
            logger.info("No weather data available - recommendations will be based on soil, market, and seasonal data");
        }
        
        // Soil data
        if (context.soilData != null) {
            prompt.append("SOIL COMPOSITION:\n");
            prompt.append("Clay: ").append(String.format("%.1f", context.soilData.clay)).append("%\n");
            prompt.append("Sand: ").append(String.format("%.1f", context.soilData.sand)).append("%\n");
            prompt.append("Silt: ").append(String.format("%.1f", context.soilData.silt)).append("%\n");
            prompt.append("pH: ").append(String.format("%.1f", context.soilData.phh2o)).append("\n\n");
        }
        
        // Market trends - shortened to top 3 only
        if (context.marketSummary != null && context.marketTrends != null && !context.marketTrends.isEmpty()) {
            prompt.append("MARKET: Top 3 crops: ");
            
            // Top 3 crops by average price
            String topCrops = context.marketTrends.stream()
                .filter(trend -> trend.getpModal() != null)
                .collect(Collectors.groupingBy(
                    MarketTrend::getCmdty,
                    Collectors.averagingDouble(MarketTrend::getpModal)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(e -> e.getKey() + " (₹" + String.format("%.0f", e.getValue()) + "/q)")
                .collect(Collectors.joining(", "));
            
            prompt.append(topCrops).append("\n\n");
        }
        
        // User preferences
        if (request.getPreferredCrops() != null && !request.getPreferredCrops().isEmpty()) {
            prompt.append("FARMER PREFERENCES:\n");
            prompt.append("Preferred crops: ").append(request.getPreferredCrops()).append("\n");
        }
        if (request.getSeason() != null) {
            prompt.append("Season: ").append(request.getSeason()).append("\n");
        }
        if (request.getFarmSize() != null) {
            prompt.append("Farm size: ").append(request.getFarmSize()).append("\n");
        }
        if (request.getSoilType() != null) {
            prompt.append("Farmer-reported soil type: ").append(request.getSoilType()).append("\n");
        }
        prompt.append("\n");
        
        // Language instruction - MUST BE AT THE TOP AND VERY EXPLICIT
        String language = request.getLanguage();
        if (language != null && !language.isEmpty()) {
            String languageName = getLanguageName(language);
            if ("kn".equals(language) || "Kannada".equalsIgnoreCase(languageName)) {
                // MAXIMUM ENFORCEMENT FOR KANNADA - ABSOLUTE REQUIREMENT
                prompt.insert(0, "\n\n═══════════════════════════════════════════════\n");
                prompt.insert(0, "🚫 BANNED ENGLISH WORDS - NEVER USE THESE:\n");
                prompt.insert(0, "Ragi→ಬಾಜ್ರಾ/ರಾಗಿ | Paddy/Rice→ಭತ್ತ | Jowar/Maize→ಜೋಳ | Wheat→ಗೋಧಿ | Sugarcane→ಕಬ್ಬು\n");
                prompt.insert(0, "Groundnut→ಶೇಂಗಾ | Pulses→ಕಾಳು | Cotton→ಹತ್ತಿ | Vegetables→ತರಕಾರಿಗಳು\n");
                prompt.insert(0, "suitable→ಸೂಕ್ತ | good→ಉತ್ತಮ | high→ಹೆಚ್ಚು | soil→ಮಣ್ಣು | weather→ಹವಾಮಾನ\n");
                prompt.insert(0, "price→ಬೆಲೆ | rainfall→ಮಳೆ | profit→ಲಾಭ | market→ಮಾರುಕಟ್ಟೆ | season→ಋತು\n\n");
                prompt.insert(0, "EXAMPLE OUTPUT (COPY THIS EXACT FORMAT):\n");
                prompt.insert(0, "{\n  \"recommendations\": [\n    {\"crop\": \"ರಾಗಿ\", \"score\": 85, \"reason\": \"ಮಣ್ಣು ಮತ್ತು ಹವಾಮಾನಕ್ಕೆ ಸೂಕ್ತ, ಉತ್ತಮ ಬೆಲೆ\"},\n    {\"crop\": \"ಭತ್ತ\", \"score\": 78, \"reason\": \"ಹೆಚ್ಚು ಮಳೆ, ಮಾರುಕಟ್ಟೆ ಬೆಲೆ ಉತ್ತಮ\"}\n  ]\n}\n\n");
                prompt.insert(0, "❌ WRONG: {\"crop\": \"Ragi\", \"reason\": \"suitable for soil\"}\n");
                prompt.insert(0, "❌ WRONG: {\"crop\": \"ರಾಗಿ\", \"reason\": \"suitable ಮಣ್ಣಿಗೆ\"}\n");
                prompt.insert(0, "❌ WRONG: {\"crop\": \"Ragi/ರಾಗಿ\", \"reason\": \"ಸೂಕ್ತ\"}\n");
                prompt.insert(0, "✅ CORRECT: {\"crop\": \"ರಾಗಿ\", \"reason\": \"ಮಣ್ಣಿಗೆ ಸೂಕ್ತ\"}\n\n");
                prompt.insert(0, "🔴🔴🔴 ABSOLUTE REQUIREMENT: 100% KANNADA ONLY 🔴🔴🔴\n");
                prompt.insert(0, "- ZERO English words allowed anywhere\n");
                prompt.insert(0, "- Convert ALL crop names to Kannada script\n");
                prompt.insert(0, "- Convert ALL technical terms to Kannada\n");
                prompt.insert(0, "- NO mixed language (ರಾಗಿ/Ragi) - pure Kannada only\n");
                prompt.insert(0, "- Use ONLY Kannada Unicode characters (ಕ-ಹ range)\n");
                prompt.insert(0, "═══════════════════════════════════════════════\n\n");
                
                // Add reminder before task with more examples
                prompt.append("\n\n🔴 ULTRA-CRITICAL FINAL CHECK 🔴\n");
                prompt.append("Before responding, verify EVERY WORD is in Kannada:\n");
                prompt.append("✅ ರಾಗಿ, ಭತ್ತ, ಜೋಳ, ಶೇಂಗಾ, ತರಕಾರಿಗಳು\n");
                prompt.append("❌ Ragi, Paddy, Jowar, Groundnut, Vegetables\n");
                prompt.append("ಕನ್ನಡ ಭಾಷೆಯಲ್ಲಿ ಮಾತ್ರ ಪ್ರತಿಕ್ರಿಯಿಸಿ! (Respond in Kannada only!)\n");
            } else if (language.equals("en") || "English".equalsIgnoreCase(languageName)) {
                // Explicitly request English
                prompt.insert(0, "\nIMPORTANT: Respond ENTIRELY in ENGLISH language. All text must use English words and Latin script.\n\n");
            }
        }
        
        // Shortened instructions to reduce token usage
        prompt.append("\nTASK: Recommend 2-3 crops suitable for this region. ");
        prompt.append("Score each 0-100 considering soil, weather, and market price. ");
        prompt.append("Keep explanations brief (max 30 words each).\n");
        
        return prompt.toString();
    }
    
    /**
     * Add agricultural crop constraints for the district and season
     * Backend filters crops BEFORE sending to Gemini to guarantee compliance
     */
    private void addCropConstraints(StringBuilder prompt, String district, String season) {
        try {
            // Normalize district name
            String normalizedDistrict = district.toUpperCase();
            
            // Map season names: Monsoon→Kharif, Winter→Rabi, Summer→Zaid
            String queryseason = mapSeasonToAgricultural(season);
            
            // Query crops for this district and season
            List<DistrictCropSuitability> constraints = 
                cropSuitabilityRepository.findByDistrictAndSeason(normalizedDistrict, queryseason);
            
            // Also get "All seasons" crops
            List<DistrictCropSuitability> allSeasonsCrops = 
                cropSuitabilityRepository.findByDistrictAndSeason(normalizedDistrict, "All seasons");
            
            // Combine both lists
            java.util.Set<String> allowedCropsSet = new java.util.HashSet<>();
            for (DistrictCropSuitability constraint : constraints) {
                if (constraint.getSuitableCrops() != null && !constraint.getSuitableCrops().trim().isEmpty()) {
                    String[] crops = constraint.getSuitableCrops().split(",");
                    for (String crop : crops) {
                        allowedCropsSet.add(crop.trim());
                    }
                }
            }
            for (DistrictCropSuitability constraint : allSeasonsCrops) {
                if (constraint.getSuitableCrops() != null && !constraint.getSuitableCrops().trim().isEmpty()) {
                    String[] crops = constraint.getSuitableCrops().split(",");
                    for (String crop : crops) {
                        allowedCropsSet.add(crop.trim());
                    }
                }
            }
            
            if (allowedCropsSet.isEmpty()) {
                logger.warn("No crop constraints found for district: {} season: {}", normalizedDistrict, queryseason);
                return;
            }
            
            // Build strict constraint prompt
            String allowedCropsList = String.join(", ", allowedCropsSet);
            
            prompt.append("---------------------------------------------------------\n");
            prompt.append("DISTRICT–SEASON CROPS (already filtered by backend):\n");
            prompt.append(allowedCropsList).append("\n\n");
            prompt.append("🚨 CRITICAL CONSTRAINT 🚨\n");
            prompt.append("You MUST recommend ONLY from the above list.\n");
            prompt.append("DO NOT generate crops outside this list.\n");
            prompt.append("If a crop has details in brackets like 'Pulses (tur/gram/greengram)',\n");
            prompt.append("recommend the SPECIFIC crop name (e.g., 'Tur' or 'Greengram'), NOT the category.\n");
            prompt.append("---------------------------------------------------------\n\n");
            
            logger.info("Added {} allowed crops for {} ({})", allowedCropsSet.size(), normalizedDistrict, queryseason);
            
        } catch (Exception e) {
            logger.error("Failed to add crop constraints: {}", e.getMessage(), e);
            // Continue without constraints - graceful degradation
        }
    }
    
    /**
     * Map user-friendly season names to agricultural seasons
     */
    private String mapSeasonToAgricultural(String season) {
        if (season == null || season.trim().isEmpty()) {
            // Use current season
            String currentSeason = seasonService.getCurrentSeason().toString();
            season = currentSeason;
        }
        
        String normalized = season.trim().toLowerCase();
        
        // Map to agricultural season names used in CSV
        if (normalized.contains("monsoon") || normalized.contains("kharif")) {
            return "Kharif";
        } else if (normalized.contains("winter") || normalized.contains("rabi")) {
            return "Rabi";
        } else if (normalized.contains("summer") || normalized.contains("zaid")) {
            return "Zaid";
        }
        
        // Default: treat as Kharif
        return "Kharif";
    }
    
    /**
     * Convert language code to full language name for Gemini
     */
    private String getLanguageName(String languageCode) {
        return switch (languageCode.toLowerCase()) {
            case "kn" -> "Kannada";
            case "en" -> "English";
            default -> "English"; // Fallback to English
        };
    }
    
    /**
     * Parse Gemini response and validate JSON structure
     */
    private List<CropRecommendation> parseGeminiResponse(String response) {
        try {
            logger.debug("Parsing Gemini response: {}", response);
            
            // Clean response (remove any non-JSON content)
            String cleanJson = cleanJsonResponse(response);
            
            JsonNode root = objectMapper.readTree(cleanJson);
            JsonNode recommendations = root.path("recommendations");
            
            if (!recommendations.isArray()) {
                throw new RuntimeException("Invalid response format: 'recommendations' should be an array");
            }
            
            List<CropRecommendation> result = new ArrayList<>();
            SeasonService.Season currentSeason = seasonService.getCurrentSeason();
            
            for (JsonNode rec : recommendations) {
                String crop = rec.path("crop").asText();
                int score = rec.path("score").asInt();
                String reason = rec.path("reason").asText();
                
                if (crop.isEmpty() || reason.isEmpty()) {
                    logger.warn("Skipping invalid recommendation: {}", rec);
                    continue;
                }
                
                if (score < 0 || score > 100) {
                    logger.warn("Invalid score {}, clamping to 0-100 range", score);
                    score = Math.max(0, Math.min(100, score));
                }
                
                // SEASON VALIDATION: Check if crop is suitable for current season
                if (!seasonService.isCropSuitableNow(crop)) {
                    logger.warn("Filtering out '{}' - not suitable for {} season. Suitable seasons: {}", 
                        crop, currentSeason, seasonService.getSuitableSeasonsForCrop(crop));
                    
                    // Reduce score significantly for out-of-season crops
                    score = Math.max(0, score - 50);
                    
                    // Add season warning to reason
                    Set<SeasonService.Season> suitableSeasons = seasonService.getSuitableSeasonsForCrop(crop);
                    String seasonsStr = suitableSeasons.stream()
                        .map(Enum::toString)
                        .collect(java.util.stream.Collectors.joining(", "));
                    
                    reason = "⚠️ NOT RECOMMENDED for current season (" + currentSeason + "). " +
                            "Suitable for: " + seasonsStr + ". " + reason;
                    
                    // Still add but with warning - or skip entirely
                    if (score < 30) {
                        logger.info("Skipping '{}' due to low seasonal score: {}", crop, score);
                        continue;
                    }
                }
                
                result.add(new CropRecommendation(crop, score, reason));
            }
            
            if (result.isEmpty()) {
                throw new RuntimeException("No valid recommendations found in response");
            }
            
            if (result.size() > 3) {
                logger.warn("More than 3 recommendations returned, keeping top 3");
                result = result.subList(0, 3);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to parse Gemini response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse recommendation response", e);
        }
    }
    
    /**
     * Clean JSON response to extract only the JSON part
     */
    private String cleanJsonResponse(String response) {
        // Find JSON boundaries
        int jsonStart = response.indexOf('{');
        int jsonEnd = response.lastIndexOf('}');
        
        if (jsonStart == -1 || jsonEnd == -1 || jsonStart >= jsonEnd) {
            throw new RuntimeException("No valid JSON found in response");
        }
        
        return response.substring(jsonStart, jsonEnd + 1);
    }
    
    /**
     * Internal class to hold context data
     */
    private static class ContextData {
        String district;
        String weatherData;  // JSON string from weather API
        SoilData soilData;
        List<MarketTrend> marketTrends;
        MarketTrendService.DistrictMarketSummary marketSummary;
    }
}