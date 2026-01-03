package ai.cropplanner.market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV Parser for Market Trend Data
 * 
 * IMPORTANT: This service does NOT auto-load CSV files on application startup.
 * CSV data must be manually uploaded via the REST API endpoint:
 *   POST /api/datasets/market/upload
 * 
 * This design prevents:
 * - Duplicate data insertion on every app restart
 * - Unnecessary database operations during startup
 * - Performance overhead from processing large CSV files repeatedly
 * 
 * Usage:
 * 1. Place your market_trends.csv in the /data folder
 * 2. Use the upload endpoint to process and load the data
 * 3. The endpoint will skip duplicate records automatically
 * 4. Subsequent app restarts will NOT reload the data
 */
@Service
public class MarketTrendCsvLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketTrendCsvLoader.class);
    
    private final MarketTrendRepository repository;
    
    // Column names we care about
    private static final String COL_CMDTY = "cmdty";
    private static final String COL_MARKET_NAME = "market_name";
    private static final String COL_DISTRICT_NAME = "district_name";
    private static final String COL_VARIETY = "variety";
    private static final String COL_P_MIN = "p_min";
    private static final String COL_P_MAX = "p_max";
    private static final String COL_P_MODAL = "p_modal";
    
    public MarketTrendCsvLoader(MarketTrendRepository repository) {
        this.repository = repository;
    }
    
    /**
     * DTO for CSV upload results
     */
    public static class CsvUploadResult {
        private int totalRows;
        private int successfulInserts;
        private int duplicatesSkipped;
        private int invalidRowsSkipped;
        private List<String> errors;
        
        public CsvUploadResult() {
            this.errors = new ArrayList<>();
        }
        
        // Getters and setters
        public int getTotalRows() { return totalRows; }
        public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
        
        public int getSuccessfulInserts() { return successfulInserts; }
        public void setSuccessfulInserts(int successfulInserts) { this.successfulInserts = successfulInserts; }
        
        public int getDuplicatesSkipped() { return duplicatesSkipped; }
        public void setDuplicatesSkipped(int duplicatesSkipped) { this.duplicatesSkipped = duplicatesSkipped; }
        
        public int getInvalidRowsSkipped() { return invalidRowsSkipped; }
        public void setInvalidRowsSkipped(int invalidRowsSkipped) { this.invalidRowsSkipped = invalidRowsSkipped; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public void addError(String error) { this.errors.add(error); }
    }
    
    /**
     * Process CSV file and load into database with duplicate detection
     * This method is called ONLY via the upload REST endpoint, never on startup
     * 
     * @param filePath Absolute path to the CSV file to process
     * @return CsvUploadResult containing statistics and any errors
     */
    public CsvUploadResult processCsvFile(String filePath) throws IOException {
        CsvUploadResult result = new CsvUploadResult();
        
        logger.info("Processing CSV file: {}", filePath);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }
            
            // Parse header to find column indices
            Map<String, Integer> columnIndices = parseHeader(headerLine);
            
            // Validate required columns exist
            validateRequiredColumns(columnIndices);
            
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                result.setTotalRows(result.getTotalRows() + 1);
                
                try {
                    MarketTrend trend = parseLine(line, columnIndices);
                    
                    if (trend == null || !isValid(trend)) {
                        result.setInvalidRowsSkipped(result.getInvalidRowsSkipped() + 1);
                        logger.debug("Skipping invalid line {}", lineNumber);
                        continue;
                    }
                    
                    // Check for duplicates BEFORE inserting
                    if (isDuplicate(trend)) {
                        result.setDuplicatesSkipped(result.getDuplicatesSkipped() + 1);
                        logger.debug("Skipping duplicate record at line {}: {} - {} - {} - {}", 
                            lineNumber, trend.getCmdty(), trend.getDistrictName(), 
                            trend.getMarketName(), trend.getVariety());
                        continue;
                    }
                    
                    // Save new record
                    repository.save(trend);
                    result.setSuccessfulInserts(result.getSuccessfulInserts() + 1);
                    
                } catch (Exception e) {
                    String errorMsg = String.format("Line %d: %s", lineNumber, e.getMessage());
                    result.addError(errorMsg);
                    logger.warn("Error processing line {}: {}", lineNumber, e.getMessage());
                }
            }
        }
        
        logger.info("CSV processing complete. Inserted: {}, Duplicates skipped: {}, Invalid: {}, Errors: {}", 
            result.getSuccessfulInserts(), result.getDuplicatesSkipped(), 
            result.getInvalidRowsSkipped(), result.getErrors().size());
        
        return result;
    }
    
    /**
     * Check if a market trend record already exists in the database
     * Duplicate check is based on: cmdty, district_name, market_name, and variety
     */
    private boolean isDuplicate(MarketTrend trend) {
        return repository.existsByCmdtyAndDistrictNameAndMarketNameAndVariety(
            trend.getCmdty(),
            trend.getDistrictName(),
            trend.getMarketName(),
            trend.getVariety()
        );
    }
    
    /**
     * Parse CSV file and extract only the columns we need (for backward compatibility)
     * @deprecated Use processCsvFile() instead for better duplicate handling
     */
    @Deprecated
    public List<MarketTrend> parseCsvFile(String filePath) throws IOException {
        List<MarketTrend> trends = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }
            
            // Parse header to find column indices
            Map<String, Integer> columnIndices = parseHeader(headerLine);
            
            // Validate required columns exist
            validateRequiredColumns(columnIndices);
            
            String line;
            int lineNumber = 1;
            int skippedLines = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                try {
                    MarketTrend trend = parseLine(line, columnIndices);
                    if (trend != null && isValid(trend)) {
                        trends.add(trend);
                    } else {
                        skippedLines++;
                    }
                } catch (Exception e) {
                    logger.warn("Skipping line {}: {} - Error: {}", lineNumber, line, e.getMessage());
                    skippedLines++;
                }
            }
            
            if (skippedLines > 0) {
                logger.info("Skipped {} invalid lines during CSV parsing", skippedLines);
            }
        }
        
        return trends;
    }
    
    /**
     * Parse CSV header and create column index mapping
     */
    private Map<String, Integer> parseHeader(String headerLine) {
        Map<String, Integer> columnIndices = new HashMap<>();
        String[] headers = headerLine.split(",");
        
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim().toLowerCase();
            columnIndices.put(header, i);
        }
        
        return columnIndices;
    }
    
    /**
     * Validate that all required columns are present
     */
    private void validateRequiredColumns(Map<String, Integer> columnIndices) throws IOException {
        List<String> missingColumns = new ArrayList<>();
        
        if (!columnIndices.containsKey(COL_CMDTY)) missingColumns.add(COL_CMDTY);
        if (!columnIndices.containsKey(COL_MARKET_NAME)) missingColumns.add(COL_MARKET_NAME);
        if (!columnIndices.containsKey(COL_DISTRICT_NAME)) missingColumns.add(COL_DISTRICT_NAME);
        if (!columnIndices.containsKey(COL_VARIETY)) missingColumns.add(COL_VARIETY);
        if (!columnIndices.containsKey(COL_P_MIN)) missingColumns.add(COL_P_MIN);
        if (!columnIndices.containsKey(COL_P_MAX)) missingColumns.add(COL_P_MAX);
        if (!columnIndices.containsKey(COL_P_MODAL)) missingColumns.add(COL_P_MODAL);
        
        if (!missingColumns.isEmpty()) {
            throw new IOException("CSV file is missing required columns: " + String.join(", ", missingColumns));
        }
    }
    
    /**
     * Parse a single CSV line and extract only the columns we need
     */
    private MarketTrend parseLine(String line, Map<String, Integer> columnIndices) {
        String[] values = line.split(",", -1); // -1 to keep trailing empty strings
        
        String cmdty = getValueOrNull(values, columnIndices.get(COL_CMDTY));
        String marketName = getValueOrNull(values, columnIndices.get(COL_MARKET_NAME));
        String districtName = getValueOrNull(values, columnIndices.get(COL_DISTRICT_NAME));
        String variety = getValueOrNull(values, columnIndices.get(COL_VARIETY));
        
        Double pMin = parseDouble(getValueOrNull(values, columnIndices.get(COL_P_MIN)));
        Double pMax = parseDouble(getValueOrNull(values, columnIndices.get(COL_P_MAX)));
        Double pModal = parseDouble(getValueOrNull(values, columnIndices.get(COL_P_MODAL)));
        
        return new MarketTrend(cmdty, marketName, districtName, variety, pMin, pMax, pModal);
    }
    
    /**
     * Safely get value from array by index
     */
    private String getValueOrNull(String[] values, Integer index) {
        if (index == null || index >= values.length) {
            return null;
        }
        String value = values[index].trim();
        return value.isEmpty() ? null : value;
    }
    
    /**
     * Parse double value, return null if invalid
     */
    private Double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Validate that essential fields are present
     */
    private boolean isValid(MarketTrend trend) {
        // At minimum, we need cmdty and district_name for district-level analytics
        return trend.getCmdty() != null && !trend.getCmdty().isEmpty() &&
               trend.getDistrictName() != null && !trend.getDistrictName().isEmpty();
    }
}
