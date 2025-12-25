package com.example.booking.image;

import com.example.booking.image.dto.ImageResponseDto;
import com.example.booking.security.CurrentUserService;
import com.example.booking.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
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
    public ResponseEntity<ImageResponseDto> uploadHotelImage(
            @PathVariable Long hotelId,
            @RequestParam("image") MultipartFile file) {
        User seller = currentUserService.getCurrentUser();
        HotelImage image = imageService.uploadHotelImage(seller, hotelId, file);
        ImageResponseDto dto = new ImageResponseDto(
                image.getId(),
                image.getImageUrl(),
                image.getIsPrimary(),
                image.getDisplayOrder(),
                image.getUploadedAt());
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @DeleteMapping("/hotels/{hotelId}/{imageId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteHotelImage(
            @PathVariable Long hotelId,
            @PathVariable Long imageId) {
        User seller = currentUserService.getCurrentUser();
        imageService.deleteHotelImage(seller, hotelId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/hotels/{hotelId}/{imageId}/primary")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> setHotelImageAsPrimary(
            @PathVariable Long hotelId,
            @PathVariable Long imageId) {
        User seller = currentUserService.getCurrentUser();
        imageService.setHotelImageAsPrimary(seller, hotelId, imageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<List<ImageResponseDto>> getHotelImages(@PathVariable Long hotelId) {
        List<ImageResponseDto> images = imageService.getHotelImages(hotelId);
        return ResponseEntity.ok(images);
    }

    // Room Image Endpoints

    @PostMapping(value = "/rooms/{roomId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ImageResponseDto> uploadRoomImage(
            @PathVariable Long roomId,
            @RequestParam("image") MultipartFile file) {
        User seller = currentUserService.getCurrentUser();
        RoomImage image = imageService.uploadRoomImage(seller, roomId, file);
        ImageResponseDto dto = new ImageResponseDto(
                image.getId(),
                image.getImageUrl(),
                image.getIsPrimary(),
                image.getDisplayOrder(),
                image.getUploadedAt());
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @DeleteMapping("/rooms/{roomId}/{imageId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteRoomImage(
            @PathVariable Long roomId,
            @PathVariable Long imageId) {
        User seller = currentUserService.getCurrentUser();
        imageService.deleteRoomImage(seller, roomId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/rooms/{roomId}/{imageId}/primary")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> setRoomImageAsPrimary(
            @PathVariable Long roomId,
            @PathVariable Long imageId) {
        User seller = currentUserService.getCurrentUser();
        imageService.setRoomImageAsPrimary(seller, roomId, imageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<ImageResponseDto>> getRoomImages(@PathVariable Long roomId) {
        List<ImageResponseDto> images = imageService.getRoomImages(roomId);
        return ResponseEntity.ok(images);
    }
}
