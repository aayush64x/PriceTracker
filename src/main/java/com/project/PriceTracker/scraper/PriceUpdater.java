package com.project.PriceTracker.scraper;

import com.project.PriceTracker.model.PriceHistory;
import com.project.PriceTracker.model.Product;
import com.project.PriceTracker.notification.NotificationService;
import com.project.PriceTracker.repository.PriceHistoryRepository;
import com.project.PriceTracker.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PriceUpdater {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private IndividualScraper individualScraper;

    @Autowired
    private NotificationService notificationService;  // ⬅️ ADD THIS

    List<Product> productList = new ArrayList<>();

    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        System.out.println("Running scraper on startup...");
        scrapePricesDaily();
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void scrapePricesDaily() {
        System.out.println("==========================================");
        System.out.println("Starting scheduled price scraping...");
        System.out.println("==========================================");

        productList = productRepository.findAll();
        System.out.println("Found " + productList.size() + " products to scrape");

        int successCount = 0;
        int failCount = 0;

        for (Product product : productList) {
            System.out.println("\n--- Processing: " + product.getProductName() + " ---");

            String link = product.getLink();

            // Validate link before scraping
            if (link == null || link.trim().isEmpty()) {
                System.err.println("❌ Invalid link for product: " + product.getProductName());
                failCount++;
                continue;
            }

            try {
                // Scrape product price
                IndividualScraper.PriceResult priceResult = individualScraper.scrapeAmazonPrice(link);
                Double price = priceResult.getPrice();

                // Check if price was successfully scraped
                if (price == null) {
                    System.err.println("❌ Failed to scrape price for " + product.getProductName());
                    failCount++;
                    continue;
                }

                System.out.println("✅ Scraped price: $" + price);
                successCount++;

                // Get current price before update
                Double currentPrice = product.getProductPrice();

                // Handle case where product has no previous price
                if (currentPrice == null || !price.equals(currentPrice)) {
                    System.out.println("💰 Price changed from $" + currentPrice + " to $" + price);

                    // Update price history
                    updatePrices(product, price);

                    // Update product's current price
                    product.setProductPrice(price);
                    productRepository.save(product);

                    // ========================================
                    // 🔔 CHECK AND SEND NOTIFICATIONS
                    // ========================================
                    System.out.println("🔔 Checking for notification triggers...");
                    notificationService.checkAndNotify(product, price);

                } else {
                    System.out.println("Price unchanged at $" + currentPrice);
                }

                // Add small delay between scrapes to avoid rate limiting
                Thread.sleep(3000);  // 3 second delay

            } catch (Exception e) {
                System.err.println("❌ Error processing " + product.getProductName() + ": " + e.getMessage());
                e.printStackTrace();
                failCount++;
            }
        }

        System.out.println("\n==========================================");
        System.out.println("Scraping complete!");
        System.out.println("✅ Success: " + successCount);
        System.out.println("❌ Failed: " + failCount);
        System.out.println("==========================================\n");
    }

    public PriceHistory updatePrices(Product product, Double newPrice) {
        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setProductPrice(newPrice);
        priceHistory.setProduct(product);
        return priceHistoryRepository.save(priceHistory);
    }
}