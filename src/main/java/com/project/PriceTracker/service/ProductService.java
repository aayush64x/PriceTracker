package com.project.PriceTracker.service;

import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductCacheService cacheService;


    public ProductService(ProductCacheService cacheService) {
        this.cacheService = cacheService;

    }

    /*
    public List<ProductDTO> searchProducts(String keyword){
        List<Product> cachedProducts = scraperService.getCachedProducts(keyword);
        if ca
        return scrapedProducts;
    }
    */

}
