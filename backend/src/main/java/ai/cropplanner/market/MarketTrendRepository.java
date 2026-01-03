package ai.cropplanner.market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketTrendRepository extends JpaRepository<MarketTrend, Long> {
    
    /**
     * Find all market trends for a specific district (case-insensitive)
     */
    List<MarketTrend> findByDistrictNameIgnoreCase(String districtName);
    
    /**
     * Find market trends by district and crop name
     */
    List<MarketTrend> findByDistrictNameIgnoreCaseAndCmdtyIgnoreCase(String districtName, String cmdty);
    
    /**
     * Get all distinct district names
     */
    @Query("SELECT DISTINCT m.districtName FROM MarketTrend m ORDER BY m.districtName")
    List<String> findAllDistinctDistricts();
    
    /**
     * Get all distinct crops for a district
     */
    @Query("SELECT DISTINCT m.cmdty FROM MarketTrend m WHERE LOWER(m.districtName) = LOWER(?1) ORDER BY m.cmdty")
    List<String> findDistinctCropsByDistrict(String districtName);
    
    /**
     * Check if a record already exists with the same combination of fields
     * Used to prevent duplicate entries during CSV upload
     */
    boolean existsByCmdtyAndDistrictNameAndMarketNameAndVariety(
        String cmdty, String districtName, String marketName, String variety
    );
}
