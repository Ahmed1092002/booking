package com.example.booking.promotion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoyaltyPointsRepository extends JpaRepository<LoyaltyPoints, Long> {

    Optional<LoyaltyPoints> findByUserId(Long userId);
}
