package com.project.PriceTracker.repository;

import com.project.PriceTracker.model.Product;
import com.project.PriceTracker.model.UserTemporary;
import com.project.PriceTracker.model.WatchListTemporary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchListTemporaryRepository extends JpaRepository<WatchListTemporary, Integer> {

    // ========================================
    // FIND METHODS
    // ========================================

    /**
     * Find all watchlist entries for a product
     */
    List<WatchListTemporary> findByProduct(Product product);

    /**
     * Find all watchlist entries for a user
     */
    List<WatchListTemporary> findByUserTemporary(UserTemporary user);

    /**
     * Find watchlist entry by product and user (for unique constraint check)
     */
    Optional<WatchListTemporary> findByProductAndUserTemporary(Product product, UserTemporary user);

    /**
     * Find all watchlist entries for a user by email
     */
    List<WatchListTemporary> findByUserTemporary_Email(String email);

    /**
     * Find enabled watchlist entries for a product
     */
    List<WatchListTemporary> findByProductAndNotificationEnabledTrue(Product product);

    /**
     * Find all enabled watchlist entries
     */
    List<WatchListTemporary> findByNotificationEnabledTrue();

    // ========================================
    // DELETE METHODS
    // ========================================

    /**
     * Delete all watchlist entries for a user
     */
    void deleteByUserTemporary(UserTemporary user);

    /**
     * Delete all watchlist entries for a product
     */
    void deleteByProduct(Product product);

    /**
     * Delete specific watchlist entry by product and user
     */
    void deleteByProductAndUserTemporary(Product product, UserTemporary user);

    // ========================================
    // COUNT/EXISTS METHODS
    // ========================================

    /**
     * Count watchlist items for a user
     */
    long countByUserTemporary(UserTemporary user);

    /**
     * Check if product is in user's watchlist
     */
    boolean existsByProductAndUserTemporary(Product product, UserTemporary user);

    /**
     * Count total watchlist entries for a product
     */
    long countByProduct(Product product);

    List<WatchListTemporary> findByUserTemporaryEmail(String email);
}