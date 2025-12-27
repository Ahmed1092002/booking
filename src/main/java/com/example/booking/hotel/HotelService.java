package com.example.booking.hotel;

import com.example.booking.user.User;
import com.example.booking.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final com.example.booking.booking.BookingRepository bookingRepository;

    public HotelService(HotelRepository hotelRepository, RoomRepository roomRepository, UserRepository userRepository,
            com.example.booking.booking.BookingRepository bookingRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Hotel createHotel(Long sellerId, Hotel hotel) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        // Basic check could be added here to ensure user has ROLE_SELLER
        hotel.setSeller(seller);
        return hotelRepository.save(hotel);
    }

    @Transactional
    public Room addRoomToHotel(Long sellerId, Long hotelId, Room room) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        if (!hotel.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Unauthorized: You do not own this hotel");
        }

        room.setHotel(hotel);
        room.setAvailable(true);
        return roomRepository.save(room);
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    @Cacheable(value = "hotelsByCity", key = "#city")
    public List<Hotel> getHotelsByCity(String city) {
        return hotelRepository.findByCityIgnoreCase(city);
    }

    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id).orElseThrow(
                () -> new com.example.booking.exception.ResourceNotFoundException("Hotel not found with id: " + id));
    }

    public List<Room> getRoomsByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    @Transactional
    public Hotel updateHotel(Long sellerId, Long hotelId, Hotel updatedHotel) {
        Hotel existingHotel = getHotelById(hotelId);

        if (!existingHotel.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Unauthorized: You do not own this hotel");
        }

        if (updatedHotel.getName() != null)
            existingHotel.setName(updatedHotel.getName());
        if (updatedHotel.getCity() != null)
            existingHotel.setCity(updatedHotel.getCity());
        if (updatedHotel.getAddress() != null)
            existingHotel.setAddress(updatedHotel.getAddress());
        if (updatedHotel.getGoogleMapUrl() != null)
            existingHotel.setGoogleMapUrl(updatedHotel.getGoogleMapUrl());
        if (updatedHotel.getAmenities() != null)
            existingHotel.setAmenities(updatedHotel.getAmenities());

        return hotelRepository.save(existingHotel);
    }

    @Transactional
    public void deleteHotel(Long sellerId, Long hotelId) {
        Hotel existingHotel = getHotelById(hotelId);

        if (!existingHotel.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Unauthorized: You do not own this hotel");
        }

        boolean hasBookings = bookingRepository.existsByRoomHotelId(hotelId);
        if (hasBookings) {
            throw new RuntimeException("Cannot delete hotel: there are existing bookings associated with it.");
        }

        hotelRepository.delete(existingHotel);
    }

    public List<Hotel> getHotelsBySeller(Long sellerId) {
        return hotelRepository.findBySellerId(sellerId);
    }

    public com.example.booking.hotel.dto.SellerStatsResponseDto getSellerStats(Long sellerId) {
        List<Hotel> sellerHotels = hotelRepository.findBySellerId(sellerId);

        int totalHotels = sellerHotels.size();
        int totalReviews = sellerHotels.stream().mapToInt(Hotel::getTotalReviews).sum();

        java.math.BigDecimal averageRating = java.math.BigDecimal.ZERO;
        if (totalHotels > 0) {
            double avg = sellerHotels.stream()
                    .map(Hotel::getAverageRating)
                    .filter(java.util.Objects::nonNull)
                    .mapToDouble(java.math.BigDecimal::doubleValue)
                    .average()
                    .orElse(0.0);
            averageRating = java.math.BigDecimal.valueOf(avg);
        }

        long totalBookings = bookingRepository.countByRoomHotelSellerId(sellerId);
        java.math.BigDecimal totalRevenue = bookingRepository.sumRevenueBySellerId(sellerId);

        if (totalRevenue == null) {
            totalRevenue = java.math.BigDecimal.ZERO;
        }

        return com.example.booking.hotel.dto.SellerStatsResponseDto.builder()
                .totalHotels(totalHotels)
                .totalBookings((int) totalBookings)
                .totalRevenue(totalRevenue)
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .build();
    }
}
