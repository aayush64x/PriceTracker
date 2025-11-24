package com.project.PriceTracker.repository;

import com.project.PriceTracker.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Users findByEmail(String Email);
    Optional<Users> findByUserId(int Id);

}


