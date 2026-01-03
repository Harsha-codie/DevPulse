package ai.cropplanner.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

/**
 * Service to handle crop season information and validation for Karnataka, India
 */
@Service
public class SeasonService {
    
    private static final Logger logger = LoggerFactory.getLogger(SeasonService.class);
    
    // Karnataka seasons (based on South Indian agricultural calendar)
    public enum Season {
        KHARIF,     // Monsoon season (June-October) - Main growing season
        RABI,       // Winter season (November-February) - Post-monsoon
        SUMMER,     // Summer season (March-May) - Hot & dry
        YEAR_ROUND  // Crops that can be grown throughout the year
    }
    
    // Crop to Season mapping (Karnataka-specific)
    private static final Map<String, Set<Season>> CROP_SEASONS = new HashMap<>();
    
    static {
        // KHARIF crops (Monsoon: June-October)
        addCropSeasons("RICE", Season.KHARIF, Season.RABI);
        addCropSeasons("PADDY", Season.KHARIF, Season.RABI);
        addCropSeasons("MAIZE", Season.KHARIF, Season.RABI, Season.SUMMER);
        addCropSeasons("CORN", Season.KHARIF, Season.RABI, Season.SUMMER);
        addCropSeasons("COTTON", Season.KHARIF);
        addCropSeasons("SOYBEAN", Season.KHARIF);
        addCropSeasons("GROUNDNUT", Season.KHARIF, Season.SUMMER);
        addCropSeasons("PEANUT", Season.KHARIF, Season.SUMMER);
        addCropSeasons("PEARL MILLET", Season.KHARIF, Season.SUMMER);
        addCropSeasons("BAJRA", Season.KHARIF, Season.SUMMER);
        addCropSeasons("SORGHUM", Season.KHARIF, Season.RABI);
        addCropSeasons("JOWAR", Season.KHARIF, Season.RABI);
        
        // RABI crops (Winter: November-February)
        addCropSeasons("WHEAT", Season.RABI);
        addCropSeasons("CHICKPEA", Season.RABI);
        addCropSeasons("GRAM", Season.RABI);
        addCropSeasons("LENTIL", Season.RABI);
        addCropSeasons("MUSTARD", Season.RABI);
        addCropSeasons("BARLEY", Season.RABI);
        addCropSeasons("PEAS", Season.RABI);
        
        // SUMMER crops (March-May)
        addCropSeasons("WATERMELON", Season.SUMMER);
        addCropSeasons("MUSKMELON", Season.SUMMER);
        addCropSeasons("CUCUMBER", Season.SUMMER, Season.KHARIF);
        addCropSeasons("PUMPKIN", Season.SUMMER, Season.KHARIF);
        addCropSeasons("BITTER GOURD", Season.SUMMER, Season.KHARIF);
        
        // Year-round vegetables (with proper irrigation)
        addCropSeasons("TOMATO", Season.YEAR_ROUND);
        addCropSeasons("POTATO", Season.RABI, Season.SUMMER);
        addCropSeasons("ONION", Season.YEAR_ROUND);
        addCropSeasons("CABBAGE", Season.RABI, Season.SUMMER);
        addCropSeasons("CAULIFLOWER", Season.RABI, Season.SUMMER);
        addCropSeasons("CARROT", Season.RABI, Season.SUMMER);
        addCropSeasons("BRINJAL", Season.YEAR_ROUND);
        addCropSeasons("EGGPLANT", Season.YEAR_ROUND);
        addCropSeasons("CHILI", Season.YEAR_ROUND);
        addCropSeasons("PEPPER", Season.YEAR_ROUND);
        addCropSeasons("BEANS", Season.YEAR_ROUND);
        addCropSeasons("OKRA", Season.KHARIF, Season.SUMMER);
        addCropSeasons("LADYFINGER", Season.KHARIF, Season.SUMMER);
        addCropSeasons("SPINACH", Season.RABI, Season.SUMMER);
        addCropSeasons("CORIANDER", Season.YEAR_ROUND);
        
        // Fruits (perennial - planting season matters, not growing season)
        // These should be recommended based on planting time
        addCropSeasons("MANGO", Season.KHARIF); // Planting: June-August (after monsoon starts)
        addCropSeasons("BANANA", Season.YEAR_ROUND);
        addCropSeasons("PAPAYA", Season.YEAR_ROUND);
        addCropSeasons("GRAPES", Season.RABI); // Planting: October-January
        addCropSeasons("POMEGRANATE", Season.RABI, Season.KHARIF);
        
        // Flowers
        addCropSeasons("ROSE", Season.YEAR_ROUND);
        addCropSeasons("MARIGOLD", Season.YEAR_ROUND);
        addCropSeasons("JASMINE", Season.YEAR_ROUND);
        addCropSeasons("CHRYSANTHEMUM", Season.RABI, Season.SUMMER);
        
        // Sugarcane (long duration)
        addCropSeasons("SUGARCANE", Season.YEAR_ROUND);
        
        // Turmeric & Ginger
        addCropSeasons("TURMERIC", Season.KHARIF);
        addCropSeasons("GINGER", Season.KHARIF);
    }
    
    private static void addCropSeasons(String crop, Season... seasons) {
        CROP_SEASONS.put(crop.toUpperCase(), new HashSet<>(Arrays.asList(seasons)));
    }
    
