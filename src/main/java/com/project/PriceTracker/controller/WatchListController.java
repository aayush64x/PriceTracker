package com.project.PriceTracker.controller;

import com.project.PriceTracker.dto.WatchListRequestDTO;
import com.project.PriceTracker.dto.WatchlistItemDTO;
import com.project.PriceTracker.model.WatchList;
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

    @Autowired
    public WatchListController(WatchListTemporaryService watchListTemporaryService,
                               UserTemporaryService userTemporaryService) {
        this.watchListTemporaryService = watchListTemporaryService;
        this.userTemporaryService = userTemporaryService;
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
     * Get all products saved in user's watchlist WITH target price
     * GET /api/watchlist/saved-products?email=user@example.com
     */
    @GetMapping("/saved-products")
    public ResponseEntity<List<WatchlistItemDTO>> getProductSavedToWatchList(@RequestParam String email) {
        try {
            List<WatchlistItemDTO> watchlistItems = watchListTemporaryService.getSavedProductsWithWatchlistInfo(email);
            return ResponseEntity.ok(watchlistItems);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Delete item from watchlist
     * DELETE /api/watchlist/{watchListId}
     */
    @DeleteMapping("/{watchListId}")
    public ResponseEntity<String> deleteFromWatchList(@PathVariable Integer watchListId) {
        try {
            watchListTemporaryService.deleteFromWatchList(watchListId);
            return ResponseEntity.ok("Removed from watchlist");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Watchlist item not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to remove from watchlist: " + e.getMessage());
        }
    }

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
            WatchList updated = watchListTemporaryService.updateTargetPrice(watchListId, targetPrice);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update target price: " + e.getMessage()));
        }
    }
}