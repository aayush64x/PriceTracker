package com.project.PriceTracker.service;

import com.project.PriceTracker.dto.ProductDTO;
import com.project.PriceTracker.model.Product;
import com.project.PriceTracker.model.WatchList;
import com.project.PriceTracker.model.WatchList;
import com.project.PriceTracker.repository.ProductRepository;
import com.project.PriceTracker.repository.WatchListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WatchListService {

    @Autowired
    public WatchListRepository watchListRepository;
    private final ProductRepository productRepository;

    public WatchListService(WatchListRepository watchListRepository, ProductRepository productRepository) {
        this.watchListRepository = watchListRepository;
        this.productRepository = productRepository;
    }

    public List<ProductDTO> getProductsForUser(int userId) {
        List<WatchList> watchlistItems = watchListRepository.findByUserId(userId);
        List<ProductDTO> products = new ArrayList<>();

        for (WatchList item : watchlistItems) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);

            if (product != null) {
                products.add(new ProductDTO(
                        product.getProductId(),
                        product.getProductName(),
                        product.getProductPrice(),
                        product.getLink()
                ));
            }
        }
        return products;
    }

}
