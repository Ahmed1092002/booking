package com.example.booking.hotel;

import com.example.booking.hotel.dto.CreateHotelRequest;
import com.example.booking.hotel.dto.CreateRoomRequest;
import com.example.booking.hotel.dto.HotelResponseDto;
import com.example.booking.hotel.dto.RoomResponseDto;
import com.example.booking.user.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HotelMapper {

    public Hotel toEntity(CreateHotelRequest request, User seller) {
        if (request == null) {
            return null;
        }

        Hotel hotel = new Hotel();
        hotel.setName(request.getName());
        hotel.setCity(request.getCity());
        hotel.setAddress(request.getAddress());
        hotel.setGoogleMapUrl(request.getGoogleMapUrl());
        hotel.setAmenities(request.getAmenities());
        hotel.setSeller(seller);
        return hotel;
    }

    public HotelResponseDto toResponseDto(Hotel hotel) {
        if (hotel == null) {
            return null;
        }

        HotelResponseDto dto = new HotelResponseDto();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setCity(hotel.getCity());
        dto.setAddress(hotel.getAddress());
        dto.setGoogleMapUrl(hotel.getGoogleMapUrl());
        dto.setAmenities(hotel.getAmenities());
        dto.setSellerId(hotel.getSeller().getId());
        dto.setSellerName(hotel.getSeller().getFullName());
        return dto;
    }

    public Room toEntity(CreateRoomRequest request, Hotel hotel) {
        if (request == null) {
            return null;
        }

        Room room = new Room();
        room.setName(request.getName());
        room.setPricePerNight(request.getPricePerNight());
        room.setCapacity(request.getCapacity());
        room.setViewType(request.getViewType());
        room.setHasKitchen(request.getHasKitchen() != null ? request.getHasKitchen() : false);
        room.setAvailable(true);
        room.setHotel(hotel);
        return room;
    }

    public RoomResponseDto toResponseDto(Room room) {
        if (room == null) {
            return null;
        }

        RoomResponseDto dto = new RoomResponseDto();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setPricePerNight(room.getPricePerNight());
        dto.setCapacity(room.getCapacity());
        dto.setViewType(room.getViewType());
        dto.setHasKitchen(room.isHasKitchen());
        dto.setIsAvailable(room.isAvailable());
        dto.setHotelId(room.getHotel().getId());
        dto.setHotelName(room.getHotel().getName());
        return dto;
    }
}
