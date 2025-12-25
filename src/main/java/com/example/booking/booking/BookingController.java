package com.example.booking.booking;

import com.example.booking.booking.dto.BookingResponseDto;
import com.example.booking.booking.dto.CreateBookingRequest;
import com.example.booking.security.CurrentUserService;
import com.example.booking.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final CurrentUserService currentUserService;

    public BookingController(BookingService bookingService, BookingMapper bookingMapper,
            CurrentUserService currentUserService) {
        this.bookingService = bookingService;
        this.bookingMapper = bookingMapper;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponseDto> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        User booker = currentUserService.getCurrentUser();
        Booking booking = bookingService.createBooking(
                booker.getId(),
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate());

        return new ResponseEntity<>(bookingMapper.toResponseDto(booking), HttpStatus.CREATED);
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponseDto>> getMyBookings() {
        User booker = currentUserService.getCurrentUser();
        List<Booking> bookings = bookingService.getBookingsByBooker(booker.getId());
        List<BookingResponseDto> dtos = bookings.stream()
                .map(bookingMapper::toResponseDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }
}
