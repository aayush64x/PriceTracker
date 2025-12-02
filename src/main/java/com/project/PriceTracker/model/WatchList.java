package com.project.PriceTracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"asin", "userTemporaryId"})
)
public class WatchList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int watchListId;

    @ManyToOne
    @JoinColumn(name="productId")
    private Product product;

    @ManyToOne
    @JoinColumn(name="userId")
    private Users users;

    @Column(name = "date_added", columnDefinition = "TIMESTAMP")
    @CreationTimestamp
    private Timestamp dateAdded;

    private Double targetPrice;

    // ========================================
    // NEW NOTIFICATION FIELDS
    // ========================================

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled = true;

    @Column(name = "last_notified_at")
    private LocalDateTime lastNotifiedAt;

    @Column(name = "notification_sent")
    private Boolean notificationSent = false;

    @Column(name = "lowest_notified_price")
    private Double lowestNotifiedPrice;

    @Column(name = "cooldown_hours")
    private Integer cooldownHours = 24;

    // ========================================
    // HELPER METHODS FOR NOTIFICATIONS
    // ========================================

    /**
     * Check if notification is in cooldown period
     */
    public boolean isInCooldown() {
        if (lastNotifiedAt == null) {
            return false;
        }
        LocalDateTime cooldownEnd = lastNotifiedAt.plusHours(cooldownHours);
        return LocalDateTime.now().isBefore(cooldownEnd);
    }

    /**
     * Check if should send notification
     */
    public boolean shouldNotify(Double currentPrice) {
        // Check if notifications are enabled
        if (!Boolean.TRUE.equals(notificationEnabled)) {
            return false;
        }

        // Check if price and target are valid
        if (currentPrice == null || targetPrice == null) {
            return false;
        }

        // Check if price meets target
        if (currentPrice > targetPrice) {
            return false;
        }

        // Check if in cooldown period
        if (isInCooldown()) {
            return false;
        }

        // Check if price is lower than previous notification
        if (lowestNotifiedPrice != null && currentPrice >= lowestNotifiedPrice) {
            return false;
        }

        return true;
    }

    /**
     * Mark notification as sent
     */
    public void markAsNotified(Double price) {
        this.notificationSent = true;
        this.lastNotifiedAt = LocalDateTime.now();
        this.lowestNotifiedPrice = price;
    }

    /**
     * Reset notification state (allows re-notification)
     */
    public void resetNotification() {
        this.notificationSent = false;
        this.lastNotifiedAt = null;
        this.lowestNotifiedPrice = null;
    }
}