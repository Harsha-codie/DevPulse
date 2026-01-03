package ai.cropplanner.fertilizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FertilizerService {
    
    private static final Logger logger = LoggerFactory.getLogger(FertilizerService.class);
    
    // Crop-specific NPK requirements (simplified)
    private static final Map<String, int[]> CROP_NPK_REQUIREMENTS = new HashMap<>();
    
    static {
        // Format: [N, P, K] in kg per acre
        CROP_NPK_REQUIREMENTS.put("RICE", new int[]{60, 30, 30});
        CROP_NPK_REQUIREMENTS.put("WHEAT", new int[]{80, 40, 40});
        CROP_NPK_REQUIREMENTS.put("MAIZE", new int[]{90, 45, 45});
        CROP_NPK_REQUIREMENTS.put("COTTON", new int[]{60, 30, 30});
        CROP_NPK_REQUIREMENTS.put("SUGARCANE", new int[]{120, 60, 60});
        CROP_NPK_REQUIREMENTS.put("POTATO", new int[]{100, 50, 75});
        CROP_NPK_REQUIREMENTS.put("TOMATO", new int[]{80, 40, 40});
        CROP_NPK_REQUIREMENTS.put("ONION", new int[]{75, 37, 50});
        CROP_NPK_REQUIREMENTS.put("GROUNDNUT", new int[]{20, 40, 40});
        CROP_NPK_REQUIREMENTS.put("SOYBEAN", new int[]{30, 60, 40});
    }
    
    public FertilizerResponse generateRecommendations(FertilizerRequest request) {
        try {
            logger.info("Generating fertilizer recommendations for crop: {}, district: {}", 
                request.getCrop(), request.getDistrict());
            
            List<FertilizerRecommendation> recommendations = new ArrayList<>();
            
            // Get crop requirements
            String cropKey = request.getCrop().toUpperCase();
            int[] npkRequired = CROP_NPK_REQUIREMENTS.getOrDefault(cropKey, new int[]{60, 30, 30});
            
            // Calculate deficiencies based on soil NPK
            double nDeficit = calculateDeficit(request.getNitrogen(), npkRequired[0]);
            double pDeficit = calculateDeficit(request.getPhosphorus(), npkRequired[1]);
            double kDeficit = calculateDeficit(request.getPotassium(), npkRequired[2]);
            
            // Generate recommendations based on deficiencies
            if (nDeficit > 10) {
                recommendations.add(createUreaRecommendation(nDeficit, request.getSeason()));
            }
            
            if (pDeficit > 10) {
                recommendations.add(createDAPRecommendation(pDeficit, request.getSeason()));
            }
            
            if (kDeficit > 10) {
                recommendations.add(createPotashRecommendation(kDeficit, request.getSeason()));
            }
            
            // Always recommend complex fertilizer for balanced nutrition
            recommendations.add(createComplexFertilizerRecommendation(
                npkRequired, request.getSeason(), request.getSoilPh()));
            
            // Add organic recommendations
            if ("MONSOON".equalsIgnoreCase(request.getSeason()) && 
                request.getAnnualRainfall() != null && request.getAnnualRainfall() > 800) {
                recommendations.add(createOrganicRecommendation(request.getCrop()));
            }
            
            logger.info("Generated {} fertilizer recommendations", recommendations.size());
            
            return new FertilizerResponse(recommendations, request.getCrop(), request.getDistrict());
            
        } catch (Exception e) {
            logger.error("Failed to generate fertilizer recommendations", e);
            return FertilizerResponse.error("Failed to generate recommendations: " + e.getMessage());
        }
    }
    
    private double calculateDeficit(Double currentLevel, int required) {
        if (currentLevel == null) return required;
        // Assuming soil test gives % of adequate levels
        double adequateLevel = (currentLevel / 100.0) * required;
        return Math.max(0, required - adequateLevel);
    }
    
    private FertilizerRecommendation createUreaRecommendation(double nDeficit, String season) {
        // Urea is 46% N
        double ureaNeeded = (nDeficit / 0.46);
        
        return new FertilizerRecommendation(
            "Urea",
            String.format("%.0f kg/acre", ureaNeeded),
            "Split application",
            "50% at sowing, 25% at tillering, 25% at flowering",
            "46-0-0",
            "High nitrogen content essential for leaf growth and chlorophyll production. " +
            "Split application reduces leaching losses and improves nitrogen use efficiency."
        );
    }
    
    private FertilizerRecommendation createDAPRecommendation(double pDeficit, String season) {
        // DAP is 18% N, 46% P2O5
        double dapNeeded = (pDeficit / 0.46);
        
        return new FertilizerRecommendation(
            "DAP (Di-Ammonium Phosphate)",
            String.format("%.0f kg/acre", dapNeeded),
            "Basal application",
            "Full dose at sowing/planting as basal dressing",
            "18-46-0",
            "Phosphorus is essential for root development and energy transfer. " +
            "Basal application ensures availability during critical root establishment phase."
        );
    }
    
    private FertilizerRecommendation createPotashRecommendation(double kDeficit, String season) {
        // MOP (Muriate of Potash) is 60% K2O
        double mopNeeded = (kDeficit / 0.60);
        
        return new FertilizerRecommendation(
            "MOP (Muriate of Potash)",
            String.format("%.0f kg/acre", mopNeeded),
            "Split application",
            "50% at sowing, 50% at 30-40 days after sowing",
            "0-0-60",
            "Potassium improves drought tolerance, disease resistance, and fruit quality. " +
            "Split application matches crop demand pattern and reduces fixation losses."
        );
    }
    
    private FertilizerRecommendation createComplexFertilizerRecommendation(
            int[] npkRequired, String season, Double soilPh) {
        
        String phAdjustment = "";
        if (soilPh != null) {
            if (soilPh < 6.0) {
                phAdjustment = " Consider applying lime to correct soil acidity.";
            } else if (soilPh > 8.0) {
                phAdjustment = " Consider applying gypsum to correct soil alkalinity.";
            }
        }
        
        return new FertilizerRecommendation(
            "NPK Complex (10-26-26)",
            "50 kg/acre",
            "Basal application",
            "Full dose at time of sowing as basal fertilizer",
            "10-26-26",
            "Provides balanced nutrition with all three macro-nutrients. " +
            "Complex fertilizers ensure uniform distribution of nutrients and reduce application labor." +
            phAdjustment
        );
    }
    
    private FertilizerRecommendation createOrganicRecommendation(String crop) {
        return new FertilizerRecommendation(
            "Farmyard Manure (FYM) / Compost",
            "2-3 tons/acre",
            "Before sowing",
            "Apply 2-3 weeks before sowing and incorporate into soil",
            "Variable",
            "Improves soil organic matter, water holding capacity, and microbial activity. " +
            "Provides slow-release nutrients and improves soil structure. " +
            "Especially beneficial in high rainfall areas to prevent nutrient leaching."
        );
    }
}
