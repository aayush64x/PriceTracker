package com.project.PriceTracker.notification;

import com.project.PriceTracker.model.Product;
import com.project.PriceTracker.model.WatchList;
import com.project.PriceTracker.repository.WatchListTemporaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private WatchListTemporaryRepository watchListRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Check and send notifications for a product after price update
     * Called by PriceUpdater after scraping
     */
    public void checkAndNotify(Product product, Double newPrice) {
        // Get all watchlist entries for this product
        List<WatchList> watchListEntries = watchListRepository.findByProduct(product);

        if (watchListEntries.isEmpty()) {
            System.out.println("No users watching product: " + product.getProductName());
            return;
        }

        System.out.println("Checking " + watchListEntries.size() +
                " watcher(s) for: " + product.getProductName());

        int notificationsSent = 0;
        for (WatchList watchList : watchListEntries) {
            if (watchList.shouldNotify(newPrice)) {
                sendNotification(watchList, product, newPrice);
                notificationsSent++;
            }
        }

        System.out.println("✅ Sent " + notificationsSent + " notification(s)");
    }

    /**
     * Send notification email and update watchlist record
     */
    private void sendNotification(WatchList watchList, Product product, Double currentPrice) {
        try {
            // Get user email
            String userEmail = watchList.getUsers().getEmail();

            // Send email
            emailService.sendPriceAlert(
                    userEmail,
                    product.getProductName(),
                    currentPrice,
                    watchList.getTargetPrice(),
                    product.getLink()
            );

            // Mark as notified
            watchList.markAsNotified(currentPrice);
            watchListRepository.save(watchList);

            System.out.println("✅ Notification sent to: " + userEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Enable/disable notification for a watchlist entry
     */
    public WatchList toggleNotification(Integer watchListId, boolean enabled) {
        WatchList watchList = watchListRepository.findById(watchListId)
                .orElseThrow(() -> new IllegalArgumentException("Watchlist entry not found"));

        watchList.setNotificationEnabled(enabled);
        return watchListRepository.save(watchList);
    }

    /**
     * Reset notification state (allows re-notification)
     */
    public WatchList resetNotification(Integer watchListId) {
        WatchList watchList = watchListRepository.findById(watchListId)
                .orElseThrow(() -> new IllegalArgumentException("Watchlist entry not found"));

        watchList.resetNotification();
        return watchListRepository.save(watchList);
    }

    /**
     * Update cooldown period for a watchlist entry
     */
    public WatchList updateCooldown(Integer watchListId, Integer cooldownHours) {
        WatchList watchList = watchListRepository.findById(watchListId)
                .orElseThrow(() -> new IllegalArgumentException("Watchlist entry not found"));

        watchList.setCooldownHours(cooldownHours);
        return watchListRepository.save(watchList);
    }

    /**
     * Get all watchlist entries for a user
     */
    public List<WatchList> getUserWatchList(String email) {
        return watchListRepository.findByUsersEmail(email);
    }
}