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
    public UserTemporary findOrCreateUser(String email) {
        return userTemporaryRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserTemporary newUser = new UserTemporary();
                    newUser.setEmail(email);
                    return userTemporaryRepository.save(newUser);
                });
    }

    // Find product by ASIN or create
    public Product findOrCreateProduct(String ASIN) {
        Optional<Product> existing = productRepository.findByASIN(ASIN);

        if (existing.isPresent()) return existing.get();

        Product cachedProduct = productCacheService.getCacheIndividualProducts(ASIN);
        if (cachedProduct != null) return productRepository.save(cachedProduct);

        throw new RuntimeException("Product with ASIN " + ASIN + " not found in DB or cache");
    }


    // Add watchlist entry
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

