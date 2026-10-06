package dev.ambll.cloudclip.service;

import dev.ambll.cloudclip.dto.request.*;
import dev.ambll.cloudclip.dto.response.RoomResponse;
import dev.ambll.cloudclip.entity.Item;
import dev.ambll.cloudclip.entity.Room;
import dev.ambll.cloudclip.enums.ItemType;
import dev.ambll.cloudclip.exception.*;
import dev.ambll.cloudclip.repository.ItemRepository;
import dev.ambll.cloudclip.repository.RoomRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        room.setOwnerPasswordHash(
                passwordEncoder.encode(request.getOwnerPassword())
        );
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        room.setExpiresAt(request.getExpiresAt());

        Room savedRoom = roomRepository.save(room);

        RoomResponse response = toRoomResponse(savedRoom);

        return response;
    }

    public void deleteRoom(String roomKey, RoomAccessRequest request) {
        Room room = getActiveRoom(roomKey);

        accessOwnerPassword(room, request.getPassword());
        deleteRoom(room);
    }

    private void deleteRoom(Room room) {
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

    public void renameRoom(String roomKey, RenameRoomRequest request) {
        Room room = getActiveRoom(roomKey);

        accessOwnerPassword(room, request.getOwnerPassword());

        room.setName(request.getNewName());
        room.setUpdatedAt(LocalDateTime.now());

        roomRepository.save(room);
    }

    public void updateRoomPassword(String roomKey, UpdatePasswordRequest request) {
        Room room = getActiveRoom(roomKey);

        accessOwnerPassword(room, request.getOwnerPassword());

        if(request.getNewPassword() == null) {
            room.setPasswordHash(null);
        } else {
            room.setPasswordHash(
                    passwordEncoder.encode(request.getNewPassword())
            );
        }

        room.setUpdatedAt(LocalDateTime.now());

        roomRepository.save(room);
    }

    public void updateOwnerRoomPassword(String roomKey, UpdateOwnerPasswordRequest request) {
        Room room = getActiveRoom(roomKey);

        accessOwnerPassword(room, request.getOwnerPassword());

        room.setOwnerPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        room.setUpdatedAt(LocalDateTime.now());

        roomRepository.save(room);
    }

    public void updateRoomExpiration(String roomKey, UpdateExpirationRequest request) {
        Room room = getActiveRoom(roomKey);

        accessOwnerPassword(room, request.getOwnerPassword());

        room.setExpiresAt(request.getExpiresAt());
        room.setUpdatedAt(LocalDateTime.now());

        roomRepository.save(room);
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanupExpiredRooms() {
        List<Room> expiresRooms = roomRepository.findExpiresRooms();

        for (Room room : expiresRooms) {
            deleteRoom(room);
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
            throw new InvalidPasswordException();
        }
    }

    private void accessOwnerPassword(Room room, String password) {
        if(!passwordEncoder.matches(
                password,
                room.getOwnerPasswordHash()
        )) {
            throw new InvalidPasswordException();
        }
    }

    public boolean hasAccess(
            String roomKey,
            HttpSession session
    ) {
        return Boolean.TRUE.equals(
                session.getAttribute("room:" + roomKey)
        );
    }

    public Room getActiveRoom(String roomKey) {
        Room room = roomRepository.findByRoomKey(roomKey)
                .orElseThrow(RoomNotFoundException::new);

        if(room.getExpiresAt() != null && !LocalDateTime.now().isBefore(room.getExpiresAt())) {
            throw new RoomExpiredException();
        }

        return room;
    }

    public Room requireAccess(String roomKey, HttpSession session) {
        Room room = getActiveRoom(roomKey);

        if(room.getPasswordHash() != null && !hasAccess(roomKey, session)) {
            throw new RoomAccessDeniedException();
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
        response.setCreatedAt(room.getCreatedAt());
        response.setUpdatedAt(room.getUpdatedAt());
        response.setExpiresAt(room.getExpiresAt());
        response.setPasswordProtected(room.getPasswordHash() != null);

        return response;
    }
}
