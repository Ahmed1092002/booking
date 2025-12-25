package com.example.booking.booking.dto;

import com.example.booking.booking.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {
    private Long id;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;
    private BookingStatus status;

    // Nested room info
    private Long roomId;
    private String roomName;

    // Nested hotel info
    private Long hotelId;
    private String hotelName;
    private String hotelCity;

    // Booker info
    private Long bookerId;
    private String bookerName;
}
