package ai.cropplanner.location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads Karnataka pincode data from CSV on application startup
 * CSV Format: stateCode,stateNameEnglish,districtCode,districtNameEnglish,subdistrictNameEnglish,villageCode,villageNameEnglish,pincode
 * We use: districtNameEnglish, subdistrictNameEnglish (taluk), villageNameEnglish, pincode
 */
@Component
public class PincodeDataLoader implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(PincodeDataLoader.class);
    
    @Autowired
    private PincodeRepository pincodeRepository;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Check if data already loaded
        long count = pincodeRepository.count();
        if (count > 0) {
            logger.info("Pincode database already loaded with {} entries", count);
            return;
        }
        
        logger.info("Loading Karnataka pincode data from CSV...");
        
        Resource resource = null;
        
        // Try external file first (data/pincodes_unique.csv)
        try {
            resource = new FileSystemResource("data/pincodes_unique.csv");
            if (!resource.exists()) {
                // Fallback to classpath
                resource = new ClassPathResource("data/pincodes_unique.csv");
            }
        } catch (Exception e) {
            logger.warn("Could not load from external file, trying classpath");
            resource = new ClassPathResource("data/pincodes_unique.csv");
        }
        
        if (!resource.exists()) {
            logger.error("Pincode CSV file not found! Skipping pincode data loading.");
            return;
        }
        
        int loadedCount = 0;
        int skippedCount = 0;
        int lineNumber = 0;
        List<Pincode> batchPincodes = new ArrayList<>();
        final int BATCH_SIZE = 500;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            // Skip header line
            reader.readLine();
            lineNumber++;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    String[] parts = line.split(",");
                    
                    // CSV format: stateCode,stateNameEnglish,districtCode,districtNameEnglish,subdistrictNameEnglish,villageCode,villageNameEnglish,pincode
                    if (parts.length < 8) {
                        logger.warn("Line {}: Invalid CSV format - expected 8 columns", lineNumber);
                        skippedCount++;
                        continue;
                    }
                    
                    String state = parts[1].trim();
                    String district = parts[3].trim();
                    String taluk = parts[4].trim();  // subdistrictNameEnglish
                    String village = parts[6].trim();  // villageNameEnglish
                    String pincode = parts[7].trim();
                    
                    // Only process Karnataka entries
                    if (!"Karnataka".equalsIgnoreCase(state)) {
                        skippedCount++;
                        continue;
                    }
                    
                    // Validate pincode format (6 digits)
                    if (!pincode.matches("\\d{6}")) {
                        skippedCount++;
                        continue;
                    }
                    
                    Pincode pincodeEntity = new Pincode(
                        pincode, village, taluk, district, null, null  // No coordinates in this CSV
                    );
                    
                    batchPincodes.add(pincodeEntity);
                    loadedCount++;
                    
                    // Save in batches
                    if (batchPincodes.size() >= BATCH_SIZE) {
                        pincodeRepository.saveAll(batchPincodes);
                        logger.info("Saved batch of {} pincodes (total: {})", batchPincodes.size(), loadedCount);
                        batchPincodes.clear();
                    }
                    
                } catch (Exception e) {
                    logger.warn("Line {}: Error parsing - {} ({})", lineNumber, line, e.getMessage());
                    skippedCount++;
                }
            }
            
            // Save remaining pincodes
            if (!batchPincodes.isEmpty()) {
                pincodeRepository.saveAll(batchPincodes);
                logger.info("Saved final batch of {} pincodes", batchPincodes.size());
            }
            
            logger.info("✅ Successfully loaded {} Karnataka pincode records", loadedCount);
            if (skippedCount > 0) {
                logger.info("⚠️ Skipped {} non-Karnataka/invalid records", skippedCount);
            }
            logger.info("Pincode database ready for village-level recommendations!");
            
        } catch (Exception e) {
            logger.error("Failed to load pincode data: {}", e.getMessage(), e);
        }
    }
}
