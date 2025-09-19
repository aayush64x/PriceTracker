package com.project.PriceTracker.scraper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Proxy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


public class WebScraper {
    public static void main(String[] args) {
        String APIKey = "27f5176e3cb0c8ecefedb35107f7778a";


        try (Playwright playwright = Playwright.create()){
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );
            Page page = browser.newPage();
            page.navigate("https://amazon.com");


            page.fill("input#twotabsearchtextbox", "iphone");
            page.click("input#nav-search-submit-button");
            page.waitForSelector("div.s-main-slot div[data-component-type='s-search-result']");

            try{
                Thread.sleep(6000);
            }catch (InterruptedException e){
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            String html = page.content();
            Document doc = Jsoup.parse(html);

            /*try{
                Files.write(Paths.get("amazon-results.html"), html.getBytes());
                System.out.println("Saved HTML to amazon-results.html");
            } catch(IOException e){
                e.printStackTrace();
            }*/


            System.out.println("Title: "+page.title());



            Elements products = doc.select("div[data-component-type=s-search-result]");
            for (Element product: products){
                 String title = product.select("div h2 span").text();
                 String productName = product.select("a h2 span").text();

                 System.out.println(title+" "+productName);
            }
            browser.close();
        }
    }
}






