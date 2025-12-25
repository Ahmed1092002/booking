package com.example.booking.image;

import com.example.booking.exception.ForbiddenException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.hotel.Hotel;
import com.example.booking.hotel.HotelRepository;
import com.example.booking.hotel.Room;
import com.example.booking.hotel.RoomRepository;
import com.example.booking.image.dto.ImageResponseDto;
import com.example.booking.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private final CloudinaryService cloudinaryService;
    private final HotelImageRepository hotelImageRepository;
    private final RoomImageRepository roomImageRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    public ImageService(CloudinaryService cloudinaryService,
            HotelImageRepository hotelImageRepository,
            RoomImageRepository roomImageRepository,
            HotelRepository hotelRepository,
            RoomRepository roomRepository) {
        this.cloudinaryService = cloudinaryService;
        this.hotelImageRepository = hotelImageRepository;
        this.roomImageRepository = roomImageRepository;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public HotelImage uploadHotelImage(User seller, Long hotelId, MultipartFile file) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        // Validate seller owns the hotel
        if (!hotel.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You can only upload images for your own hotels");
        }

        // Upload to Cloudinary
        Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, "hotels");

        // Get next display order
        List<HotelImage> existingImages = hotelImageRepository.findByHotelIdOrderByDisplayOrderAsc(hotelId);
        int nextOrder = existingImages.isEmpty() ? 0
                : existingImages.get(existingImages.size() - 1).getDisplayOrder() + 1;

        // Create image record
        HotelImage image = HotelImage.builder()
                .hotel(hotel)
                .imageUrl((String) uploadResult.get("secure_url"))
                .cloudinaryPublicId((String) uploadResult.get("public_id"))
                .isPrimary(existingImages.isEmpty()) // First image is primary
                .displayOrder(nextOrder)
                .build();

        return hotelImageRepository.save(image);
    }

    @Transactional
    public RoomImage uploadRoomImage(User seller, Long roomId, MultipartFile file) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // Validate seller owns the hotel
        if (!room.getHotel().getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You can only upload images for your own rooms");
        }

        // Upload to Cloudinary
        Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, "rooms");

        // Get next display order
        List<RoomImage> existingImages = roomImageRepository.findByRoomIdOrderByDisplayOrderAsc(roomId);
        int nextOrder = existingImages.isEmpty() ? 0
                : existingImages.get(existingImages.size() - 1).getDisplayOrder() + 1;

        // Create image record
        RoomImage image = RoomImage.builder()
                .room(room)
                .imageUrl((String) uploadResult.get("secure_url"))
                .cloudinaryPublicId((String) uploadResult.get("public_id"))
                .isPrimary(existingImages.isEmpty()) // First image is primary
                .displayOrder(nextOrder)
                .build();

        return roomImageRepository.save(image);
    }

    @Transactional
    public void deleteHotelImage(User seller, Long hotelId, Long imageId) {
        HotelImage image = hotelImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        // Validate seller owns the hotel
        if (!image.getHotel().getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You can only delete images from your own hotels");
        }

        // Delete from Cloudinary
        cloudinaryService.deleteImage(image.getCloudinaryPublicId());

        // Delete from database
        hotelImageRepository.delete(image);

        // If deleted image was primary, set first remaining image as primary
        if (image.getIsPrimary()) {
            List<HotelImage> remainingImages = hotelImageRepository.findByHotelIdOrderByDisplayOrderAsc(hotelId);
            if (!remainingImages.isEmpty()) {
                HotelImage newPrimary = remainingImages.get(0);
                newPrimary.setIsPrimary(true);
                hotelImageRepository.save(newPrimary);
            }
        }
    }

    @Transactional
    public void deleteRoomImage(User seller, Long roomId, Long imageId) {
        RoomImage image = roomImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        // Validate seller owns the room
        if (!image.getRoom().getHotel().getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You can only delete images from your own rooms");
        }

        // Delete from Cloudinary
        cloudinaryService.deleteImage(image.getCloudinaryPublicId());

        // Delete from database
        roomImageRepository.delete(image);

        // If deleted image was primary, set first remaining image as primary
        if (image.getIsPrimary()) {
            List<RoomImage> remainingImages = roomImageRepository.findByRoomIdOrderByDisplayOrderAsc(roomId);
            if (!remainingImages.isEmpty()) {
                RoomImage newPrimary = remainingImages.get(0);
                newPrimary.setIsPrimary(true);
                roomImageRepository.save(newPrimary);
            }
        }
    }

    @Transactional
    public void setHotelImageAsPrimary(User seller, Long hotelId, Long imageId) {
        HotelImage image = hotelImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        // Validate seller owns the hotel
        if (!image.getHotel().getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You can only modify images for your own hotels");
        }

        // Clear current primary
        hotelImageRepository.clearPrimaryForHotel(hotelId);

        // Set new primary
        image.setIsPrimary(true);
        hotelImageRepository.save(image);
    }

    @Transactional
    public void setRoomImageAsPrimary(User seller, Long roomId, Long imageId) {
        RoomImage image = roomImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        // Validate seller owns the room
        if (!image.getRoom().getHotel().getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You can only modify images for your own rooms");
        }

        // Clear current primary
        roomImageRepository.clearPrimaryForRoom(roomId);

        // Set new primary
        image.setIsPrimary(true);
        roomImageRepository.save(image);
    }

    @Transactional(readOnly = true)
    public List<ImageResponseDto> getHotelImages(Long hotelId) {
        return hotelImageRepository.findByHotelIdOrderByDisplayOrderAsc(hotelId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ImageResponseDto> getRoomImages(Long roomId) {
        return roomImageRepository.findByRoomIdOrderByDisplayOrderAsc(roomId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ImageResponseDto toDto(HotelImage image) {
        return new ImageResponseDto(
                image.getId(),
                image.getImageUrl(),
                image.getIsPrimary(),
                image.getDisplayOrder(),
                image.getUploadedAt());
    }

    private ImageResponseDto toDto(RoomImage image) {
        return new ImageResponseDto(
                image.getId(),
                image.getImageUrl(),
                image.getIsPrimary(),
                image.getDisplayOrder(),
                image.getUploadedAt());
    }
}
