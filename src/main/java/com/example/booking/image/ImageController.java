package com.example.booking.image;

import com.example.booking.image.dto.ImageResponseDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@Tag(name = "Images", description = "Hotel and room image upload and management (Cloudinary)")
public class ImageController {

    private final ImageService imageService;
    private final CurrentUserService currentUserService;

    public ImageController(ImageService imageService, CurrentUserService currentUserService) {
        this.imageService = imageService;
        this.currentUserService = currentUserService;
    }

    // Hotel Image Endpoints

    @PostMapping(value = "/hotels/{hotelId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Upload hotel image", description = "Upload an image for a hotel. Images are stored on Cloudinary. Only hotel owner can upload images.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Image uploaded successfully", content = @Content(schema = @Schema(implementation = ImageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or file too large"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the hotel owner"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<ImageResponseDto> uploadHotelImage(
            @Parameter(description = "Hotel ID") @PathVariable Long hotelId,
            @Parameter(description = "Image file (JPEG/PNG, max 5MB)") @RequestParam("image") MultipartFile file) {
        User seller = currentUserService.getCurrentUser();
        HotelImage image = imageService.uploadHotelImage(seller, hotelId, file);
        ImageResponseDto dto = new ImageResponseDto(image.getId(), image.getImageUrl(), image.getIsPrimary(),
                image.getDisplayOrder(), image.getUploadedAt());
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @DeleteMapping("/hotels/{hotelId}/{imageId}")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete hotel image", description = "Delete a hotel image. Also removes it from Cloudinary.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the hotel owner"),
            @ApiResponse(responseCode = "404", description = "Hotel or image not found")
    })
    public ResponseEntity<Void> deleteHotelImage(@PathVariable Long hotelId, @PathVariable Long imageId) {
        User seller = currentUserService.getCurrentUser();
        imageService.deleteHotelImage(seller, hotelId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/hotels/{hotelId}/{imageId}/primary")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Set primary hotel image", description = "Set an image as the primary display image for the hotel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Primary image set successfully"),
            @ApiResponse(responseCode = "404", description = "Hotel or image not found")
    })
    public ResponseEntity<Void> setHotelImageAsPrimary(@PathVariable Long hotelId, @PathVariable Long imageId) {
        User seller = currentUserService.getCurrentUser();
        imageService.setHotelImageAsPrimary(seller, hotelId, imageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hotels/{hotelId}")
    @Operation(summary = "Get hotel images", description = "Retrieve all images for a specific hotel")
    public ResponseEntity<List<ImageResponseDto>> getHotelImages(
            @Parameter(description = "Hotel ID") @PathVariable Long hotelId) {
        List<ImageResponseDto> images = imageService.getHotelImages(hotelId);
        return ResponseEntity.ok(images);
    }

    // Room Image Endpoints

    @PostMapping(value = "/rooms/{roomId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Upload room image", description = "Upload an image for a room. Only hotel owner can upload room images.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<ImageResponseDto> uploadRoomImage(
            @Parameter(description = "Room ID") @PathVariable Long roomId,
            @RequestParam("image") MultipartFile file) {
        User seller = currentUserService.getCurrentUser();
        RoomImage image = imageService.uploadRoomImage(seller, roomId, file);
        ImageResponseDto dto = new ImageResponseDto(image.getId(), image.getImageUrl(), image.getIsPrimary(),
                image.getDisplayOrder(), image.getUploadedAt());
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @DeleteMapping("/rooms/{roomId}/{imageId}")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete room image")
    public ResponseEntity<Void> deleteRoomImage(@PathVariable Long roomId, @PathVariable Long imageId) {
        User seller = currentUserService.getCurrentUser();
        imageService.deleteRoomImage(seller, roomId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/rooms/{roomId}/{imageId}/primary")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Set primary room image")
    public ResponseEntity<Void> setRoomImageAsPrimary(@PathVariable Long roomId, @PathVariable Long imageId) {
        User seller = currentUserService.getCurrentUser();
        imageService.setRoomImageAsPrimary(seller, roomId, imageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rooms/{roomId}")
    @Operation(summary = "Get room images")
    public ResponseEntity<List<ImageResponseDto>> getRoomImages(@PathVariable Long roomId) {
        List<ImageResponseDto> images = imageService.getRoomImages(roomId);
        return ResponseEntity.ok(images);
    }
}
