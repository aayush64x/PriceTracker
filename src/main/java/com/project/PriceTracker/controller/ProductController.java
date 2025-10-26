package com.project.PriceTracker.controller;

import com.project.PriceTracker.dto.ProductDTO;
import com.project.PriceTracker.model.Product;
import com.project.PriceTracker.repository.ProductRepository;
import com.project.PriceTracker.service.ProductCacheService;
import com.project.PriceTracker.service.ScraperService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/products/")
public class ProductController {

    private final ScraperService scraperService;

    private final ProductCacheService cacheService;

    public ProductController(ScraperService scraperService, ProductCacheService cacheService) {
        this.scraperService = scraperService;
        this.cacheService = cacheService;
    }

    @GetMapping("/search")
    public List<ProductDTO> searchProducts(@RequestParam String keyword) {
        // 1️⃣ Check Redis cache first
        List<Product> cached = cacheService.getCachedProducts(keyword);
        if (!cached.isEmpty()) {
            return cached.stream().map(p -> new ProductDTO(
                    p.getProductId(),
                    p.getASIN(),
                    p.getProductName(),
                    p.getProductPrice(),
                    p.getLink(),
                    p.getImageURL(),
                    p.getTimestamp(),
                    p.getProductGroup(),
                    p.getCategory()
            )).toList(); //list of JSON objects
        }

        // 2️⃣ Scrape Amazon via Bright Data
        List<Product> scraped = scraperService.scrapeAmazon(keyword);
        System.out.println("Scraped products: " + scraped.size());

        // 3️⃣ Cache results
        int id = 0;
        for(Product product : scraped){
            product.setProductId(id);
            id += 1;
        }
        cacheService.cacheProducts(keyword, scraped);
        cacheService.cacheIndividualProducts(scraped);

        // 4️⃣ Convert to DTO for view
        List<ProductDTO> dtos = scraped.stream().map(p -> new ProductDTO(
                p.getProductId(),
                p.getASIN(),
                p.getProductName(),
                p.getProductPrice(),
                p.getLink(),
                p.getImageURL(),
                p.getTimestamp(),
                p.getProductGroup(),
                p.getCategory()
        )).toList();

        return dtos;
    }

    @GetMapping("/product-detail")
    public ProductDTO getIndividualProduct(@RequestParam String q){
        Product product = cacheService.getCachedIndividualProduct(q);
        return new ProductDTO(
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
    }
}
