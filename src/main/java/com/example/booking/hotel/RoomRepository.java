package com.example.booking.hotel;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);

    // Find rooms that fit a certain capacity
    List<Room> findByHotelIdAndCapacityGreaterThanEqual(Long hotelId, Integer capacity);
}
