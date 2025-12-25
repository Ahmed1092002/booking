package com.example.booking.analytics;

import com.example.booking.booking.Booking;
import com.example.booking.booking.BookingRepository;
import com.example.booking.hotel.Hotel;
import com.example.booking.hotel.HotelRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;

    public DashboardService(HotelRepository hotelRepository, BookingRepository bookingRepository) {
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
    }

    public Map<String, Object> getSellerDashboard(Long sellerId) {
        Map<String, Object> stats = new HashMap<>();

        // 1. Get all hotels by this seller
        List<Hotel> hotels = hotelRepository.findBySellerId(sellerId);
        List<Long> hotelIds = hotels.stream().map(Hotel::getId).toList();

        // 2. Mocking complex aggregation (In a real app, use @Query for SUM/COUNT)
        // Here we iterate for demonstration of logic
        List<Booking> allBookings = bookingRepository.findAll(); // Ineffecient, but simple for demo

        List<Booking> sellerBookings = allBookings.stream()
                .filter(b -> hotelIds.contains(b.getRoom().getHotel().getId()))
                .toList();

        BigDecimal totalRevenue = sellerBookings.stream()
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalRevenue", totalRevenue);
        stats.put("totalBookings", sellerBookings.size());
        stats.put("totalHotels", hotels.size());

        // Simple demographic check
        long familyBookings = sellerBookings.stream()
                .filter(b -> b.getRoom().getCapacity() > 2)
                .count();

        stats.put("familyBookings", familyBookings);
        stats.put("singleCoupleBookings", sellerBookings.size() - familyBookings);

        return stats;
    }
}
