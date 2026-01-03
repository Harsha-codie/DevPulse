package ai.cropplanner.soil;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SoilCacheRepository extends JpaRepository<SoilCache, Long> {
    
    /**
     * Find cached soil data within 0.01 degree radius (~1km)
     * Orders by distance to get the closest match
     */
    @Query(value = """
        SELECT * FROM soil_cache 
        WHERE ABS(latitude - :lat) < 0.01 
        AND ABS(longitude - :lon) < 0.01 
        ORDER BY (ABS(latitude - :lat) + ABS(longitude - :lon)) ASC 
        LIMIT 1
        """, nativeQuery = true)
    Optional<SoilCache> findNearbyCache(@Param("lat") Double latitude, @Param("lon") Double longitude);
}
