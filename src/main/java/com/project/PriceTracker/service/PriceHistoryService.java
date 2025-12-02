package com.project.PriceTracker.service;

import com.project.PriceTracker.dto.PriceHistoryDTO;
import com.project.PriceTracker.model.PriceHistory;
import com.project.PriceTracker.repository.PriceHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Constructor injection - no @Autowired needed in newer Spring versions
    public PriceHistoryService(PriceHistoryRepository priceHistoryRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
    }

    public List<PriceHistoryDTO> getPriceHistory(String asin) {
        List<PriceHistory> priceHistories = priceHistoryRepository
                .findByProduct_ASIN(asin.trim().toUpperCase());

        // Convert to DTOs and return
        return priceHistories.stream()
                .map(ph -> new PriceHistoryDTO(
                        ph.getDateAdded().toLocalDateTime().format(DATE_FORMATTER),
                        ph.getProductPrice()
                ))
                .toList();
    }
}
