package com.example.booking.booking;

import com.example.booking.booking.dto.BookingResponseDto;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponseDto toResponseDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus());

        // Room info
        dto.setRoomId(booking.getRoom().getId());
        dto.setRoomName(booking.getRoom().getName());

        // Hotel info
        dto.setHotelId(booking.getRoom().getHotel().getId());
        dto.setHotelName(booking.getRoom().getHotel().getName());
        dto.setHotelCity(booking.getRoom().getHotel().getCity());

        // Booker info
        dto.setBookerId(booking.getBooker().getId());
        dto.setBookerName(booking.getBooker().getFullName());

        return dto;
    }
}
