package com.example.booking.booking;

import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.ForbiddenException;
import com.example.booking.exception.ResourceNotFoundException;
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
        // 1. Validate Room existencia
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.isAvailable()) {
            throw new BadRequestException("Room is strictly unavailable (closed)");
        }

        // 2. Concurrency Check: Check for overlapping bookings
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                roomId,
                checkInDate,
                checkOutDate);

        if (!overlaps.isEmpty()) {
            throw new BadRequestException("Room is already booked for these dates!");
        }

        // 3. User validation
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 4. Calculate Price
        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (days < 1)
            throw new BadRequestException("Booking must be at least 1 night");

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

    @Transactional
    public void cancelBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getBooker().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed booking");
        }

        // Logic to check connection/time constraints could be added here
        // e.g., cannot cancel if check-in is today

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(java.time.LocalDateTime.now());
        bookingRepository.save(booking);
    }
}
