package com.project.PriceTracker.repository;

import com.project.PriceTracker.model.Product;
import com.project.PriceTracker.model.Users;
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
    List<WatchListTemporary> findByUsers(Users user);

    /**
     * Find all watchlist entries for a user by email
     */
    List<WatchListTemporary> findByUsersEmail(String email);

    /**
     * Find watchlist entry by product and user (for unique constraint check)
     */
    Optional<WatchListTemporary> findByProductAndUsers(Product product, Users user);

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
    void deleteByUsers(Users user);

    /**
     * Delete all watchlist entries for a user by email
     */
    void deleteByUsersEmail(String email);

    /**
     * Delete all watchlist entries for a product
     */
    void deleteByProduct(Product product);

    /**
     * Delete specific watchlist entry by product and user
     */
    void deleteByProductAndUsers(Product product, Users user);

    /**
     * Delete specific watchlist entry by ASIN and user email
     */
    void deleteByProductASINAndUsersEmail(String asin, String email);

    // ========================================
    // COUNT/EXISTS METHODS
    // ========================================

    /**
     * Count watchlist items for a user
     */
    long countByUsers(Users user);

    /**
     * Count watchlist items for a user by email
     */
    long countByUsersEmail(String email);

    /**
     * Check if product is in user's watchlist
     */
    boolean existsByProductAndUsers(Product product, Users user);

    /**
     * Check if product is in user's watchlist by email and ASIN
     */
    boolean existsByProductASINAndUsersEmail(String asin, String email);

    /**
     * Count total watchlist entries for a product
     */
    long countByProduct(Product product);
}