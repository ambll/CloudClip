package dev.ambll.cloudclip.service;

import dev.ambll.cloudclip.entity.Room;
import dev.ambll.cloudclip.exception.RoomExpiredException;
import dev.ambll.cloudclip.exception.RoomNotFoundException;
import dev.ambll.cloudclip.repository.ItemRepository;
import dev.ambll.cloudclip.repository.RoomRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private FileStorageService fileStorageService;
    @InjectMocks
    private RoomService roomService;

    /*     getActiveRoom    */
    @Test
    void getActiveRoom_returnRoom() {
        String targetRoomKey = "test-room-key-12345";

        Room room = new Room();
        room.setRoomKey(targetRoomKey);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        Mockito.when(roomRepository.findByRoomKey(targetRoomKey)).thenReturn(Optional.of(room));

        Room result = roomService.getActiveRoom(targetRoomKey);
        Assertions.assertSame(room, result);
    }

    @Test
    void getActiveRoom_throwsRoomExpiredException() {
        String targetRoomKey = "test-room-key-12345";

        Room room = new Room();
        room.setRoomKey(targetRoomKey);
        room.setExpiresAt(LocalDateTime.now().minusDays(1));

        Mockito.when(roomRepository.findByRoomKey(targetRoomKey)).thenReturn(Optional.of(room));

        Assertions.assertThrows(RoomExpiredException.class, () -> roomService.getActiveRoom(targetRoomKey));
    }

    @Test
    void getActiveRoom_throwsRoomNotFoundException() {
        String targetRoomKey = "test-room-key-12345";

        Mockito.when(roomRepository.findByRoomKey(targetRoomKey)).thenReturn(Optional.empty());

        Assertions.assertThrows(RoomNotFoundException.class, () -> roomService.getActiveRoom(targetRoomKey));
    }
}
