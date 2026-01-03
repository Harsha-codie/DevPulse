package ai.cropplanner.crop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
@Order(2) // Load after pincodes
public class DistrictCropSuitabilityLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DistrictCropSuitabilityLoader.class);
    
    private final DistrictCropSuitabilityRepository repository;

    public DistrictCropSuitabilityLoader(DistrictCropSuitabilityRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if data already loaded
        long count = repository.count();
        if (count > 0) {
            logger.info("District crop suitability data already loaded. Total records: {}", count);
            return;
        }

        logger.info("Loading district crop suitability data from CSV...");
        
        try {
            ClassPathResource resource = new ClassPathResource("data/district_crop_suitability.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            
            String line;
            boolean isFirstLine = true;
            int recordCount = 0;
            
            while ((line = reader.readLine()) != null) {
                // Skip header
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // New CSV format: district,crop_1,season_1,score_1,crop_2,season_2,score_2,crop_3,season_3,score_3
                String[] parts = line.split(",");
                if (parts.length < 10) {
                    logger.warn("Skipping invalid line (expected 10 columns): {}", line);
                    continue;
                }
                
                String district = parts[0].trim();
                
                // Parse all 3 crops with their seasons
                String[] crops = new String[3];
                String[] seasons = new String[3];
                
                crops[0] = parts[1].trim();
                seasons[0] = parts[2].trim();
                // parts[3] is score_1, not needed
                
                crops[1] = parts[4].trim();
                seasons[1] = parts[5].trim();
                // parts[6] is score_2, not needed
                
                crops[2] = parts[7].trim();
                seasons[2] = parts[8].trim();
                // parts[9] is score_3, not needed
                
                // Create entries for each season type (Kharif, Rabi, All seasons)
                // Group crops by season to avoid duplicates
                java.util.Map<String, java.util.List<String>> seasonCropMap = new java.util.HashMap<>();
                
                for (int i = 0; i < 3; i++) {
                    String crop = crops[i];
                    String season = seasons[i];
                    
                    // Handle multi-season crops (e.g., "Kharif/Rabi")
                    String[] seasonParts = season.split("/");
                    for (String s : seasonParts) {
                        String normalizedSeason = s.trim();
                        seasonCropMap.computeIfAbsent(normalizedSeason, k -> new java.util.ArrayList<>()).add(crop);
                    }
                }
                
                // Create database entries for each season
                for (java.util.Map.Entry<String, java.util.List<String>> entry : seasonCropMap.entrySet()) {
                    DistrictCropSuitability suitability = new DistrictCropSuitability();
                    suitability.setDistrict(district);
                    suitability.setAgroClimaticZone(""); // Not in new CSV
                    suitability.setSuitableCrops(String.join(",", entry.getValue()));
                    suitability.setAvoidCrops(""); // Not needed - strict constraint on allowed list
                    suitability.setSeason(entry.getKey());
                    suitability.setRemarks(""); // Not in new CSV
                    
                    repository.save(suitability);
                    recordCount++;
                }
            }
            
            reader.close();
            logger.info("Successfully loaded {} district crop suitability records", recordCount);
            
        } catch (Exception e) {
            logger.error("Error loading district crop suitability data: {}", e.getMessage(), e);
            throw e;
        }
    }
}
