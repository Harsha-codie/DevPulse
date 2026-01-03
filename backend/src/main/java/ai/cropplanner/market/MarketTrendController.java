package ai.cropplanner.market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Market Trend Data
 * 
 * IMPORTANT: CSV data is NOT loaded automatically on startup.
 * Use POST /api/market-trends/upload to manually load CSV data.
 * This prevents duplicate data insertion on every application restart.
 */
@RestController
@RequestMapping("/api/market-trends")
@CrossOrigin(origins = "*")
public class MarketTrendController {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketTrendController.class);
    
    private final MarketTrendService service;
    private final MarketTrendCsvLoader csvLoader;
    
    @Value("${market.csv.path:../data/market_trends.csv}")
    private String defaultCsvPath;
    
    public MarketTrendController(MarketTrendService service, MarketTrendCsvLoader csvLoader) {
        this.service = service;
        this.csvLoader = csvLoader;
    }
    
    /**
     * Upload and process market trends CSV file
     * POST /api/market-trends/upload
     * 
     * This endpoint accepts either:
     * 1. A CSV file upload (multipart/form-data with 'file' parameter)
     * 2. No file - uses the default CSV from /data folder
     * 
     * The endpoint will:
     * - Parse the CSV file
     * - Skip duplicate records (based on cmdty + district + market + variety)
     * - Insert only new records
     * - Return a summary of inserted, skipped, and error records
     * 
     * This is the ONLY way to load CSV data - it does NOT happen automatically on startup.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMarketData(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String csvFilePath;
            
            if (file != null && !file.isEmpty()) {
                // Handle uploaded file
                logger.info("Processing uploaded CSV file: {}", file.getOriginalFilename());
                
                // Validate file type
                String filename = file.getOriginalFilename();
                if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
                    return ResponseEntity.badRequest()
                        .body(createErrorResponse("Only CSV files are supported"));
                }
                
                // Save uploaded file to temp location
                Path tempFile = Files.createTempFile("market_trends_", ".csv");
                file.transferTo(tempFile.toFile());
                csvFilePath = tempFile.toString();
                
                logger.info("Uploaded file saved to: {}", csvFilePath);
                
            } else {
                // Use default CSV file from /data folder
                logger.info("No file uploaded. Using default CSV from: {}", defaultCsvPath);
                
                Path path = Paths.get(defaultCsvPath);
                if (!Files.exists(path)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(
                            "Default CSV file not found at: " + defaultCsvPath + 
                            ". Please upload a CSV file or place market_trends.csv in the /data folder."
                        ));
                }
                
                csvFilePath = path.toString();
            }
            
            // Process the CSV file
            MarketTrendCsvLoader.CsvUploadResult result = csvLoader.processCsvFile(csvFilePath);
            
            // Clean up temp file if it was uploaded
            if (file != null && !file.isEmpty()) {
                try {
                    Files.deleteIfExists(Paths.get(csvFilePath));
                } catch (IOException e) {
                    logger.warn("Failed to delete temp file: {}", e.getMessage());
                }
            }
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "CSV processing completed");
            response.put("statistics", Map.of(
                "totalRows", result.getTotalRows(),
                "successfulInserts", result.getSuccessfulInserts(),
                "duplicatesSkipped", result.getDuplicatesSkipped(),
                "invalidRowsSkipped", result.getInvalidRowsSkipped(),
                "errors", result.getErrors().size()
            ));
            
            if (!result.getErrors().isEmpty()) {
                response.put("errors", result.getErrors());
            }
            
            // Add database statistics
            long totalRecords = service.getTotalRecordCount();
            long distinctDistricts = service.getAllDistricts().size();
            response.put("databaseStatistics", Map.of(
                "totalRecordsInDatabase", totalRecords,
                "distinctDistricts", distinctDistricts
            ));
            
            logger.info("CSV upload completed. Inserted: {}, Duplicates: {}, Invalid: {}", 
                result.getSuccessfulInserts(), result.getDuplicatesSkipped(), result.getInvalidRowsSkipped());
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            logger.error("Failed to process CSV file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to process CSV file: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during CSV upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    /**
     * Get database statistics
     * GET /api/datasets/market/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getDatabaseStats() {
        long totalRecords = service.getTotalRecordCount();
        long distinctDistricts = service.getAllDistricts().size();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", totalRecords);
        stats.put("distinctDistricts", distinctDistricts);
        stats.put("dataLoaded", totalRecords > 0);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Helper method to create error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return error;
    }
    
    /**
     * Get all available districts
     * GET /api/market-trends/districts
     */
    @GetMapping("/districts")
    public ResponseEntity<List<String>> getAllDistricts() {
        List<String> districts = service.getAllDistricts();
        return ResponseEntity.ok(districts);
    }
    
    /**
     * Get district-level market summary
     * GET /api/market-trends/summary?district=BANGALORE
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getDistrictSummary(@RequestParam String district) {
        if (district == null || district.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("District parameter is required");
        }
        
        MarketTrendService.DistrictMarketSummary summary = service.getDistrictSummary(district);
        
        if (summary == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No market data found for district: " + district);
        }
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get all crops available in a district
     * GET /api/market-trends/crops?district=BANGALORE
     */
    @GetMapping("/crops")
    public ResponseEntity<?> getCropsByDistrict(@RequestParam String district) {
        if (district == null || district.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("District parameter is required");
        }
        
        List<String> crops = service.getCropsByDistrict(district);
        
        if (crops.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No crops found for district: " + district);
        }
        
        return ResponseEntity.ok(crops);
    }
    
    /**
     * Get all market trends for a district
     * GET /api/market-trends?district=BANGALORE
     */
    @GetMapping
    public ResponseEntity<?> getMarketTrends(
            @RequestParam String district,
            @RequestParam(required = false) String crop) {
        
        if (district == null || district.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("District parameter is required");
        }
        
        List<MarketTrend> trends;
        
        if (crop != null && !crop.trim().isEmpty()) {
            trends = service.getTrendsByDistrictAndCrop(district, crop);
        } else {
            trends = service.getTrendsByDistrict(district);
        }
        
        if (trends.isEmpty()) {
            String message = crop != null 
                ? String.format("No market data found for crop '%s' in district: %s", crop, district)
                : "No market data found for district: " + district;
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        
        return ResponseEntity.ok(trends);
    }
    
    /**
     * Get crop-specific analytics for a district
     * GET /api/market-trends/analytics?district=BANGALORE&crop=RICE
     */
    @GetMapping("/analytics")
    public ResponseEntity<?> getCropAnalytics(
            @RequestParam String district,
            @RequestParam String crop) {
        
        if (district == null || district.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("District parameter is required");
        }
        
        if (crop == null || crop.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Crop parameter is required");
        }
        
        MarketTrendService.CropMarketAnalytics analytics = service.getCropAnalytics(district, crop);
        
        if (analytics == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("No analytics available for crop '%s' in district: %s", crop, district));
        }
        
        return ResponseEntity.ok(analytics);
    }
}
