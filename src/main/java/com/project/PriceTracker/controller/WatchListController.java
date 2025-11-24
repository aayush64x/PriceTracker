package com.project.PriceTracker.controller;

import com.project.PriceTracker.dto.ProductDTO;
import com.project.PriceTracker.dto.WatchListRequestDTO;
import com.project.PriceTracker.model.WatchListTemporary;
import com.project.PriceTracker.notification.NotificationService;
import com.project.PriceTracker.service.UserTemporaryService;
import com.project.PriceTracker.service.WatchListTemporaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watchlist")
@CrossOrigin(origins = "*")
public class WatchListController {

    private WatchListTemporaryService watchListTemporaryService;
    private UserTemporaryService userTemporaryService;

    private NotificationService notificationService;

    @Autowired
    public WatchListController(WatchListTemporaryService watchListTemporaryService,
                               UserTemporaryService userTemporaryService,
                               NotificationService notificationService) {
        this.watchListTemporaryService = watchListTemporaryService;
        this.userTemporaryService = userTemporaryService;
        this.notificationService = notificationService;
    }

    // ========================================
    // WATCHLIST CRUD OPERATIONS
    // ========================================

    /**
     * Add product to watchlist
     * POST /api/watchlist/add
     * Body: { "email": "user@example.com", "asin": "B08N5WRWNW", "targetPrice": 850 }
     */
    @PostMapping("/add")
    public ResponseEntity<String> addToWatchList(@RequestBody WatchListRequestDTO request) {
        try {
            watchListTemporaryService.addToWatchList(request);
            return ResponseEntity.ok("Added to watchList");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to add to watchlist: " + e.getMessage());
        }
    }

    /**
     * Get all products saved in user's watchlist
     * GET /api/watchlist/saved-products?email=user@example.com
     */
    @GetMapping("/saved-products")
    public ResponseEntity<List<ProductDTO>> getProductSavedToWatchList(@RequestParam String email) {
        try {
            List<ProductDTO> products = watchListTemporaryService.getSavedProducts(email);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Delete item from watchlist
     * DELETE /api/watchlist/{watchListId}
     */
//    @DeleteMapping("/{watchListId}")
//    public ResponseEntity<String> deleteFromWatchList(@PathVariable Integer watchListId) {
//        try {
//            watchListTemporaryService.deleteFromWatchList(watchListId);
//            return ResponseEntity.ok("Removed from watchlist");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body("Watchlist item not found: " + e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to remove from watchlist: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Delete item from watchlist by email and ASIN
//     * DELETE /api/watchlist/remove?email=user@example.com&asin=B08N5WRWNW
//     */
//    @DeleteMapping("/remove")
//    public ResponseEntity<String> deleteFromWatchListByEmailAndAsin(
//            @RequestParam String email,
//            @RequestParam String asin) {
//        try {
//            watchListTemporaryService.deleteFromWatchListByEmailAndAsin(email, asin);
//            return ResponseEntity.ok("Removed from watchlist");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body("Watchlist item not found: " + e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to remove from watchlist: " + e.getMessage());
//        }
//    }

    /**
     * Update target price for a watchlist item
     * PUT /api/watchlist/{watchListId}/target-price
     * Body: { "targetPrice": 800 }
     */
    @PutMapping("/{watchListId}/target-price")
    public ResponseEntity<?> updateTargetPrice(
            @PathVariable Integer watchListId,
            @RequestBody Map<String, Double> request) {
        try {
            Double targetPrice = request.get("targetPrice");
            WatchListTemporary updated = watchListTemporaryService.updateTargetPrice(watchListId, targetPrice);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update target price: " + e.getMessage()));
        }
    }

    // ========================================
    // NOTIFICATION MANAGEMENT
    // ========================================

    /**
     * Get all watchlist entries for a user (with notification details)
     * GET /api/watchlist/user/{email}
     */
    @GetMapping("/user/{email}")
    public ResponseEntity<List<WatchListTemporary>> getUserWatchList(@PathVariable String email) {
        try {
            List<WatchListTemporary> watchList = notificationService.getUserWatchList(email);
            return ResponseEntity.ok(watchList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Enable/disable notifications for a watchlist entry
     * PUT /api/watchlist/{watchListId}/notification/toggle
     * Body: { "enabled": true }
     */
    @PutMapping("/{watchListId}/notification/toggle")
    public ResponseEntity<?> toggleNotification(
            @PathVariable Integer watchListId,
            @RequestBody Map<String, Boolean> request) {
        try {
            boolean enabled = request.get("enabled");
            WatchListTemporary updated = notificationService.toggleNotification(watchListId, enabled);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to toggle notification: " + e.getMessage()));
        }
    }

    /**
     * Reset notification state (allows re-notification)
     * POST /api/watchlist/{watchListId}/notification/reset
     */
    @PostMapping("/{watchListId}/notification/reset")
    public ResponseEntity<?> resetNotification(@PathVariable Integer watchListId) {
        try {
            WatchListTemporary updated = notificationService.resetNotification(watchListId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reset notification: " + e.getMessage()));
        }
    }

    /**
     * Update cooldown period for notifications
     * PUT /api/watchlist/{watchListId}/notification/cooldown
     * Body: { "cooldownHours": 48 }
     */
    @PutMapping("/{watchListId}/notification/cooldown")
    public ResponseEntity<?> updateCooldown(
            @PathVariable Integer watchListId,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer cooldownHours = request.get("cooldownHours");
            WatchListTemporary updated = notificationService.updateCooldown(watchListId, cooldownHours);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update cooldown: " + e.getMessage()));
        }
    }
}