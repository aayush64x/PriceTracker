package com.project.PriceTracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_temporary")  //
public class UserTemporary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_temporary_id")
    private int userTemporaryId;

    @Column(nullable = false, unique = true)
    private String email;

}
