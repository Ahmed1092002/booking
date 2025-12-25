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

    public HotelService(HotelRepository hotelRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
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
}
