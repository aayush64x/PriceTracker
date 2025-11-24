package com.project.PriceTracker.service;

import com.project.PriceTracker.dto.ProductDTO;
import com.project.PriceTracker.dto.WatchListRequestDTO;
import com.project.PriceTracker.model.*;
import com.project.PriceTracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    // Add watchlist entry
    @Transactional
    public WatchListTemporary addToWatchList(WatchListRequestDTO request) {
        String email = request.getEmail();
        String asin = request.getAsin();

        Users user = findOrCreateUser(email);
        Product product = findOrCreateProduct(asin);

        // Check if already in watchlist
        Optional<WatchListTemporary> existing = watchListRepository.findByProductAndUsers(product, user);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Product already in watchlist");
        }

        WatchListTemporary watchListTemporary = new WatchListTemporary();
        watchListTemporary.setUsers(user);
        watchListTemporary.setProduct(product);
        watchListTemporary.setTargetPrice(request.getTargetPrice());

        // Create initial price history entry
        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setProduct(product);
        priceHistory.setProductPrice(product.getProductPrice());
        priceHistoryRepository.save(priceHistory);

        return watchListRepository.save(watchListTemporary);
    }

    // Get all products in user's watchlist
    @Transactional(readOnly = true)
    public List<ProductDTO> getSavedProducts(String email) {
        List<WatchListTemporary> watchListEntries = watchListRepository.findByUsersEmail(email);
        List<ProductDTO> productDTOList = new ArrayList<>();

        for (WatchListTemporary entry : watchListEntries) {
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

    // Delete from watchlist by watchListId
    @Transactional
    public void deleteFromWatchList(Integer watchListId) {
        WatchListTemporary watchList = watchListRepository.findById(watchListId)
                .orElseThrow(() -> new IllegalArgumentException("Watchlist item not found with ID: " + watchListId));

        watchListRepository.delete(watchList);
    }

    // Delete from watchlist by email and ASIN
    @Transactional
    public void deleteFromWatchListByEmailAndAsin(String email, String asin) {
        Users user = findOrCreateUser(email);
        Product product = productRepository.findByASIN(asin)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ASIN: " + asin));

        WatchListTemporary watchList = watchListRepository.findByProductAndUsers(product, user)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Watchlist entry not found for user: " + email + " and product: " + asin));

        watchListRepository.delete(watchList);
    }

    // Update target price
    @Transactional
    public WatchListTemporary updateTargetPrice(Integer watchListId, Double newTargetPrice) {
        WatchListTemporary watchList = watchListRepository.findById(watchListId)
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