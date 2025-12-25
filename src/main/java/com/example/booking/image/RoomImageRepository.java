package com.example.booking.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {

    List<RoomImage> findByRoomIdOrderByDisplayOrderAsc(Long roomId);

    Optional<RoomImage> findByRoomIdAndIsPrimaryTrue(Long roomId);

    @Modifying
    @Query("UPDATE RoomImage r SET r.isPrimary = false WHERE r.room.id = :roomId")
    void clearPrimaryForRoom(Long roomId);

    void deleteByRoomId(Long roomId);
}
