package com.example.booking.booking;

import com.example.booking.booking.dto.BookingResponseDto;
import com.example.booking.booking.dto.CreateBookingRequest;
import com.example.booking.security.CurrentUserService;
import com.example.booking.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Hotel room booking management endpoints")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Create new booking", description = "Book a hotel room for specified check-in and check-out dates. Validates room availability and calculates total price.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - Room not available or invalid dates"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
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
    @Operation(summary = "Get my bookings", description = "Retrieve all bookings made by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<List<BookingResponseDto>> getMyBookings() {
        User booker = currentUserService.getCurrentUser();
        List<Booking> bookings = bookingService.getBookingsByBooker(booker.getId());
        List<BookingResponseDto> dtos = bookings.stream()
                .map(bookingMapper::toResponseDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{bookingId}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel booking", description = "Cancel an existing booking. Only the user who made the booking can cancel it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Booking cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - Booking already cancelled or cannot be cancelled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not own the booking"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<Void> cancelBooking(
            @io.swagger.v3.oas.annotations.Parameter(description = "Booking ID") @PathVariable Long bookingId) {
        User booker = currentUserService.getCurrentUser();
        bookingService.cancelBooking(booker.getId(), bookingId);
        return ResponseEntity.noContent().build();
    }
}
