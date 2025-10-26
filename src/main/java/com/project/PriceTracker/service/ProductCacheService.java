package com.project.PriceTracker.service;

import com.project.PriceTracker.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class ProductCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public ProductCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheProducts(String keyword, List<Product> products){
        String key = "search:" + keyword.trim().toLowerCase();
        redisTemplate.opsForValue().set(key, products, Duration.ofHours(24));
    }

    @SuppressWarnings("unchecked")
    public List<Product> getCachedProducts(String keyword) {
        String key = "search:" + keyword.trim().toLowerCase();
        List<Product> products = (List<Product>) redisTemplate.opsForValue().get(key);
        return products == null ? List.of() : products;
    }

    public void cacheIndividualProducts(List<Product> products){
        for (Product product : products){
            String key = "product:" + product.getASIN().trim().toUpperCase();
            redisTemplate.opsForValue().set(key, product, Duration.ofHours(24));
        }
    }

    public Product getCachedIndividualProduct(String ASIN){
        String key = "product:" + ASIN.trim().toUpperCase();
        return (Product) redisTemplate.opsForValue().get(key);
    }
}
