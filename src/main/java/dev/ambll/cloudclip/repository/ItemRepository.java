package dev.ambll.cloudclip.repository;

import dev.ambll.cloudclip.entity.Item;
import dev.ambll.cloudclip.entity.Room;
import dev.ambll.cloudclip.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByRoomOrderByCreatedAtDesc(Room room);
    List<Item> findByRoomAndType(Room room, ItemType type);
}