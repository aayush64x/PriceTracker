package com.project.PriceTracker.scraper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class IndividualScraper {

    private Playwright playwright;
    private Browser browser;
    private final Random random = new Random();

    // User agents to rotate
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0"
    };

    @PostConstruct
    public void init() {
        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true) // Changed to true for production
                            .setArgs(java.util.Arrays.asList(
                                    "--disable-blink-features=AutomationControlled",
                                    "--disable-dev-shm-usage",
                                    "--no-sandbox"
                            ))
            );
            System.out.println("Browser initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize browser: " + e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        if (browser != null) {
            try {
                browser.close();
            } catch (Exception e) {
                System.err.println("Error closing browser: " + e.getMessage());
            }
        }
        if (playwright != null) {
            try {
                playwright.close();
            } catch (Exception e) {
                System.err.println("Error closing playwright: " + e.getMessage());
            }
        }
    }

    public PriceResult scrapeAmazonPrice(String url) {
        if (browser == null) {
            return PriceResult.error("Browser not initialized");
        }

        BrowserContext context = null;
        Page page = null;

        try {
            // Create a new context with anti-detection measures
            context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent(getRandomUserAgent())
                    .setViewportSize(1920, 1080)
                    .setLocale("en-US")
                    .setTimezoneId("America/New_York")
            );

            // Add extra headers to appear more legitimate
            context.setExtraHTTPHeaders(createHeaders());

            page = context.newPage();

            // Set reasonable timeouts
            page.setDefaultNavigationTimeout(30000);
            page.setDefaultTimeout(30000);

            // Add random delay before navigation (simulate human behavior)
            randomDelay(1000, 3000);

            try {
                page.navigate(url);
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);

                // Additional wait for dynamic content
                randomDelay(2000, 4000);

            } catch (TimeoutError e) {
                return PriceResult.error("Page load timeout: " + url);
            } catch (Exception e) {
                return PriceResult.error("Navigation failed: " + e.getMessage());
            }

            // Validate page load
            String title = page.title();
            if (isErrorPage(title)) {
                return PriceResult.error("Error page detected: " + title);
            }

            // Check for CAPTCHA or robot check
            String content = page.content();
            if (content.contains("robot check") || content.contains("captcha")) {
                return PriceResult.error("CAPTCHA detected - Amazon blocked request");
            }

            // Get and parse HTML
            Document doc = Jsoup.parse(content);

            // Extract price with enhanced logic
            Double price = extractPrice(doc);

            if (price == null) {
                return PriceResult.error("Price not found on page");
            }

            // Extract additional metadata
            String productTitle = extractProductTitle(doc);
            String availability = extractAvailability(doc);

            return PriceResult.success(price, productTitle, availability);

        } catch (Exception e) {
            return PriceResult.error("Scraping error: " + e.getMessage());
        } finally {
            // Close context and page
            if (page != null) {
                try {
                    page.close();
                } catch (Exception e) {
                    System.err.println("Error closing page: " + e.getMessage());
                }
            }
            if (context != null) {
                try {
                    context.close();
                } catch (Exception e) {
                    System.err.println("Error closing context: " + e.getMessage());
                }
            }
        }
    }

    private Double extractPrice(Document doc) {
        // Updated selectors for 2024/2025 Amazon layout
        String[] selectors = {
                // New Amazon layout
                "span.a-price[data-a-color='base'] span.a-offscreen",
                "span.a-price[data-a-color='price'] span.a-offscreen",
                ".a-price.aok-align-center span.a-offscreen",

                // Whole and fraction parts
                ".a-price-whole",
                "span.a-price-whole",
                "#corePrice_feature_div .a-price-whole",

                // Legacy selectors
                "#priceblock_ourprice",
                "#priceblock_dealprice",
                "#priceblock_saleprice",
                ".a-price .a-offscreen",

                // Deal price
                "span.a-price.a-text-price span.a-offscreen"
        };

        for (String selector : selectors) {
            Element priceElement = doc.selectFirst(selector);
            if (priceElement != null) {
                String priceText = priceElement.text();
                Double price = parsePrice(priceText);
                if (price != null && price > 0) {
                    return price;
                }
            }
        }

        // Try finding price by class pattern matching
        Elements priceElements = doc.select("span[class*=price], div[class*=price]");
        for (Element el : priceElements) {
            if (el.text().matches(".*\\$?\\d+[.,]\\d{2}.*")) {
                Double price = parsePrice(el.text());
                if (price != null && price > 0) {
                    return price;
                }
            }
        }

        return null;
    }

    private String extractProductTitle(Document doc) {
        Element titleElement = doc.selectFirst("#productTitle");
        return titleElement != null ? titleElement.text().trim() : "Unknown Product";
    }

    private String extractAvailability(Document doc) {
        Element availElement = doc.selectFirst("#availability span");
        return availElement != null ? availElement.text().trim() : "Unknown";
    }

    private Double parsePrice(String priceText) {
        if (priceText == null || priceText.isEmpty()) {
            return null;
        }

        try {
            // Remove all non-numeric characters except decimal separators
            String cleaned = priceText.replaceAll("[^0-9.,]", "").trim();

            if (cleaned.isEmpty()) {
                return null;
            }

            // Handle different formats
            if (cleaned.contains(",") && cleaned.contains(".")) {
                // Format: 1,234.56 or 1.234,56
                int lastComma = cleaned.lastIndexOf(",");
                int lastDot = cleaned.lastIndexOf(".");

                if (lastDot > lastComma) {
                    // US format: 1,234.56
                    cleaned = cleaned.replace(",", "");
                } else {
                    // EU format: 1.234,56
                    cleaned = cleaned.replace(".", "").replace(",", ".");
                }
            } else if (cleaned.contains(",")) {
                // Check if comma is decimal separator (exactly 2 digits after)
                int commaPos = cleaned.lastIndexOf(",");
                if (commaPos == cleaned.length() - 3) {
                    // Likely decimal: 12,99
                    cleaned = cleaned.replace(",", ".");
                } else {
                    // Likely thousands: 1,234
                    cleaned = cleaned.replace(",", "");
                }
            }

            double price = Double.parseDouble(cleaned);

            // Sanity check: price should be reasonable (0.01 to 999,999)
            if (price < 0.01 || price > 999999) {
                System.err.println("Unreasonable price detected: " + price);
                return null;
            }

            return price;

        } catch (NumberFormatException e) {
            System.err.println("Failed to parse price: " + priceText);
            return null;
        }
    }

    private boolean isErrorPage(String title) {
        if (title == null || title.isEmpty()) {
            return true;
        }
        String lowerTitle = title.toLowerCase();
        return lowerTitle.contains("404") ||
                lowerTitle.contains("page not found") ||
                lowerTitle.contains("error") ||
                lowerTitle.contains("sorry");
    }

    private String getRandomUserAgent() {
        return USER_AGENTS[random.nextInt(USER_AGENTS.length)];
    }

    private Map<String, String> createHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Language", "en-US,en;q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("DNT", "1");
        headers.put("Connection", "keep-alive");
        headers.put("Upgrade-Insecure-Requests", "1");
        return headers;
    }

    private void randomDelay(int minMs, int maxMs) {
        try {
            int delay = minMs + random.nextInt(maxMs - minMs);
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Result class for better return values
    public static class PriceResult {
        private final boolean success;
        private final Double price;
        private final String productTitle;
        private final String availability;
        private final String errorMessage;

        private PriceResult(boolean success, Double price, String productTitle,
                            String availability, String errorMessage) {
            this.success = success;
            this.price = price;
            this.productTitle = productTitle;
            this.availability = availability;
            this.errorMessage = errorMessage;
        }

        public static PriceResult success(Double price, String productTitle, String availability) {
            return new PriceResult(true, price, productTitle, availability, null);
        }

        public static PriceResult error(String message) {
            return new PriceResult(false, null, null, null, message);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public Double getPrice() { return price; }
        public String getProductTitle() { return productTitle; }
        public String getAvailability() { return availability; }
        public String getErrorMessage() { return errorMessage; }

        @Override
        public String toString() {
            if (success) {
                return String.format("Success: $%.2f - %s (%s)",
                        price, productTitle, availability);
            } else {
                return "Error: " + errorMessage;
            }
        }
    }
}