package dev.ambll.cloudclip.service;

import dev.ambll.cloudclip.dto.request.CreateRoomRequest;
import dev.ambll.cloudclip.dto.request.RoomAccessRequest;
import dev.ambll.cloudclip.dto.response.RoomResponse;
import dev.ambll.cloudclip.entity.Item;
import dev.ambll.cloudclip.entity.Room;
import dev.ambll.cloudclip.enums.ItemType;
import dev.ambll.cloudclip.enums.Visibility;
import dev.ambll.cloudclip.exception.InvalidRoomPasswordException;
import dev.ambll.cloudclip.exception.ItemNotFoundException;
import dev.ambll.cloudclip.exception.RoomExpiredException;
import dev.ambll.cloudclip.exception.RoomNotFoundException;
import dev.ambll.cloudclip.repository.ItemRepository;
import dev.ambll.cloudclip.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;
    private final ItemRepository itemRepository;
    private final FileStorageService fileStorageService;

    public RoomResponse createRoom(CreateRoomRequest request) {
        Room room = new Room();
        room.setRoomKey(generateRoomKey());
        room.setName(request.getName());
        if (request.getPassword() != null) {
            room.setPasswordHash(
                    passwordEncoder.encode(request.getPassword())
            );
        }
        room.setVisibility(request.getVisibility());
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        room.setExpiresAt(request.getExpiresAt());

        Room savedRoom = roomRepository.save(room);

        RoomResponse response = toRoomResponse(savedRoom);

        return response;
    }

    public void deleteRoom(String roomKey) {
        Room room = roomRepository.findByRoomKey(roomKey)
                .orElseThrow(RoomNotFoundException::new);

        List<Item> items = itemRepository.findByRoomAndType(room, ItemType.FILE);
        for (Item item : items) {
            try {
                fileStorageService.delete(item.getFilePath());
            } catch (IOException e) {
                log.error("Failed to delete file: {}", item.getFilePath(), e);
            }
        }

        roomRepository.delete(room);
    }

    public RoomResponse getRoom(String roomKey) {
        Room room = getActiveRoom(roomKey);

        return toRoomResponse(room);
    }

    public List<RoomResponse> listRooms() {
        Pageable pageable = PageRequest.of(0, 10);

        return roomRepository
                .findRoomsByVisibility(
                        Visibility.PUBLIC,
                        pageable
                )
                .stream()
                .map(this::toRoomResponse)
                .toList();
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanupExpiredRooms() {
        List<Room> expiresRooms = roomRepository.findExpiresRooms();

        for (Room room : expiresRooms) {
            deleteRoom(room.getRoomKey());
        }
    }

    public void accessRoom(String roomKey, RoomAccessRequest request) {
        Room room = getActiveRoom(roomKey);

        if(room.getPasswordHash() == null) {
            return;
        }

        if(!passwordEncoder.matches(
                request.getPassword(),
                room.getPasswordHash()
        )) {
            throw new InvalidRoomPasswordException();
        }
    }

    Room getActiveRoom(String roomKey) {
        Room room = roomRepository.findByRoomKey(roomKey)
                .orElseThrow(RoomNotFoundException::new);

        if(room.getExpiresAt() != null && !LocalDateTime.now().isBefore(room.getExpiresAt())) {
            throw new RoomExpiredException();
        }

        return room;
    }

    private String generateRoomKey() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }

    private RoomResponse toRoomResponse(Room room) {
        RoomResponse response = new RoomResponse();
        response.setRoomKey(room.getRoomKey());
        response.setName(room.getName());
        response.setVisibility(room.getVisibility());
        response.setCreatedAt(room.getCreatedAt());
        response.setUpdatedAt(room.getUpdatedAt());
        response.setExpiresAt(room.getExpiresAt());

        return response;
    }
}
