package com.project.PriceTracker.service;

import com.project.PriceTracker.model.Product;
import com.microsoft.playwright.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScraperService {

    private final String wsEndpoint;

    public ScraperService(@Value("${brightdata.wsEndpoint}") String wsEndpoint) {
        this.wsEndpoint = wsEndpoint;
    }

    public List<Product> scrapeAmazon(String keyword) {
        List<Product> products = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );
            Page page = browser.newPage();

            // Navigate to Amazon and search
            page.navigate("https://www.amazon.com");
            page.fill("#twotabsearchtextbox", keyword);
            page.click("#nav-search-submit-button");

            // Wait for search results
            page.waitForSelector("div.s-main-slot");

            // Scroll a few times to load more
            for (int i = 0; i < 5; i++) {
                page.mouse().wheel(0, 1000);
                page.waitForTimeout(1000);
            }

            // Parse HTML content
            String html = page.content();
            Document doc = Jsoup.parse(html);
            Elements items = doc.select("div.s-main-slot div[data-component-type='s-search-result']");

            System.out.println("Found " + items.size() + " results. Showing first 10.");

            int limit = Math.min(items.size(), 10);
            for (int i = 0; i < limit; i++) {
                Element item = items.get(i);
                Product product = new Product();

                // ASIN
                String asin = item.attr("data-asin");
                product.setASIN(asin);

                // Name
                Element titleEl = item.selectFirst("h2 span");
                product.setProductName(titleEl != null ? titleEl.text() : "N/A");

                // Link - build canonical Amazon link
                String href = "";
                Element linkEl = item.selectFirst("h2 a");
                if (linkEl != null) {
                    String rawHref = linkEl.attr("href");
                    if (asin != null && !asin.isEmpty()) {
                        href = "https://www.amazon.com/dp/" + asin;
                    } else if (rawHref != null && !rawHref.isEmpty()) {
                        href = rawHref.startsWith("http") ? rawHref : "https://www.amazon.com" + rawHref;
                    }
                }
                product.setLink(href);

                // Price
                Element priceEl = item.selectFirst("span.a-price > span.a-offscreen");
                Double price = null;
                if (priceEl != null) {
                    String priceText = priceEl.text().replaceAll("[^0-9.]", "");
                    if (!priceText.isEmpty()) {
                        try {
                            price = Double.parseDouble(priceText);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                product.setProductPrice(price);

                // Image
                Element imgEl = item.selectFirst("img.s-image");
                product.setImageURL(imgEl != null ? imgEl.attr("src") : "");

                // Category
                Element categoryEl = item.selectFirst("span.a-size-base.a-color-base");
                product.setCategory(categoryEl != null ? categoryEl.text() : "");

                // Product Group
                product.setProductGroup("");

                // Timestamp
                product.setTimestamp(new Timestamp(System.currentTimeMillis()));

                products.add(product);

                // Debug print
                System.out.println((i + 1) + ". " + product.getProductName());
                System.out.println("   ASIN: " + asin);
                System.out.println("   Link: " + product.getLink());
                System.out.println("   Price: " + product.getProductPrice());
                System.out.println("   Image: " + product.getImageURL());
                System.out.println("--------------------------------------------");
            }

            browser.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }
}
