package com.example.booking.hotel;

import com.example.booking.booking.BookingRepository;
import com.example.booking.booking.BookingStatus;
import com.example.booking.hotel.dto.HotelSearchRequest;
import com.example.booking.hotel.dto.HotelSearchResponse;
import com.example.booking.hotel.dto.RoomResponseDto;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelSearchService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final HotelMapper hotelMapper;

    public HotelSearchService(HotelRepository hotelRepository,
            RoomRepository roomRepository,
            BookingRepository bookingRepository,
            HotelMapper hotelMapper) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.hotelMapper = hotelMapper;
    }

    @Transactional(readOnly = true)
    public List<HotelSearchResponse> advancedSearch(HotelSearchRequest request) {
        // Get base hotels by city
        List<Hotel> hotels = request.getCity() != null && !request.getCity().isBlank()
                ? hotelRepository.findByCity(request.getCity())
                : hotelRepository.findAll(getSortOrder(request));

        // Apply filters and build response
        return hotels.stream()
                .map(hotel -> buildSearchResponse(hotel, request))
                .filter(response -> matchesSearchCriteria(response, request))
                .sorted((h1, h2) -> compareHotels(h1, h2, request))
                .collect(Collectors.toList());
    }

    private HotelSearchResponse buildSearchResponse(Hotel hotel, HotelSearchRequest request) {
        HotelSearchResponse response = new HotelSearchResponse();
        response.setId(hotel.getId());
        response.setName(hotel.getName());
        response.setCity(hotel.getCity());
        response.setAddress(hotel.getAddress());
        response.setGoogleMapUrl(hotel.getGoogleMapUrl());
        response.setAmenities(hotel.getAmenities());
        response.setSellerId(hotel.getSeller().getId());
        response.setSellerName(hotel.getSeller().getFullName());
        response.setAverageRating(hotel.getAverageRating());
        response.setTotalReviews(hotel.getTotalReviews());

        // Set Images
        if (hotel.getImages() != null) {
            response.setImages(hotel.getImages().stream()
                    .sorted(java.util.Comparator.comparingInt(com.example.booking.image.HotelImage::getDisplayOrder))
                    .map(img -> new com.example.booking.image.dto.ImageResponseDto(
                            img.getId(),
                            img.getImageUrl(),
                            img.getIsPrimary(),
                            img.getDisplayOrder(),
                            img.getUploadedAt()))
                    .collect(Collectors.toList()));
        }

        // Get rooms for this hotel
        List<Room> rooms = roomRepository.findByHotelId(hotel.getId());

        // Filter rooms by criteria
        List<Room> filteredRooms = rooms.stream()
                .filter(room -> matchesRoomCriteria(room, request))
                .collect(Collectors.toList());

        // Convert to DTOs
        List<RoomResponseDto> roomDtos = filteredRooms.stream()
                .map(hotelMapper::toResponseDto)
                .collect(Collectors.toList());

        response.setRooms(roomDtos);
        response.setAvailableRooms(roomDtos.size());

        // Calculate min room price
        BigDecimal minPrice = filteredRooms.stream()
                .map(Room::getPricePerNight)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        response.setMinRoomPrice(minPrice);

        return response;
    }

    private boolean matchesRoomCriteria(Room room, HotelSearchRequest request) {
        // Check capacity
        if (request.getMinCapacity() != null && room.getCapacity() < request.getMinCapacity()) {
            return false;
        }

        // Check price range
        if (request.getMinPrice() != null && room.getPricePerNight().compareTo(request.getMinPrice()) < 0) {
            return false;
        }
        if (request.getMaxPrice() != null && room.getPricePerNight().compareTo(request.getMaxPrice()) > 0) {
            return false;
        }

        // Check availability if dates provided
        if (request.getCheckInDate() != null && request.getCheckOutDate() != null) {
            return isRoomAvailable(room.getId(), request.getCheckInDate(), request.getCheckOutDate());
        }

        return room.isAvailable();
    }

    private boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<com.example.booking.booking.Booking> overlappingBookings = bookingRepository
                .findOverlappingBookings(roomId, checkIn, checkOut);

        // Filter out cancelled bookings
        return overlappingBookings.stream()
                .noneMatch(booking -> booking.getStatus() != BookingStatus.CANCELLED);
    }

    private boolean matchesSearchCriteria(HotelSearchResponse response, HotelSearchRequest request) {
        // Must have at least one available room
        if (response.getAvailableRooms() == 0) {
            return false;
        }

        // Check amenities
        if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
            if (response.getAmenities() == null ||
                    !response.getAmenities().containsAll(request.getAmenities())) {
                return false;
            }
        }

        return true;
    }

    private int compareHotels(HotelSearchResponse h1, HotelSearchResponse h2, HotelSearchRequest request) {
        if (request.getSortBy() == null) {
            return 0;
        }

        int comparison = 0;
        switch (request.getSortBy().toLowerCase()) {
            case "price":
                comparison = h1.getMinRoomPrice().compareTo(h2.getMinRoomPrice());
                break;
            case "rating":
                BigDecimal rating1 = h1.getAverageRating() != null ? h1.getAverageRating() : BigDecimal.ZERO;
                BigDecimal rating2 = h2.getAverageRating() != null ? h2.getAverageRating() : BigDecimal.ZERO;
                comparison = rating1.compareTo(rating2);
                break;
            case "name":
                comparison = h1.getName().compareTo(h2.getName());
                break;
            default:
                comparison = 0;
        }

        // Apply sort order
        if ("desc".equalsIgnoreCase(request.getSortOrder())) {
            comparison = -comparison;
        }

        return comparison;
    }

    private Sort getSortOrder(HotelSearchRequest request) {
        if (request.getSortBy() == null) {
            return Sort.by("name").ascending();
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortOrder())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        String sortField = switch (request.getSortBy().toLowerCase()) {
            case "rating" -> "averageRating";
            case "name" -> "name";
            default -> "name";
        };

        return Sort.by(direction, sortField);
    }
}
