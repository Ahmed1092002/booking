package com.example.booking.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HotelImageRepository extends JpaRepository<HotelImage, Long> {

    List<HotelImage> findByHotelIdOrderByDisplayOrderAsc(Long hotelId);

    Optional<HotelImage> findByHotelIdAndIsPrimaryTrue(Long hotelId);

    @Modifying
    @Query("UPDATE HotelImage h SET h.isPrimary = false WHERE h.hotel.id = :hotelId")
    void clearPrimaryForHotel(Long hotelId);

    void deleteByHotelId(Long hotelId);
}
