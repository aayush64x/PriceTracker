package com.project.PriceTracker.scraper;

import com.project.PriceTracker.model.Product;
import com.microsoft.playwright.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ScraperService {

    private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);

    private final String wsEndpoint;

    @Value("${scraper.headless:true}")
    private boolean headless;

    @Value("${scraper.timeout:30000}")
    private int timeout;

    @Value("${scraper.max-results:10}")
    private int maxResults;

    public ScraperService(@Value("${brightdata.wsEndpoint}") String wsEndpoint) {
        this.wsEndpoint = wsEndpoint;
    }

    public List<Product> scrapeAmazon(String keyword) {
        List<Product> products = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            logger.error("Keyword is null or empty");
            return products;
        }

        Browser browser = null;
        Page page = null;

        try (Playwright playwright = Playwright.create()) {
            logger.info("Starting scrape for keyword: {}", keyword);

            // Launch browser with Bright Data endpoint
            if (wsEndpoint != null && !wsEndpoint.isEmpty() && wsEndpoint.startsWith("wss://")) {
                // Connect to Bright Data
                BrowserType.ConnectOptions connectOptions = new BrowserType.ConnectOptions()
                        .setTimeout(timeout);
                browser = playwright.chromium().connect(wsEndpoint, connectOptions);
                logger.info("Connected to Bright Data browser at: {}", wsEndpoint.substring(0, 30) + "...");
            } else {
                // Use local browser (for development)
                BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                        .setHeadless(headless)
                        .setTimeout(timeout);
                browser = playwright.chromium().launch(launchOptions);
                logger.info("Launched local Chromium browser");
            }

            page = browser.newPage();
            page.setDefaultTimeout(timeout);

            // Navigate to Amazon
            logger.info("Navigating to Amazon...");
            page.navigate("https://www.amazon.com", new Page.NavigateOptions().setTimeout(timeout));

            // Handle potential CAPTCHA or bot detection
            if (page.content().toLowerCase().contains("captcha")) {
                logger.error("CAPTCHA detected - cannot proceed");
                return products;
            }

            // Search for keyword
            logger.info("Searching for: {}", keyword);
            page.fill("#twotabsearchtextbox", keyword);
            page.click("#nav-search-submit-button");

            // Wait for results
            try {
                page.waitForSelector("div.s-main-slot", new Page.WaitForSelectorOptions().setTimeout(timeout));
            } catch (TimeoutError e) {
                logger.error("Timeout waiting for search results");
                return products;
            }

            // Scroll to load lazy-loaded content
            logger.info("Scrolling to load content...");
            for (int i = 0; i < 3; i++) {
                page.evaluate("window.scrollBy(0, 1000)");
                page.waitForTimeout(1000);
            }

            // Parse HTML
            String html = page.content();
            Document doc = Jsoup.parse(html);

            // Extract products
            Elements items = doc.select("div.s-main-slot div[data-component-type='s-search-result']");
            logger.info("Found {} search results", items.size());

            int limit = Math.min(items.size(), maxResults);
            int successCount = 0;

            for (int i = 0; i < limit; i++) {
                try {
                    Element item = items.get(i);
                    Product product = extractProductFromElement(item, keyword);

                    if (product != null && isValidProduct(product)) {
                        products.add(product);
                        successCount++;
                        logger.debug("Successfully extracted product: {} (ASIN: {})",
                                product.getProductName(), product.getASIN());
                    }
                } catch (Exception e) {
                    logger.warn("Error extracting product at index {}: {}", i, e.getMessage());
                }
            }

            logger.info("Successfully scraped {}/{} products", successCount, limit);

        } catch (PlaywrightException e) {
            logger.error("Playwright error during scraping: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during scraping: {}", e.getMessage(), e);
        } finally {
            if (page != null) {
                try {
                    page.close();
                } catch (Exception e) {
                    logger.warn("Error closing page: {}", e.getMessage());
                }
            }
            if (browser != null) {
                try {
                    browser.close();
                } catch (Exception e) {
                    logger.warn("Error closing browser: {}", e.getMessage());
                }
            }
        }

        return products;
    }

    private Product extractProductFromElement(Element item, String searchKeyword) {
        Product product = new Product();

        try {
            // Extract ASIN
            String asin = item.attr("data-asin");
            if (asin == null || asin.isEmpty()) {
                logger.warn("Product missing ASIN, skipping");
                return null;
            }
            product.setASIN(asin);

            // Extract product name
            Element titleEl = item.selectFirst("h2 span");
            String productName = titleEl != null ? titleEl.text().trim() : "Unknown Product";
            product.setProductName(productName);

            // Extract product link
            String link = extractProductLink(item, asin);
            product.setLink(link);

            // Extract price
            Double price = extractPrice(item);
            product.setProductPrice(price);

            // Extract image
            String imageUrl = extractImageUrl(item);
            product.setImageURL(imageUrl);

            // Extract category from breadcrumb or department
            String category = extractCategory(item, searchKeyword);
            product.setCategory(category);

            // Extract product group/department
            String productGroup = extractProductGroup(item);
            product.setProductGroup(productGroup);

            // Set timestamp
            product.setTimestamp(new Timestamp(System.currentTimeMillis()));

            return product;

        } catch (Exception e) {
            logger.error("Error extracting product data: {}", e.getMessage());
            return null;
        }
    }

    private String extractProductLink(Element item, String asin) {
        Element linkEl = item.selectFirst("h2 a.a-link-normal");
        if (linkEl != null) {
            String href = linkEl.attr("href");
            if (href != null && !href.isEmpty()) {
                return href.startsWith("http") ? href : "https://www.amazon.com" + href;
            }
        }
        // Fallback to ASIN-based link
        return "https://www.amazon.com/dp/" + asin;
    }

    private Double extractPrice(Element item) {
        // Try primary price selector
        Element priceEl = item.selectFirst("span.a-price > span.a-offscreen");
        if (priceEl != null) {
            return parsePrice(priceEl.text());
        }

        // Try alternate price selectors
        priceEl = item.selectFirst("span.a-price-whole");
        if (priceEl != null) {
            Element fractionEl = item.selectFirst("span.a-price-fraction");
            String wholePart = priceEl.text().replaceAll("[^0-9]", "");
            String fractionPart = fractionEl != null ? fractionEl.text().replaceAll("[^0-9]", "") : "00";
            return parsePrice(wholePart + "." + fractionPart);
        }

        // Try text-based price
        priceEl = item.selectFirst("span.a-color-price");
        if (priceEl != null) {
            return parsePrice(priceEl.text());
        }

        return null;
    }

    private Double parsePrice(String priceText) {
        if (priceText == null || priceText.isEmpty()) {
            return null;
        }

        try {
            String cleaned = priceText.replaceAll("[^0-9.]", "");
            if (!cleaned.isEmpty()) {
                return Double.parseDouble(cleaned);
            }
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse price: {}", priceText);
        }

        return null;
    }

    private String extractImageUrl(Element item) {
        // Try primary image
        Element imgEl = item.selectFirst("img.s-image");
        if (imgEl != null) {
            String src = imgEl.attr("src");
            if (src != null && !src.isEmpty()) {
                return src;
            }
        }

        // Try data-image-latency attribute
        if (imgEl != null) {
            String dataSrc = imgEl.attr("data-image-latency");
            if (dataSrc != null && !dataSrc.isEmpty()) {
                return dataSrc;
            }
        }

        return "";
    }

    private String extractCategory(Element item, String searchKeyword) {
        // Try to extract from badge or label
        Element badgeEl = item.selectFirst("span.a-badge-label");
        if (badgeEl != null && !badgeEl.text().isEmpty()) {
            return badgeEl.text().trim();
        }

        // Try department/category text
        Element categoryEl = item.selectFirst("div.a-section.a-spacing-none.a-spacing-top-small span.a-size-base");
        if (categoryEl != null && !categoryEl.text().isEmpty()) {
            return categoryEl.text().trim();
        }

        // Fallback to search keyword as category
        return searchKeyword != null ? capitalizeWords(searchKeyword) : "General";
    }

    private String extractProductGroup(Element item) {
        // Try to extract department
        Element deptEl = item.selectFirst("h2 a.a-link-normal");
        if (deptEl != null) {
            String href = deptEl.attr("href");
            if (href.contains("/b/") || href.contains("node=")) {
                // Extract department from URL structure
                String[] parts = href.split("/");
                for (String part : parts) {
                    if (part.length() > 3 && !part.matches(".*\\d.*")) {
                        return capitalizeWords(part.replace("-", " "));
                    }
                }
            }
        }

        // Try sponsored badge or brand
        Element brandEl = item.selectFirst("span.a-size-base-plus");
        if (brandEl != null && !brandEl.text().isEmpty()) {
            return brandEl.text().trim();
        }

        return "Electronics"; // Default fallback
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    private boolean isValidProduct(Product product) {
        return product != null
                && product.getASIN() != null && !product.getASIN().isEmpty()
                && product.getProductName() != null && !product.getProductName().isEmpty()
                && product.getLink() != null && !product.getLink().isEmpty();
    }
}