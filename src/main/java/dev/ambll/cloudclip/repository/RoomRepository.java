package dev.ambll.cloudclip.repository;

import dev.ambll.cloudclip.entity.Room;
import dev.ambll.cloudclip.enums.Visibility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomKey(String roomKey);

    @Query("""
            SELECT r 
            FROM Room r
            WHERE (r.visibility = :visibility)
                AND (r.expiresAt IS NULL OR r.expiresAt > CURRENT_TIMESTAMP)
            ORDER BY r.createdAt DESC
            """)
    List<Room> findRoomsByVisibility(@Param("visibility") Visibility visibility, Pageable pageable);

    @Query("""
            SELECT r 
            FROM Room r
            WHERE r.expiresAt IS NOT NULL AND r.expiresAt <= CURRENT_TIMESTAMP
            """)
    List<Room> findExpiresRooms();
}

