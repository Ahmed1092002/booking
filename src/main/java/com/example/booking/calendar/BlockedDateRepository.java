package com.example.booking.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BlockedDateRepository extends JpaRepository<BlockedDate, Long> {

    List<BlockedDate> findByRoomId(Long roomId);

    @Query("SELECT b FROM BlockedDate b WHERE b.room.id = :roomId " +
            "AND b.startDate <= :endDate AND b.endDate >= :startDate")
    List<BlockedDate> findOverlappingBlockedDates(Long roomId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT b FROM BlockedDate b WHERE b.room.id = :roomId " +
            "AND :date BETWEEN b.startDate AND b.endDate")
    List<BlockedDate> findByRoomIdAndDate(Long roomId, LocalDate date);
}
