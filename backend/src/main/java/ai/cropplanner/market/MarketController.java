package ai.cropplanner.market;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for market prices
 */
@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*")
public class MarketController {
    
    private final AgmarknetClient agmarknetClient;
    
    public MarketController(AgmarknetClient agmarknetClient) {
        this.agmarknetClient = agmarknetClient;
    }
    
    /**
     * Get live price for a crop in a district
     * GET /api/market/live-price?crop=RICE&district=BANGALORE
     */
    @GetMapping("/live-price")
    public ResponseEntity<LivePriceResponse> getLivePrice(
            @RequestParam String crop,
            @RequestParam String district) {
        
        LivePriceResponse response = agmarknetClient.getLivePrice(crop, district);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get supported crops for live pricing
     * GET /api/market/supported-crops
     */
    @GetMapping("/supported-crops")
    public ResponseEntity<?> getSupportedCrops() {
        return ResponseEntity.ok(
            java.util.Map.of(
                "success", true,
                "crops", agmarknetClient.getSupportedCrops(),
                "count", agmarknetClient.getSupportedCrops().size()
            )
        );
    }
}
