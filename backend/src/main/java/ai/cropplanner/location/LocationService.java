package ai.cropplanner.location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Location service with pincode resolution and fuzzy village matching
 * Handles typos using Levenshtein distance algorithm
 */
@Service
public class LocationService {
    
    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);
    
    @Autowired
    private PincodeRepository pincodeRepository;
    
    /**
     * Resolve location from pincode (auto-detection)
     * Returns complete LocationDetails with village, taluk, district, coordinates
     */
    public LocationDetails resolveFromPincode(String pincode) {
        if (pincode == null || pincode.trim().isEmpty()) {
            return null;
        }
        
        String cleanPincode = pincode.trim();
        logger.info("Resolving location from pincode: {}", cleanPincode);
        
        Optional<Pincode> pincodeOpt = pincodeRepository.findByPincode(cleanPincode);
        
        if (pincodeOpt.isPresent()) {
            Pincode pincodeData = pincodeOpt.get();
            LocationDetails details = LocationDetails.fromPincode(pincodeData);
            logger.info("Pincode {} resolved to: {}", cleanPincode, details.getFullAddress());
            return details;
        } else {
            logger.warn("Pincode {} not found in database", cleanPincode);
            return null;
        }
    }
    
    /**
     * Resolve location from village name with FUZZY MATCHING
     * Handles typos by finding closest matches
     */
    public LocationDetails resolveFromVillage(String village, String taluk, String district) {
        if (village == null || village.trim().isEmpty()) {
            return null;
        }
        
        String cleanVillage = village.trim();
        String cleanTaluk = taluk != null ? taluk.trim() : null;
        String cleanDistrict = district != null ? district.trim() : null;
        
        logger.info("Resolving location from village: {}, taluk: {}, district: {}", 
                    cleanVillage, cleanTaluk, cleanDistrict);
        
        // STRATEGY 1: Try exact village match first
        List<Pincode> exactMatches = pincodeRepository.findByVillageIgnoreCase(cleanVillage);
        
        // Filter by taluk/district if provided
        if (!exactMatches.isEmpty()) {
            Pincode match = filterByTalukDistrict(exactMatches, cleanTaluk, cleanDistrict);
            if (match != null) {
                LocationDetails details = LocationDetails.fromPincode(match);
                details.setSource("MANUAL");
                details.setMessage("Exact village match found");
                return details;
            }
        }
        
        // STRATEGY 2: Try fuzzy search for village typos
        List<Pincode> fuzzyMatches = pincodeRepository.searchVillagesFuzzy(cleanVillage);
        
        if (!fuzzyMatches.isEmpty()) {
            // Calculate Levenshtein distance to find closest match
            Pincode bestMatch = findBestMatch(cleanVillage, fuzzyMatches, cleanTaluk, cleanDistrict);
            
            if (bestMatch != null) {
                LocationDetails details = LocationDetails.fromPincode(bestMatch);
                details.setSource("FUZZY_MATCH");
                
                if (!bestMatch.getVillage().equalsIgnoreCase(cleanVillage)) {
                    details.setMessage("Did you mean: " + bestMatch.getVillage() + "?");
                } else {
                    details.setMessage("Match found");
                }
                
                logger.info("Fuzzy match: '{}' → '{}'", cleanVillage, bestMatch.getVillage());
                return details;
            }
        }
        
        // STRATEGY 3: Village not found - Try TALUK as fallback (NEW!)
        if (cleanTaluk != null && !cleanTaluk.isEmpty()) {
            logger.info("Village '{}' not found, searching by taluk: {}", cleanVillage, cleanTaluk);
            List<Pincode> talukMatches = pincodeRepository.findByTalukIgnoreCase(cleanTaluk);
            
            if (!talukMatches.isEmpty()) {
                // Filter by district if provided
                if (cleanDistrict != null && !cleanDistrict.isEmpty()) {
                    talukMatches = talukMatches.stream()
                        .filter(p -> p.getDistrict().equalsIgnoreCase(cleanDistrict))
                        .collect(Collectors.toList());
                }
                
                if (!talukMatches.isEmpty()) {
                    Pincode talukCenter = talukMatches.get(0); // Use first entry as taluk center
                    LocationDetails details = LocationDetails.fromPincode(talukCenter);
                    details.setVillage(cleanVillage); // Keep user's village input
                    details.setSource("TALUK_FALLBACK");
                    details.setMessage("Village not found - using taluk coordinates: " + cleanTaluk);
                    logger.info("Using taluk '{}' coordinates for village '{}'", cleanTaluk, cleanVillage);
                    return details;
                }
            }
        }
        
        // STRATEGY 4: If district is provided but no village/taluk match, use district coordinates
        if (cleanDistrict != null && !cleanDistrict.isEmpty()) {
            List<Pincode> districtMatches = pincodeRepository.findByDistrictIgnoreCase(cleanDistrict);
            if (!districtMatches.isEmpty()) {
                Pincode districtCenter = districtMatches.get(0); // Use first pincode as center
                LocationDetails details = new LocationDetails();
                details.setVillage(cleanVillage); // Keep user input
                details.setTaluk(cleanTaluk);
                details.setDistrict(cleanDistrict);
                details.setLatitude(districtCenter.getLatitude());
                details.setLongitude(districtCenter.getLongitude());
                details.setSource("DISTRICT_FALLBACK");
                details.setMessage("Using district-level coordinates (village not found)");
                
                logger.warn("Village '{}' not found, using district '{}' coordinates", 
                           cleanVillage, cleanDistrict);
                return details;
            }
        }
        
        logger.warn("No match found for village: {}", cleanVillage);
        return null;
    }
    
    /**
     * Get autocomplete suggestions for village input
     */
    public List<String> getVillageSuggestions(String prefix) {
        if (prefix == null || prefix.length() < 2) {
            return Collections.emptyList();
        }
        
        List<Pincode> suggestions = pincodeRepository.getAutocompleteSuggestions(prefix);
        
        return suggestions.stream()
            .limit(10) // Top 10 suggestions
            .map(p -> p.getVillage() + ", " + p.getTaluk() + ", " + p.getDistrict())
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * Validate pincode format (6 digits for India)
     */
    public boolean isValidPincode(String pincode) {
        return pincode != null && pincode.matches("^[0-9]{6}$");
    }
    
    /**
     * Filter pincode list by taluk and district
     */
    private Pincode filterByTalukDistrict(List<Pincode> pincodes, String taluk, String district) {
        // Prefer matches with taluk AND district
        if (taluk != null && district != null) {
            Optional<Pincode> match = pincodes.stream()
                .filter(p -> p.getTaluk().equalsIgnoreCase(taluk) && 
                            p.getDistrict().equalsIgnoreCase(district))
                .findFirst();
            if (match.isPresent()) return match.get();
        }
        
        // Then try district only
        if (district != null) {
            Optional<Pincode> match = pincodes.stream()
                .filter(p -> p.getDistrict().equalsIgnoreCase(district))
                .findFirst();
            if (match.isPresent()) return match.get();
        }
        
        // Finally, return first match
        return pincodes.isEmpty() ? null : pincodes.get(0);
    }
    
    /**
     * Find best match using Levenshtein distance (typo tolerance)
     */
    private Pincode findBestMatch(String input, List<Pincode> candidates, 
                                  String taluk, String district) {
        if (candidates.isEmpty()) return null;
        
        // Calculate distance for each candidate
        Map<Pincode, Integer> distances = new HashMap<>();
        for (Pincode candidate : candidates) {
            int distance = levenshteinDistance(
                input.toLowerCase(), 
                candidate.getVillage().toLowerCase()
            );
            
            // Bonus points for matching taluk/district
            if (taluk != null && candidate.getTaluk().equalsIgnoreCase(taluk)) {
                distance -= 2;
            }
            if (district != null && candidate.getDistrict().equalsIgnoreCase(district)) {
                distance -= 3;
            }
            
            distances.put(candidate, distance);
        }
        
        // Return candidate with minimum distance
        return distances.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Levenshtein distance algorithm for typo detection
     * Returns number of single-character edits needed to transform s1 into s2
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
}
