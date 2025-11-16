package com.project.PriceTracker.service;

import com.project.PriceTracker.dto.ProductDTO;
import com.project.PriceTracker.dto.WatchListRequestDTO;
import com.project.PriceTracker.model.PriceHistory;
import com.project.PriceTracker.model.Product;
import com.project.PriceTracker.model.UserTemporary;
import com.project.PriceTracker.model.WatchListTemporary;
import com.project.PriceTracker.repository.PriceHistoryRepository;
import com.project.PriceTracker.repository.ProductRepository;
import com.project.PriceTracker.repository.UserTemporaryRepository;
import com.project.PriceTracker.repository.WatchListTemporaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WatchListTemporaryService {

    private final WatchListTemporaryRepository watchListRepository;
    private final ProductRepository productRepository;
    private final UserTemporaryRepository userTemporaryRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductCacheService productCacheService;

    @Autowired
    public WatchListTemporaryService(
            WatchListTemporaryRepository watchListRepository,
            ProductRepository productRepository,
            UserTemporaryRepository userTemporaryRepository,
            ProductCacheService productCacheService,
            PriceHistoryRepository priceHistoryRepository) {
        this.watchListRepository = watchListRepository;
        this.productRepository = productRepository;
        this.userTemporaryRepository = userTemporaryRepository;
        this.productCacheService = productCacheService;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    // Find user by email or create
    @Transactional
    public UserTemporary findOrCreateUser(String email) {
        return userTemporaryRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserTemporary newUser = new UserTemporary();
                    newUser.setEmail(email);
                    return userTemporaryRepository.save(newUser);
                });
    }

    // Find product by ASIN (DB first, then cache) safely
    @Transactional
    public Product findOrCreateProduct(String ASIN) {
        // 1️⃣ Check DB first
        Optional<Product> existing = productRepository.findByASIN(ASIN);
        if (existing.isPresent()) return existing.get();

        // 2️⃣ Check Redis cache
        Product cachedProduct = productCacheService.getCachedIndividualProduct(ASIN);
        if (cachedProduct != null) {
            // Reattach a new managed entity using data from cache
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

    // Add watchlist entry safely
    @Transactional
    public WatchListTemporary addToWatchList(WatchListRequestDTO request) {
        String email = request.getUserEmail();
        String asin = request.getAsin();

        UserTemporary user = findOrCreateUser(email);
        Product product = findOrCreateProduct(asin);

        WatchListTemporary watchListTemporary = new WatchListTemporary();
        watchListTemporary.setUserTemporary(user);
        watchListTemporary.setProduct(product);
        watchListTemporary.setTargetPrice(request.getTargetPrice());

        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setProduct(product);                     // link to the product
        priceHistory.setProductPrice(product.getProductPrice()); // store current price
        priceHistoryRepository.save(priceHistory);

        return watchListRepository.save(watchListTemporary);
    }

    public List<ProductDTO> getSavedProducts(String email) {
        // Get all watchlist entries for the user
        List<WatchListTemporary>   watchListEntries = watchListRepository.findByUserTemporaryEmail(email);
        List<ProductDTO> productDTOList = new ArrayList<>();
        for (WatchListTemporary productInWatchListTemporary : watchListEntries){
            Product product = productInWatchListTemporary.getProduct();
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


    public WatchListTemporary updateTargetPrice(Integer watchListId, Double newTargetPrice) {
        WatchListTemporary watchList = watchListRepository.findById(watchListId)
                .orElseThrow(() -> new IllegalArgumentException("Watchlist item not found with ID: " + watchListId));

        if (newTargetPrice == null || newTargetPrice <= 0) {
            throw new IllegalArgumentException("Target price must be greater than 0");
        }

        watchList.setTargetPrice(newTargetPrice);

        // Reset notification if target price changed
        // This allows user to get notified if new target is met
        watchList.resetNotification();

        return watchListRepository.save(watchList);
    }

    /**
     * Check if product is in user's watchlist
     */
    public boolean isProductInWatchList(String email, String asin) {
        try {
            UserTemporary user = userTemporaryRepository.findByEmail(email).orElse(null);
            Product product = productRepository.findByASIN(asin).orElse(null);

            if (user == null || product == null) {
                return false;
            }

            return watchListRepository.findByProductAndUserTemporary(product, user).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get watchlist item by ID
     */
    public WatchListTemporary getWatchListById(Integer watchListId) {
        return watchListRepository.findById(watchListId)
                .orElseThrow(() -> new IllegalArgumentException("Watchlist item not found with ID: " + watchListId));
    }

    /**
     * Count total watchlist items for a user
     */
    public long countUserWatchListItems(String email) {
        UserTemporary user = userTemporaryRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return 0;
        }
        return watchListRepository.countByUserTemporary(user);
    }

}
