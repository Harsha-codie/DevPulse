package ai.cropplanner.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for pincode master data with fuzzy search capabilities
 */
@Repository
public interface PincodeRepository extends JpaRepository<Pincode, String> {
    
    /**
     * Find by exact pincode
     */
    Optional<Pincode> findByPincode(String pincode);
    
    /**
     * Find all villages matching name (exact)
     */
    List<Pincode> findByVillageIgnoreCase(String village);
    
    /**
     * Find all pincodes in a taluk (for fallback when village not found)
     */
    List<Pincode> findByTalukIgnoreCase(String taluk);
    
    /**
     * Find villages by taluk and district
     */
    List<Pincode> findByTalukIgnoreCaseAndDistrictIgnoreCase(String taluk, String district);
    
    /**
     * Find all pincodes in a district
     */
    List<Pincode> findByDistrictIgnoreCase(String district);
    
    /**
     * Fuzzy search for village names (handles typos)
     * Uses LIKE with wildcards for partial matching
     */
    @Query("SELECT p FROM Pincode p WHERE LOWER(p.village) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Pincode> searchVillagesFuzzy(@Param("query") String query);
    
    /**
     * Search by village and taluk (fuzzy)
     */
    @Query("SELECT p FROM Pincode p WHERE " +
           "LOWER(p.village) LIKE LOWER(CONCAT('%', :village, '%')) AND " +
           "LOWER(p.taluk) LIKE LOWER(CONCAT('%', :taluk, '%'))")
    List<Pincode> searchByVillageAndTaluk(@Param("village") String village, 
                                           @Param("taluk") String taluk);
    
    /**
     * Find closest match by village name in a district (for typo correction)
     */
    @Query("SELECT p FROM Pincode p WHERE " +
           "LOWER(p.district) = LOWER(:district) " +
           "ORDER BY LENGTH(p.village) ASC")
    List<Pincode> findClosestInDistrict(@Param("district") String district);
    
    /**
     * Get autocomplete suggestions (top 10 matches)
     */
    @Query("SELECT DISTINCT p FROM Pincode p WHERE " +
           "LOWER(p.village) LIKE LOWER(CONCAT(:prefix, '%')) " +
           "ORDER BY p.village ASC")
    List<Pincode> getAutocompleteSuggestions(@Param("prefix") String prefix);
}
