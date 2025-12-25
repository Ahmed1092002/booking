package com.example.booking.hotel;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    // Simple query methods
    List<Hotel> findByCityIgnoreCase(String city);

    List<Hotel> findByCity(String city);

    // Custom query to find hotels by seller
    List<Hotel> findBySellerId(Long sellerId);
}
