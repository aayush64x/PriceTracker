package com.project.PriceTracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTemporary {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int userTemporaryId;

    @Column(nullable = false, unique = true)
    private String email;

}
