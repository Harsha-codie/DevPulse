package ai.cropplanner.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeedService {
    
    private static final Logger logger = LoggerFactory.getLogger(SeedService.class);
    
    // Crop-specific seed varieties
    private static final Map<String, List<SeedRecommendation>> SEED_DATABASE = new HashMap<>();
    
    static {
        // Rice varieties
        SEED_DATABASE.put("RICE", Arrays.asList(
            new SeedRecommendation("Hybrid", "MTU 1010", "120-130 days", "60-65 quintals/ha",
                "High yielding, disease resistant, good grain quality", "All soil types, irrigated"),
            new SeedRecommendation("Hybrid", "BPT 5204", "145-150 days", "55-60 quintals/ha",
                "Aromatic, medium slender grain, pest resistant", "Clay loam, high rainfall areas"),
            new SeedRecommendation("HYV", "IR-64", "115-120 days", "50-55 quintals/ha",
                "Drought tolerant, short duration", "Semi-irrigated areas")
        ));
        
        // Wheat varieties
        SEED_DATABASE.put("WHEAT", Arrays.asList(
            new SeedRecommendation("HYV", "HD 2967", "140-145 days", "45-50 quintals/ha",
                "Rust resistant, high protein content", "Irrigated, timely sown"),
            new SeedRecommendation("HYV", "DBW 88", "145-150 days", "50-55 quintals/ha",
                "High yielding, good chapati quality", "All irrigated areas"),
            new SeedRecommendation("Hybrid", "WH 1105", "135-140 days", "48-52 quintals/ha",
                "Early maturing, disease resistant", "Late sown conditions")
        ));
        
        // Maize varieties
        SEED_DATABASE.put("MAIZE", Arrays.asList(
            new SeedRecommendation("Hybrid", "DHM 117", "85-90 days", "70-75 quintals/ha",
                "Early maturity, high yielding, stress tolerant", "All soil types"),
            new SeedRecommendation("Hybrid", "Kaveri 50", "95-100 days", "80-85 quintals/ha",
                "Bold grains, excellent standability", "Irrigated areas"),
            new SeedRecommendation("Composite", "Navjot", "100-105 days", "45-50 quintals/ha",
                "Open pollinated, cost effective", "Rainfed areas")
        ));
        
        // Cotton varieties
        SEED_DATABASE.put("COTTON", Arrays.asList(
            new SeedRecommendation("BT Hybrid", "RCH 2", "150-160 days", "25-30 quintals/ha",
                "Bollworm resistant, long staple", "Black soil, irrigated"),
            new SeedRecommendation("BT Hybrid", "Mallika", "155-165 days", "28-32 quintals/ha",
                "High ginning outturn, disease resistant", "Medium to deep black soils"),
            new SeedRecommendation("Non-BT", "Suraj", "160-170 days", "20-25 quintals/ha",
                "Organic farming, good fiber quality", "Traditional cotton areas")
        ));
    }
    
    public List<SeedRecommendation> recommendSeeds(String crop) {
        logger.info("Fetching seed recommendations for crop: {}", crop);
        
        String cropKey = crop.toUpperCase();
        List<SeedRecommendation> recommendations = SEED_DATABASE.get(cropKey);
        
        if (recommendations == null) {
            // Return generic recommendations for unlisted crops
            logger.warn("No specific seed data for crop: {}. Returning generic recommendations", crop);
            return Arrays.asList(
                new SeedRecommendation("Local Variety", "Check with local dealers", "Variable", 
                    "Variable", "Contact local agricultural office", "Local climate"),
                new SeedRecommendation("Improved Variety", "Available at authorized dealers", "Variable",
                    "Higher than local", "Disease resistant, certified seeds", "Adapted to region")
            );
        }
        
        return recommendations;
    }
}
