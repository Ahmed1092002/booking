package com.example.booking.hotel;

import com.example.booking.hotel.dto.CreateHotelRequest;
import com.example.booking.hotel.dto.CreateRoomRequest;
import com.example.booking.hotel.dto.HotelResponseDto;
import com.example.booking.hotel.dto.HotelSearchRequest;
import com.example.booking.hotel.dto.HotelSearchResponse;
import com.example.booking.hotel.dto.RoomResponseDto;
import com.example.booking.security.CurrentUserService;
import com.example.booking.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/hotels")
@Tag(name = "Hotels", description = "Hotel and room management endpoints")
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
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Create new hotel", description = "Create a new hotel listing. Only sellers can create hotels.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Hotel created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HotelResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request body"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have SELLER role")
        })
        public ResponseEntity<HotelResponseDto> createHotel(@Valid @RequestBody CreateHotelRequest request) {
                User seller = currentUserService.getCurrentUser();
                Hotel hotel = hotelService.createHotel(seller.getId(), hotelMapper.toEntity(request, seller));
                return new ResponseEntity<>(hotelMapper.toResponseDto(hotel), HttpStatus.CREATED);
        }

        @PostMapping("/{hotelId}/rooms")
        @PreAuthorize("hasRole('SELLER')")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Add room to hotel", description = "Add a new room to an existing hotel. Only the hotel owner can add rooms.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Room added successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoomResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request body"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not the hotel owner"),
                        @ApiResponse(responseCode = "404", description = "Hotel not found")
        })
        public ResponseEntity<RoomResponseDto> addRoom(
                        @Parameter(description = "Hotel ID", required = true) @PathVariable Long hotelId,
                        @Valid @RequestBody CreateRoomRequest request) {

                User seller = currentUserService.getCurrentUser();
                Hotel hotel = hotelService.getHotelById(hotelId);
                Room room = hotelService.addRoomToHotel(seller.getId(), hotelId, hotelMapper.toEntity(request, hotel));
                return new ResponseEntity<>(hotelMapper.toResponseDto(room), HttpStatus.CREATED);
        }

        @GetMapping
        @Operation(summary = "Get all hotels", description = "Retrieve a list of all hotels in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Hotels retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HotelResponseDto.class)))
        })
        public ResponseEntity<List<HotelResponseDto>> getAllHotels() {
                List<Hotel> hotels = hotelService.getAllHotels();
                List<HotelResponseDto> dtos = hotels.stream()
                                .map(hotelMapper::toResponseDto)
                                .toList();
                return ResponseEntity.ok(dtos);
        }

        @GetMapping("/search")
        @Operation(summary = "Search hotels by city", description = "Find all hotels in a specific city")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Search completed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HotelResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid city parameter")
        })
        public ResponseEntity<List<HotelResponseDto>> searchByCity(
                        @Parameter(description = "City name to search for", required = true, example = "Cairo") @RequestParam String city) {
                List<Hotel> hotels = hotelService.getHotelsByCity(city);
                List<HotelResponseDto> dtos = hotels.stream()
                                .map(hotelMapper::toResponseDto)
                                .toList();
                return ResponseEntity.ok(dtos);
        }

        @PostMapping("/search/advanced")
        @Operation(summary = "Advanced hotel search", description = "Search hotels with advanced filters including price range, amenities, capacity, dates, and sorting options")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Search completed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HotelSearchResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid search criteria")
        })
        public ResponseEntity<List<HotelSearchResponse>> advancedSearch(@RequestBody HotelSearchRequest request) {
                List<HotelSearchResponse> results = hotelSearchService.advancedSearch(request);
                return ResponseEntity.ok(results);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get hotel by ID", description = "Retrieve a single hotel by its unique ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Hotel found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HotelResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Hotel not found")
        })
        public ResponseEntity<HotelResponseDto> getHotelById(@PathVariable Long id) {
                Hotel hotel = hotelService.getHotelById(id);
                return ResponseEntity.ok(hotelMapper.toResponseDto(hotel));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasRole('SELLER')")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Update hotel", description = "Update an existing hotel. Only the owner can update.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Hotel updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HotelResponseDto.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
                        @ApiResponse(responseCode = "404", description = "Hotel not found")
        })
        public ResponseEntity<HotelResponseDto> updateHotel(
                        @PathVariable Long id,
                        @Valid @RequestBody com.example.booking.hotel.dto.UpdateHotelRequest request) {
                User seller = currentUserService.getCurrentUser();
                // Manually map DTO to Hotel entity since Mapper might not support UpdateDTO yet
                Hotel hotelUpdate = new Hotel();
                hotelUpdate.setName(request.getName());
                hotelUpdate.setCity(request.getCity());
                hotelUpdate.setAddress(request.getAddress());
                hotelUpdate.setGoogleMapUrl(request.getGoogleMapUrl());
                hotelUpdate.setAmenities(request.getAmenities());

                Hotel updatedHotel = hotelService.updateHotel(seller.getId(), id, hotelUpdate);
                return ResponseEntity.ok(hotelMapper.toResponseDto(updatedHotel));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('SELLER')")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Delete hotel", description = "Delete an existing hotel. Only the owner can delete.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Hotel deleted successfully"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
                        @ApiResponse(responseCode = "404", description = "Hotel not found")
        })
        public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
                User seller = currentUserService.getCurrentUser();
                hotelService.deleteHotel(seller.getId(), id);
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/seller/my-hotels")
        @PreAuthorize("hasRole('SELLER')")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Get my hotels", description = "Retrieve all hotels owned by the currently authenticated seller")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Hotels retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HotelResponseDto.class)))
        })
        public ResponseEntity<List<HotelResponseDto>> getSellerHotels() {
                User seller = currentUserService.getCurrentUser();
                List<Hotel> hotels = hotelService.getHotelsBySeller(seller.getId());
                List<HotelResponseDto> dtos = hotels.stream()
                                .map(hotelMapper::toResponseDto)
                                .toList();
                return ResponseEntity.ok(dtos);
        }

        @GetMapping("/seller/stats")
        @PreAuthorize("hasRole('SELLER')")
        @SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Get seller statistics", description = "Retrieve aggregated statistics for the seller dashboard")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Stats retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.example.booking.hotel.dto.SellerStatsResponseDto.class)))
        })
        public ResponseEntity<com.example.booking.hotel.dto.SellerStatsResponseDto> getSellerStats() {
                User seller = currentUserService.getCurrentUser();
                return ResponseEntity.ok(hotelService.getSellerStats(seller.getId()));
        }
}
