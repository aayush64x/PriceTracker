package com.project.PriceTracker.controller;

import com.project.PriceTracker.model.PriceHistory;
import com.project.PriceTracker.model.Product;
import com.project.PriceTracker.model.WatchListTemporary;
import com.project.PriceTracker.notification.NotificationService;
import com.project.PriceTracker.repository.PriceHistoryRepository;
import com.project.PriceTracker.repository.ProductRepository;
import com.project.PriceTracker.repository.WatchListTemporaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WatchListTemporaryRepository watchListRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Quick test - automatically sets price to trigger notification
     * POST /api/test/quick-test/{productId}
     */
    @PostMapping("/quick-test/{productId}")
    public ResponseEntity<Map<String, Object>> quickTest(@PathVariable Integer productId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

            response.put("productName", product.getProductName());
            response.put("currentPrice", product.getProductPrice());

            // Get watchlist entries
            List<WatchListTemporary> watchers = watchListRepository.findByProduct(product);
            response.put("watchersCount", watchers.size());

            if (watchers.isEmpty()) {
                response.put("status", "ERROR");
                response.put("message", "No users watching this product. Add to watchlist first!");
                return ResponseEntity.ok(response);
            }

            // Get first watcher's target price
            WatchListTemporary firstWatcher = watchers.get(0);
            Double targetPrice = firstWatcher.getTargetPrice();
            response.put("targetPrice", targetPrice);
            response.put("watcherEmail", firstWatcher.getUsers().getEmail());

            // Set price to 80% of target (guaranteed to trigger)
            Double testPrice = targetPrice * 0.8;
            Double oldPrice = product.getProductPrice();

            // Update product price
            product.setProductPrice(testPrice);
            productRepository.save(product);

            // Add to price history
            PriceHistory history = new PriceHistory();
            history.setProduct(product);
            history.setProductPrice(testPrice);
            priceHistoryRepository.save(history);

            response.put("oldPrice", oldPrice);
            response.put("newPrice", testPrice);
            response.put("priceChange", oldPrice - testPrice);

            // Trigger notifications
            System.out.println("\n========================================");
            System.out.println("🧪 TESTING NOTIFICATIONS");
            System.out.println("========================================");
            notificationService.checkAndNotify(product, testPrice);
            System.out.println("========================================\n");

            response.put("status", "SUCCESS");
            response.put("message", "Price set to $" + testPrice + ". Check your email and console logs!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Set specific price for a product
     * POST /api/test/set-price
     * Body: { "productId": 1, "newPrice": 50.0 }
     */
    @PostMapping("/set-price")
    public ResponseEntity<Map<String, Object>> setPrice(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer productId = Integer.valueOf(request.get("productId").toString());
            Double newPrice = Double.valueOf(request.get("newPrice").toString());

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            Double oldPrice = product.getProductPrice();

            // Update product price
            product.setProductPrice(newPrice);
            productRepository.save(product);

            // Add to price history
            PriceHistory history = new PriceHistory();
            history.setProduct(product);
            history.setProductPrice(newPrice);
            priceHistoryRepository.save(history);

            // Trigger notifications
            notificationService.checkAndNotify(product, newPrice);

            response.put("status", "SUCCESS");
            response.put("productName", product.getProductName());
            response.put("oldPrice", oldPrice);
            response.put("newPrice", newPrice);
            response.put("message", "Price updated and notifications checked!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Simulate price drop (20% decrease)
     * POST /api/test/simulate-drop/{productId}
     */
    @PostMapping("/simulate-drop/{productId}")
    public ResponseEntity<Map<String, Object>> simulateDrop(@PathVariable Integer productId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            Double currentPrice = product.getProductPrice();
            Double dropPrice = currentPrice * 0.8; // 20% drop

            product.setProductPrice(dropPrice);
            productRepository.save(product);

            // Add to price history
            PriceHistory history = new PriceHistory();
            history.setProduct(product);
            history.setProductPrice(dropPrice);
            priceHistoryRepository.save(history);

            notificationService.checkAndNotify(product, dropPrice);

            response.put("status", "SUCCESS");
            response.put("productName", product.getProductName());
            response.put("oldPrice", currentPrice);
            response.put("newPrice", dropPrice);
            response.put("dropPercent", 50);
            response.put("message", "Simulated 20% price drop! Check email.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get product and watchlist status
     * GET /api/test/status/{productId}
     */
    @GetMapping("/status/{productId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable Integer productId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            response.put("productId", product.getProductId());
            response.put("productName", product.getProductName());
            response.put("currentPrice", product.getProductPrice());
            response.put("link", product.getLink());

            // Get watchlist info
            List<WatchListTemporary> watchers = watchListRepository.findByProduct(product);
            response.put("totalWatchers", watchers.size());

            // Detailed watcher info
            List<Map<String, Object>> watcherDetails = watchers.stream().map(w -> {
                Map<String, Object> detail = new HashMap<>();
                detail.put("watchListId", w.getWatchListId());
                detail.put("userEmail", w.getUsers().getEmail());
                detail.put("targetPrice", w.getTargetPrice());
                detail.put("notificationEnabled", w.getNotificationEnabled());
                detail.put("notificationSent", w.getNotificationSent());
                detail.put("lastNotifiedAt", w.getLastNotifiedAt());
                detail.put("lowestNotifiedPrice", w.getLowestNotifiedPrice());
                detail.put("cooldownHours", w.getCooldownHours());
                detail.put("inCooldown", w.isInCooldown());

                // Calculate if notification would trigger
                boolean wouldNotify = w.shouldNotify(product.getProductPrice());
                detail.put("wouldNotify", wouldNotify);

                return detail;
            }).toList();

            response.put("watchers", watcherDetails);
            response.put("status", "SUCCESS");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Reset notification state for a watchlist entry
     * POST /api/test/reset-notification/{watchListId}
     */
    @PostMapping("/reset-notification/{watchListId}")
    public ResponseEntity<Map<String, Object>> resetNotification(@PathVariable Integer watchListId) {
        Map<String, Object> response = new HashMap<>();

        try {
            WatchListTemporary watchList = watchListRepository.findById(watchListId)
                    .orElseThrow(() -> new IllegalArgumentException("Watchlist entry not found"));

            watchList.resetNotification();
            watchListRepository.save(watchList);

            response.put("status", "SUCCESS");
            response.put("message", "Notification state reset. User will be notified on next price check.");
            response.put("watchListId", watchListId);
            response.put("userEmail", watchList.getUsers().getEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Force bypass cooldown for testing
     * POST /api/test/bypass-cooldown/{watchListId}
     */
    @PostMapping("/bypass-cooldown/{watchListId}")
    public ResponseEntity<Map<String, Object>> bypassCooldown(@PathVariable Integer watchListId) {
        Map<String, Object> response = new HashMap<>();

        try {
            WatchListTemporary watchList = watchListRepository.findById(watchListId)
                    .orElseThrow(() -> new IllegalArgumentException("Watchlist entry not found"));

            // Set last notified to 25 hours ago (past 24-hour cooldown)
            watchList.setLastNotifiedAt(java.time.LocalDateTime.now().minusHours(25));
            watchListRepository.save(watchList);

            response.put("status", "SUCCESS");
            response.put("message", "Cooldown bypassed. Next notification check will proceed.");
            response.put("inCooldown", watchList.isInCooldown());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Complete test scenario - resets everything and triggers notification
     * POST /api/test/full-test/{productId}
     */
    @PostMapping("/full-test/{productId}")
    public ResponseEntity<Map<String, Object>> fullTest(@PathVariable Integer productId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            List<WatchListTemporary> watchers = watchListRepository.findByProduct(product);

            if (watchers.isEmpty()) {
                response.put("status", "ERROR");
                response.put("message", "No watchers found. Add product to watchlist first!");
                return ResponseEntity.ok(response);
            }

            // Reset all notifications
            for (WatchListTemporary w : watchers) {
                w.resetNotification();
                w.setNotificationEnabled(true);
                watchListRepository.save(w);
            }

            // Set price below all targets
            Double lowestTarget = watchers.stream()
                    .map(WatchListTemporary::getTargetPrice)
                    .min(Double::compareTo)
                    .orElse(100.0);

            Double testPrice = lowestTarget * 0.75; // 25% below lowest target
            Double oldPrice = product.getProductPrice();

            product.setProductPrice(testPrice);
            productRepository.save(product);

            // Trigger notifications
            System.out.println("\n========================================");
            System.out.println("🧪 FULL TEST - TRIGGERING NOTIFICATIONS");
            System.out.println("========================================");
            notificationService.checkAndNotify(product, testPrice);
            System.out.println("========================================\n");

            response.put("status", "SUCCESS");
            response.put("productName", product.getProductName());
            response.put("oldPrice", oldPrice);
            response.put("newPrice", testPrice);
            response.put("targetPrice", lowestTarget);
            response.put("notificationsSent", watchers.size());
            response.put("message", "All notifications reset and triggered! Check your email.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * List all products with their watchers
     * GET /api/test/list-all
     */
    @GetMapping("/list-all")
    public ResponseEntity<Map<String, Object>> listAll() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Product> products = productRepository.findAll();

            List<Map<String, Object>> productList = products.stream().map(p -> {
                Map<String, Object> pMap = new HashMap<>();
                pMap.put("productId", p.getProductId());
                pMap.put("productName", p.getProductName());
                pMap.put("currentPrice", p.getProductPrice());
                pMap.put("asin", p.getASIN());

                List<WatchListTemporary> watchers = watchListRepository.findByProduct(p);
                pMap.put("watchersCount", watchers.size());

                return pMap;
            }).toList();

            response.put("status", "SUCCESS");
            response.put("totalProducts", products.size());
            response.put("products", productList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}