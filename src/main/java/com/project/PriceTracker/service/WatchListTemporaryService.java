package com.project.PriceTracker.service;

import com.project.PriceTracker.dto.ProductDTO;
import com.project.PriceTracker.dto.WatchListRequestDTO;
import com.project.PriceTracker.dto.WatchlistItemDTO;
import com.project.PriceTracker.model.*;
import com.project.PriceTracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WatchListTemporaryService {

    private final WatchListTemporaryRepository watchListRepository;
    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductCacheService productCacheService;
    private final UserRepository userRepository;

    @Autowired
    public WatchListTemporaryService(
            WatchListTemporaryRepository watchListRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            ProductCacheService productCacheService,
            PriceHistoryRepository priceHistoryRepository
    ) {
        this.watchListRepository = watchListRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productCacheService = productCacheService;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    // Find user by email or throw exception
    @Transactional(readOnly = true)
    public Users findOrCreateUser(String email) {
        Users user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }
        return user;
    }

    // Find product by ASIN (DB first, then cache)
    @Transactional
    public Product findOrCreateProduct(String ASIN) {
        // 1️⃣ Check DB first
        Optional<Product> existing = productRepository.findByASIN(ASIN);
        if (existing.isPresent()) return existing.get();

        // 2️⃣ Check Redis cache
        Product cachedProduct = productCacheService.getCachedIndividualProduct(ASIN);
        if (cachedProduct != null) {
            // Create new managed entity using data from cache
            Product managed = new Product();
            managed.setASIN(cachedProduct.getASIN());
            managed.setProductName(cachedProduct.getProductName());
            managed.setProductPrice(cachedProduct.getProductPrice());
            managed.setCategory(cachedProduct.getCategory());
            managed.setImageURL(cachedProduct.getImageURL());
            managed.setLink(cachedProduct.getLink());
            managed.setProductGroup(cachedProduct.getProductGroup());

            Product saved = productRepository.save(managed);
            productCacheService.cacheIndividualProducts(List.of(saved)); // update cache
            return saved;
        }

        throw new RuntimeException("Product with ASIN " + ASIN + " not found in DB or cache");
    }

    /**
     * Add a price history entry for a product with current timestamp
     */
    private void addPriceHistoryEntry(Product product) {
        // Check if a price history entry already exists for today
        // to avoid duplicate entries when adding to watchlist multiple times in one day
        List<PriceHistory> existingHistory = priceHistoryRepository.findByProduct_ASIN(product.getASIN());

        // Get today's date (without time component)
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();

        // Check if there's already an entry for today
        boolean hasEntryToday = existingHistory.stream()
                .anyMatch(ph -> {
                    LocalDateTime entryDate = ph.getDateAdded().toLocalDateTime().toLocalDate().atStartOfDay();
                    return entryDate.equals(today);
                });

        // Only add new entry if there's no entry for today
        if (!hasEntryToday) {
            PriceHistory priceHistory = PriceHistory.builder()
                    .product(product)
                    .productPrice(product.getProductPrice())
                    .dateAdded(Timestamp.valueOf(LocalDateTime.now()))
                    .build();

            priceHistoryRepository.save(priceHistory);
            System.out.println("Added price history entry for ASIN: " + product.getASIN() +
                    " Price: $" + product.getProductPrice() +
                    " at " + LocalDateTime.now());
        } else {
            System.out.println("Price history entry already exists for today for ASIN: " + product.getASIN());
        }
    }

    // Add watchlist entry
    @Transactional
    public WatchList addToWatchList(WatchListRequestDTO request) {
        String email = request.getEmail();
        String asin = request.getAsin();

        Users user = findOrCreateUser(email);
        Product product = findOrCreateProduct(asin);

        // Check if already in watchlist
        Optional<WatchList> existing = watchListRepository.findByProductAndUsers(product, user);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Product already in watchlist");
        }

        // Create watchlist entry
        WatchList watchList = new WatchList();
        watchList.setUsers(user);
        watchList.setProduct(product);
        watchList.setTargetPrice(request.getTargetPrice());

        // 🎯 Add current price to price history with current timestamp
        addPriceHistoryEntry(product);

        return watchListRepository.save(watchList);
    }

    // Get all products in user's watchlist (OLD METHOD - kept for backward compatibility)
    @Transactional(readOnly = true)
    public List<ProductDTO> getSavedProducts(String email) {
        List<WatchList> watchListEntries = watchListRepository.findByUsersEmail(email);
        List<ProductDTO> productDTOList = new ArrayList<>();

        for (WatchList entry : watchListEntries) {
            Product product = entry.getProduct();
            ProductDTO productDTO = new ProductDTO(
                    product.getProductId(),
                    product.getASIN(),
                    product.getProductName(),
                    product.getProductPrice(),
                    product.getLink(),
                    product.getImageURL(),
                    product.getTimestamp(),
                    product.getProductGroup(),
                    product.getCategory()
            );
            productDTOList.add(productDTO);
        }

        return productDTOList;
    }

    /**
     * NEW METHOD: Get all watchlist items for a user with product details and target price
     * This returns WatchlistItemDTO which includes both watchlist ID, targetPrice, and all product details
     */
    @Transactional(readOnly = true)
    public List<WatchlistItemDTO> getSavedProductsWithWatchlistInfo(String email) {
        // Find user by email
        Users user = findOrCreateUser(email);

        // Get all watchlist entries for this user
        List<WatchList> watchlistEntries = watchListRepository.findByUsersEmail(email);

        // Convert to WatchlistItemDTO
        return watchlistEntries.stream()
                .map(watchlistEntry -> {
                    Product product = watchlistEntry.getProduct();

                    WatchlistItemDTO dto = new WatchlistItemDTO();

                    // Set watchlist-specific fields
                    dto.setId(watchlistEntry.getWatchListId());
                    dto.setTargetPrice(watchlistEntry.getTargetPrice());

                    // Set product fields
                    dto.setAsin(product.getASIN());
                    dto.setProductName(product.getProductName());
                    dto.setPrice(product.getProductPrice());
                    dto.setImageURL(product.getImageURL());
                    dto.setUrl(product.getLink());
                    dto.setCategory(product.getCategory());
                    dto.setBrand(product.getProductGroup()); // Using ProductGroup as Brand

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Delete from watchlist by watchListId
    @Transactional
    public void deleteFromWatchList(Integer watchListId) {
        WatchList watchList = watchListRepository.findById(watchListId)
                .orElseThrow(() -> new IllegalArgumentException("Watchlist item not found with ID: " + watchListId));

        watchListRepository.delete(watchList);
    }

    // Delete from watchlist by email and ASIN
    @Transactional
    public void deleteFromWatchListByEmailAndAsin(String email, String asin) {
        Users user = findOrCreateUser(email);
        Product product = productRepository.findByASIN(asin)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ASIN: " + asin));

        WatchList watchList = watchListRepository.findByProductAndUsers(product, user)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Watchlist entry not found for user: " + email + " and product: " + asin));

        watchListRepository.delete(watchList);
    }

    // Update target price
    @Transactional
    public WatchList updateTargetPrice(Integer watchListId, Double newTargetPrice) {
        WatchList watchList = watchListRepository.findById(watchListId)
                .orElseThrow(() -> new IllegalArgumentException("Watchlist item not found with ID: " + watchListId));

        if (newTargetPrice == null || newTargetPrice <= 0) {
            throw new IllegalArgumentException("Target price must be greater than 0");
        }

        watchList.setTargetPrice(newTargetPrice);

        // Reset notification if target price changed
        watchList.resetNotification();

        return watchListRepository.save(watchList);
    }
}