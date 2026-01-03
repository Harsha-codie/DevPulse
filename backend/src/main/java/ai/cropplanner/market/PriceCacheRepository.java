package ai.cropplanner.market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceCacheRepository extends JpaRepository<PriceCache, Long> {
    
    /**
     * Find cached price for a specific crop and district (not expired)
     */
    @Query("SELECT p FROM PriceCache p WHERE UPPER(p.crop) = UPPER(:crop) " +
           "AND UPPER(p.district) = UPPER(:district) " +
           "AND p.fetchedAt > :expiryDate " +
           "ORDER BY p.fetchedAt DESC")
    Optional<PriceCache> findValidPrice(
        @Param("crop") String crop, 
        @Param("district") String district,
        @Param("expiryDate") LocalDateTime expiryDate
    );
    
    /**
     * Find all cached prices for a district
     */
    @Query("SELECT p FROM PriceCache p WHERE UPPER(p.district) = UPPER(:district) " +
           "AND p.fetchedAt > :expiryDate")
    List<PriceCache> findValidPricesForDistrict(
        @Param("district") String district,
        @Param("expiryDate") LocalDateTime expiryDate
    );
    
    /**
     * Delete expired cache entries (older than 7 days)
     */
    @Query("DELETE FROM PriceCache p WHERE p.fetchedAt < :expiryDate")
    void deleteExpiredPrices(@Param("expiryDate") LocalDateTime expiryDate);
}
