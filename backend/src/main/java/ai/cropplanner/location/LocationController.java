package ai.cropplanner.location;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for location resolution and autocomplete
 */
@RestController
@RequestMapping("/api/location")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class LocationController {
    
    @Autowired
    private LocationService locationService;
    
    /**
     * Resolve location from pincode
     * GET /api/location/pincode/{pincode}
     */
    @GetMapping("/pincode/{pincode}")
    public ResponseEntity<Map<String, Object>> resolveFromPincode(@PathVariable String pincode) {
        Map<String, Object> response = new HashMap<>();
        
        // Validate pincode format
        if (!locationService.isValidPincode(pincode)) {
            response.put("success", false);
            response.put("error", "Invalid pincode format. Must be 6 digits.");
            return ResponseEntity.badRequest().body(response);
        }
        
        LocationDetails location = locationService.resolveFromPincode(pincode);
        
        if (location != null) {
            response.put("success", true);
            response.put("pincode", pincode);
            response.put("village", location.getVillage());
            response.put("taluk", location.getTaluk());
            response.put("district", location.getDistrict());
            response.put("latitude", location.getLatitude());
            response.put("longitude", location.getLongitude());
            response.put("fullAddress", location.getFullAddress());
            response.put("message", location.getMessage());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("error", "Pincode not found in Karnataka database");
            return ResponseEntity.status(404).body(response);
        }
    }
    
    /**
     * Get autocomplete suggestions for village names
     * GET /api/location/suggestions?q=Mal
     */
    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getVillageSuggestions(
            @RequestParam(name = "q", required = false) String query) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (query == null || query.length() < 2) {
            response.put("success", false);
            response.put("error", "Query must be at least 2 characters");
            return ResponseEntity.badRequest().body(response);
        }
        
        List<String> suggestions = locationService.getVillageSuggestions(query);
        
        response.put("success", true);
        response.put("query", query);
        response.put("suggestions", suggestions);
        response.put("count", suggestions.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Resolve location from village name with fuzzy matching
     * POST /api/location/village
     * Body: { "village": "Malur", "taluk": "Kolar", "district": "Kolar" }
     */
    @PostMapping("/village")
    public ResponseEntity<Map<String, Object>> resolveFromVillage(
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        String village = request.get("village");
        String taluk = request.get("taluk");
        String district = request.get("district");
        
        if (village == null || village.trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "Village name is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        LocationDetails location = locationService.resolveFromVillage(village, taluk, district);
        
        if (location != null) {
            response.put("success", true);
            response.put("village", location.getVillage());
            response.put("taluk", location.getTaluk());
            response.put("district", location.getDistrict());
            response.put("pincode", location.getPincode());
            response.put("latitude", location.getLatitude());
            response.put("longitude", location.getLongitude());
            response.put("fullAddress", location.getFullAddress());
            response.put("source", location.getSource());
            response.put("message", location.getMessage());
            
            // Flag if it's a fuzzy match (typo correction)
            if ("FUZZY_MATCH".equals(location.getSource())) {
                response.put("corrected", true);
                response.put("originalInput", village);
            }
            
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("error", "Village not found. Please check spelling or try with district name.");
            return ResponseEntity.status(404).body(response);
        }
    }
    
    /**
     * Validate pincode format
     * GET /api/location/validate-pincode/{pincode}
     */
    @GetMapping("/validate-pincode/{pincode}")
    public ResponseEntity<Map<String, Object>> validatePincode(@PathVariable String pincode) {
        Map<String, Object> response = new HashMap<>();
        
        boolean isValid = locationService.isValidPincode(pincode);
        
        response.put("valid", isValid);
        response.put("pincode", pincode);
        
        if (!isValid) {
            response.put("error", "Pincode must be exactly 6 digits");
        }
        
        return ResponseEntity.ok(response);
    }
}
