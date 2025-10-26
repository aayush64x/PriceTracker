package com.project.PriceTracker.service;

import com.project.PriceTracker.dto.WatchListRequestDTO;
import com.project.PriceTracker.model.Product;
import com.project.PriceTracker.model.UserTemporary;
import com.project.PriceTracker.model.WatchListTemporary;
import com.project.PriceTracker.repository.ProductRepository;
import com.project.PriceTracker.repository.UserTemporaryRepository;
import com.project.PriceTracker.repository.WatchListTemporaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WatchListTemporaryService {

    private final WatchListTemporaryRepository watchListRepository;
    private final ProductRepository productRepository;
    private final UserTemporaryRepository userTemporaryRepository;
    private final ProductCacheService productCacheService;

    @Autowired
    public WatchListTemporaryService(
            WatchListTemporaryRepository watchListRepository,
            ProductRepository productRepository,
            UserTemporaryRepository userTemporaryRepository,
            ProductCacheService productCacheService) {
        this.watchListRepository = watchListRepository;
        this.productRepository = productRepository;
        this.userTemporaryRepository = userTemporaryRepository;
        this.productCacheService = productCacheService;
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

        return watchListRepository.save(watchListTemporary);
    }
}
