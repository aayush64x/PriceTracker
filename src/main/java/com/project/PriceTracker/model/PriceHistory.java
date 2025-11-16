package com.project.PriceTracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int priceHistoryId;

    @ManyToOne
    @JoinColumn(name="ASIN", referencedColumnName = "ASIN", nullable = false)
    private Product product;

    @Column(name = "date_added", columnDefinition = "TIMESTAMP")
    @CreationTimestamp
    private Timestamp dateAdded;

    private double productPrice;
}