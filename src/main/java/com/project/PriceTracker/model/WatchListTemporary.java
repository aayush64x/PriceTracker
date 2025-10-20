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
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"asin", "userTemporaryId"})
)
public class WatchListTemporary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int watchListId;

    @ManyToOne
    @JoinColumn(name="productId")
    private Product product;

    @ManyToOne
    @JoinColumn(name="userTemporaryId")
    private UserTemporary userTemporary;

    @Column(name = "date_added", columnDefinition = "TIMESTAMP")
    @CreationTimestamp
    private Timestamp dateAdded;

    private Double targetPrice;
}