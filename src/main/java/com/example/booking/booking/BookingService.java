package com.example.booking.booking;

import com.example.booking.hotel.Room;
import com.example.booking.hotel.RoomRepository;
import com.example.booking.user.User;
import com.example.booking.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Booking createBooking(Long bookerId, Long roomId, java.time.LocalDate checkInDate,
            java.time.LocalDate checkOutDate) {
        // 1. Validate Room existence
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.isAvailable()) {
            throw new RuntimeException("Room is strictly unavailable (closed)");
        }

        // 2. Concurrency Check: Check for overlapping bookings
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                roomId,
                checkInDate,
                checkOutDate);

        if (!overlaps.isEmpty()) {
            throw new RuntimeException("Room is already booked for these dates!");
        }

        // 3. User validation
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4. Calculate Price
        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (days < 1)
            throw new RuntimeException("Booking must be at least 1 night");

        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(days));

        // 5. Create Booking
        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setRoom(room);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING); // Default status

        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsByBooker(Long bookerId) {
        return bookingRepository.findByBookerId(bookerId);
    }

    public List<Booking> getBookingsForUser(Long userId) {
        return bookingRepository.findByBookerId(userId);
    }
}