    /**
     * Get current season based on current date (Karnataka calendar)
     */
    public Season getCurrentSeason() {
        return getSeasonForDate(LocalDate.now());
    }
    
    /**
     * Get season for a specific date
     */
    public Season getSeasonForDate(LocalDate date) {
        Month month = date.getMonth();
        
        switch (month) {
            case JUNE:
            case JULY:
            case AUGUST:
            case SEPTEMBER:
            case OCTOBER:
                return Season.KHARIF;  // Monsoon/Wet season
                
            case NOVEMBER:
            case DECEMBER:
            case JANUARY:
            case FEBRUARY:
                return Season.RABI;    // Winter/Post-monsoon
                
            case MARCH:
            case APRIL:
            case MAY:
                return Season.SUMMER;  // Summer/Hot season
                
            default:
                return Season.KHARIF;
        }
    }
    
    /**
     * Check if a crop is suitable for current season
     */
    public boolean isCropSuitableForSeason(String cropName, Season season) {
        String normalizedCrop = cropName.toUpperCase().trim();
        
        Set<Season> suitableSeasons = CROP_SEASONS.get(normalizedCrop);
        if (suitableSeasons == null) {
            // Unknown crop - allow it (might be new/regional variety)
            logger.debug("Unknown crop '{}' - allowing for season {}", cropName, season);
            return true;
        }
        
        // Year-round crops are always suitable
        if (suitableSeasons.contains(Season.YEAR_ROUND)) {
            return true;
        }
        
        return suitableSeasons.contains(season);
    }
    
    /**
     * Check if crop is suitable for current season
     */
    public boolean isCropSuitableNow(String cropName) {
        return isCropSuitableForSeason(cropName, getCurrentSeason());
    }
    
    /**
     * Get suitable seasons for a crop
     */
    public Set<Season> getSuitableSeasonsForCrop(String cropName) {
        String normalizedCrop = cropName.toUpperCase().trim();
        return CROP_SEASONS.getOrDefault(normalizedCrop, Set.of(Season.YEAR_ROUND));
    }
    
    /**
     * Get season description for farmers
     */
    public String getSeasonDescription(Season season) {
        switch (season) {
            case KHARIF:
                return "Kharif (Monsoon Season: June-October) - Main growing season with good rainfall";
            case RABI:
                return "Rabi (Winter Season: November-February) - Post-monsoon with cooler temperatures";
            case SUMMER:
                return "Summer (March-May) - Hot and dry season, requires irrigation";
            case YEAR_ROUND:
                return "Year-round (with proper irrigation and care)";
            default:
                return "Unknown season";
        }
    }
    
    /**
     * Get comprehensive season information for recommendation prompt
     */
    public String getSeasonInfoForPrompt() {
        Season currentSeason = getCurrentSeason();
        LocalDate now = LocalDate.now();
        
        StringBuilder info = new StringBuilder();
        info.append("CURRENT SEASON INFORMATION:\n");
        info.append("Date: ").append(now).append("\n");
        info.append("Season: ").append(currentSeason).append("\n");
        info.append("Description: ").append(getSeasonDescription(currentSeason)).append("\n\n");
        
        info.append("SEASON-SPECIFIC GUIDANCE:\n");
        switch (currentSeason) {
            case KHARIF:
                info.append("- This is the MAIN growing season with monsoon rains\n");
                info.append("- Best for: Rice, Maize, Cotton, Sorghum, Groundnut, Soybean\n");
                info.append("- Vegetables: Tomato, Cucumber, Okra, Bitter Gourd\n");
                info.append("- Planting time for Mango saplings (if establishing orchard)\n");
                break;
            case RABI:
                info.append("- POST-MONSOON season with stored soil moisture\n");
                info.append("- Best for: Wheat, Chickpea, Mustard, Lentil, Vegetables\n");
                info.append("- Good for cool-season vegetables: Cabbage, Cauliflower, Carrot\n");
                info.append("- Planting time for Grapes\n");
                break;
            case SUMMER:
                info.append("- HOT and DRY season - requires good irrigation\n");
                info.append("- Best for: Summer vegetables, Watermelon, Muskmelon\n");
                info.append("- Can grow: Maize, Groundnut, Pearl Millet (with irrigation)\n");
                info.append("- NOT suitable for Mango planting (wait for Kharif)\n");
                break;
            case YEAR_ROUND:
                info.append("- Crops that can be grown throughout year with proper care\n");
                break;
        }
        
        info.append("\nIMPORTANT: **DO NOT recommend crops outside their planting season**\n");
        info.append("For example: Do NOT recommend Mango/Grapes in Summer, or Wheat in Kharif.\n\n");
        
        return info.toString();
    }
    
    /**
     * Get list of all crops suitable for current season
     */
    public List<String> getCropsForCurrentSeason() {
        Season currentSeason = getCurrentSeason();
        List<String> suitableCrops = new ArrayList<>();
        
        for (Map.Entry<String, Set<Season>> entry : CROP_SEASONS.entrySet()) {
            if (entry.getValue().contains(Season.YEAR_ROUND) || 
                entry.getValue().contains(currentSeason)) {
                suitableCrops.add(entry.getKey());
            }
        }
        
        Collections.sort(suitableCrops);
        return suitableCrops;
    }
}
