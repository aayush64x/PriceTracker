package com.project.PriceTracker.repository;

import com.project.PriceTracker.model.UserTemporary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTemporaryRepository extends JpaRepository<UserTemporary, Integer> {
    Optional<UserTemporary> findByEmail(String email);
}
