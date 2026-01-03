package ai.cropplanner.crop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for district-level crop suitability constraints
 */
@Repository
public interface DistrictCropSuitabilityRepository extends JpaRepository<DistrictCropSuitability, Long> {
    
    /**
     * Find crop constraints for a district and season
     */
    Optional<DistrictCropSuitability> findByDistrictIgnoreCaseAndSeasonIgnoreCase(
        String district, String season
    );
    
    /**
     * Find crop constraints for a district (any season or ALL)
     */
    @Query("SELECT d FROM DistrictCropSuitability d WHERE " +
           "LOWER(d.district) = LOWER(:district) AND " +
           "(LOWER(d.season) = LOWER(:season) OR LOWER(d.season) = 'all')")
    List<DistrictCropSuitability> findByDistrictAndSeason(
        @Param("district") String district, 
        @Param("season") String season
    );
    
    /**
     * Find all constraints for a district
     */
    List<DistrictCropSuitability> findByDistrictIgnoreCase(String district);
}
