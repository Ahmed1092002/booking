package com.example.booking.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SeasonalPricingRepository extends JpaRepository<SeasonalPricing, Long> {

    List<SeasonalPricing> findByRoomId(Long roomId);

    @Query("SELECT s FROM SeasonalPricing s WHERE s.room.id = :roomId " +
            "AND :date BETWEEN s.startDate AND s.endDate")
    Optional<SeasonalPricing> findByRoomIdAndDate(Long roomId, LocalDate date);

    @Query("SELECT s FROM SeasonalPricing s WHERE s.room.id = :roomId " +
            "AND s.startDate <= :endDate AND s.endDate >= :startDate")
    List<SeasonalPricing> findOverlappingSeasonalPricing(Long roomId, LocalDate startDate, LocalDate endDate);
}
