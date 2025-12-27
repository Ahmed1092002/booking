package com.example.booking.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

        List<Booking> findByBookerId(Long bookerId);

        // Check for overlapping bookings for a room
        @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND " +
                        "(b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate) AND " +
                        "b.status <> 'CANCELLED'")
        List<Booking> findOverlappingBookings(@Param("roomId") Long roomId,
                        @Param("checkInDate") LocalDate checkInDate,
                        @Param("checkOutDate") LocalDate checkOutDate);

        boolean existsByRoomHotelId(Long hotelId);
}
