package com.example.booking.hotel;

import com.example.booking.hotel.dto.CreateHotelRequest;
import com.example.booking.hotel.dto.CreateRoomRequest;
import com.example.booking.hotel.dto.HotelResponseDto;
import com.example.booking.hotel.dto.HotelSearchRequest;
import com.example.booking.hotel.dto.HotelSearchResponse;
import com.example.booking.hotel.dto.RoomResponseDto;
import com.example.booking.security.CurrentUserService;
import com.example.booking.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;
    private final HotelMapper hotelMapper;
    private final CurrentUserService currentUserService;
    private final HotelSearchService hotelSearchService;

    public HotelController(HotelService hotelService,
            HotelMapper hotelMapper,
            CurrentUserService currentUserService,
            HotelSearchService hotelSearchService) {
        this.hotelService = hotelService;
        this.hotelMapper = hotelMapper;
        this.currentUserService = currentUserService;
        this.hotelSearchService = hotelSearchService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<HotelResponseDto> createHotel(@Valid @RequestBody CreateHotelRequest request) {
        User seller = currentUserService.getCurrentUser();
        Hotel hotel = hotelService.createHotel(seller.getId(), hotelMapper.toEntity(request, seller));
        return new ResponseEntity<>(hotelMapper.toResponseDto(hotel), HttpStatus.CREATED);
    }

    @PostMapping("/{hotelId}/rooms")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<RoomResponseDto> addRoom(
            @PathVariable Long hotelId,
            @Valid @RequestBody CreateRoomRequest request) {

        User seller = currentUserService.getCurrentUser();
        Hotel hotel = hotelService.getHotelById(hotelId);
        Room room = hotelService.addRoomToHotel(seller.getId(), hotelId, hotelMapper.toEntity(request, hotel));
        return new ResponseEntity<>(hotelMapper.toResponseDto(room), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<HotelResponseDto>> getAllHotels() {
        List<Hotel> hotels = hotelService.getAllHotels();
        List<HotelResponseDto> dtos = hotels.stream()
                .map(hotelMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<HotelResponseDto>> searchByCity(@RequestParam String city) {
        List<Hotel> hotels = hotelService.getHotelsByCity(city);
        List<HotelResponseDto> dtos = hotels.stream()
                .map(hotelMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/search/advanced")
    public ResponseEntity<List<HotelSearchResponse>> advancedSearch(@RequestBody HotelSearchRequest request) {
        List<HotelSearchResponse> results = hotelSearchService.advancedSearch(request);
        return ResponseEntity.ok(results);
    }
}
