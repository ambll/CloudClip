package dev.ambll.cloudclip.repository;

import dev.ambll.cloudclip.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomKey(String roomKey);

    @Query("""
            SELECT r 
            FROM Room r
            WHERE r.expiresAt IS NOT NULL AND r.expiresAt <= CURRENT_TIMESTAMP
            """)
    List<Room> findExpiresRooms();
}

