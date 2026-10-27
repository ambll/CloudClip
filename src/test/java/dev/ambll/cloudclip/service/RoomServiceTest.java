package dev.ambll.cloudclip.service;

import dev.ambll.cloudclip.dto.request.CreateRoomRequest;
import dev.ambll.cloudclip.dto.request.RenameRoomRequest;
import dev.ambll.cloudclip.dto.request.RoomAccessRequest;
import dev.ambll.cloudclip.dto.response.RoomResponse;
import dev.ambll.cloudclip.entity.Item;
import dev.ambll.cloudclip.entity.Room;
import dev.ambll.cloudclip.enums.ItemType;
import dev.ambll.cloudclip.exception.InvalidPasswordException;
import dev.ambll.cloudclip.exception.RoomAccessDeniedException;
import dev.ambll.cloudclip.exception.RoomExpiredException;
import dev.ambll.cloudclip.exception.RoomNotFoundException;
import dev.ambll.cloudclip.repository.ItemRepository;
import dev.ambll.cloudclip.repository.RoomRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
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
        String targetRoomKey = "test-room-key";

        Room room = new Room();
        room.setRoomKey(targetRoomKey);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        Mockito.when(roomRepository.findByRoomKey(targetRoomKey)).thenReturn(Optional.of(room));

        Room result = roomService.getActiveRoom(targetRoomKey);
        Assertions.assertSame(room, result);
    }

    @Test
    void getActiveRoom_throwsRoomExpiredException() {
        String targetRoomKey = "test-room-key";

        Room room = new Room();
        room.setRoomKey(targetRoomKey);
        room.setExpiresAt(LocalDateTime.now().minusDays(1));

        Mockito.when(roomRepository.findByRoomKey(targetRoomKey)).thenReturn(Optional.of(room));

        Assertions.assertThrows(RoomExpiredException.class, () -> roomService.getActiveRoom(targetRoomKey));
    }

    @Test
    void getActiveRoom_throwsRoomNotFoundException() {
        String targetRoomKey = "test-room-key";

        Mockito.when(roomRepository.findByRoomKey(targetRoomKey)).thenReturn(Optional.empty());

        Assertions.assertThrows(RoomNotFoundException.class, () -> roomService.getActiveRoom(targetRoomKey));
    }

    /*     hasAccess    */
    @Test
    void hasAccess_returnTrue() {
        HttpSession localSession = Mockito.mock(HttpSession.class);

        String targetRoomKey = "test-room-key";

        Mockito.when(localSession.getAttribute("room:" + targetRoomKey)).thenReturn(true);

        boolean result = roomService.hasAccess(targetRoomKey, localSession);

        Assertions.assertTrue(result);
    }

    @Test
    void hasAccess_returnFalse() {
        HttpSession localSession = Mockito.mock(HttpSession.class);

        String targetRoomKey = "test-room-key";

        Mockito.when(localSession.getAttribute("room:" + targetRoomKey)).thenReturn(false);

        boolean result = roomService.hasAccess(targetRoomKey, localSession);

        Assertions.assertFalse(result);
    }

    /*     requireAccess    */
    @Test
    void requireAccess_returnRoom() {
        HttpSession localSession = Mockito.mock(HttpSession.class);

        String targetRoomKey = "test-room-key";

        Room room = new Room();
        room.setRoomKey(targetRoomKey);
        room.setPasswordHash("encoded-password");
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        Mockito.when(localSession.getAttribute("room:" + targetRoomKey)).thenReturn(true);
        Mockito.when(roomRepository.findByRoomKey(targetRoomKey)).thenReturn(Optional.of(room));

        Room result = roomService.requireAccess(targetRoomKey, localSession);

        Assertions.assertSame(room, result);
    }

    @Test
    void requireAccess_nullPassword_returnRoom() {
        HttpSession localSession = Mockito.mock(HttpSession.class);

        String targetRoomKey = "test-room-key";

        Room room = new Room();
        room.setRoomKey(targetRoomKey);
        room.setPasswordHash(null);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        Mockito.when(roomRepository.findByRoomKey(targetRoomKey)).thenReturn(Optional.of(room));

        Room result = roomService.requireAccess(targetRoomKey, localSession);

        Assertions.assertSame(room, result);
    }

    @Test
    void requireAccess_throwRoomAccessDeniedException() {
        HttpSession localSession = Mockito.mock(HttpSession.class);

        String targetRoomKey = "test-room-key";

        Room room = new Room();
        room.setRoomKey(targetRoomKey);
        room.setPasswordHash("encoded-password");
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        Mockito.when(localSession.getAttribute("room:" + targetRoomKey)).thenReturn(false);
        Mockito.when(roomRepository.findByRoomKey(targetRoomKey)).thenReturn(Optional.of(room));

        Assertions.assertThrows(RoomAccessDeniedException.class, () -> roomService.requireAccess(targetRoomKey, localSession));
    }

    /*     deleteRoom    */
    @Test
    void deleteRoom_success() throws IOException {
        String roomKey = "test-room-key";
        String ownerPassword = "owner-password";
        String passwordHash = "encoded-password";

        Room room = new Room();
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(passwordHash);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        RoomAccessRequest request = new RoomAccessRequest();
        request.setPassword(ownerPassword);

        Item fileItem = new Item();
        fileItem.setFilePath("test/file.txt");

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, passwordHash))
                .thenReturn(true);
        Mockito.when(itemRepository.findByRoomAndType(room, ItemType.FILE))
                .thenReturn(List.of(fileItem));

        roomService.deleteRoom(roomKey, request);

        Mockito.verify(fileStorageService).delete("test/file.txt");
        Mockito.verify(roomRepository).delete(room);
    }

    @Test
    void deleteRoom_invalidOwnerPassword() throws IOException {
        String roomKey = "test-room-key";
        String ownerPassword = "wrong-password";
        String passwordHash = "encoded-password";

        Room room = new Room();
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(passwordHash);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        RoomAccessRequest request = new RoomAccessRequest();
        request.setPassword(ownerPassword);

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, passwordHash))
                .thenReturn(false);

        Assertions.assertThrows(
                InvalidPasswordException.class,
                () -> roomService.deleteRoom(roomKey, request)
        );

        Mockito.verify(roomRepository, Mockito.never())
                .delete(Mockito.any(Room.class));

        Mockito.verify(itemRepository, Mockito.never())
                .findByRoomAndType(Mockito.any(Room.class), Mockito.any(ItemType.class));

        Mockito.verify(fileStorageService, Mockito.never())
                .delete(Mockito.anyString());
    }

    @Test
    void deleteRoom_fileDeletionFails_roomStillDeleted() throws IOException {
        String roomKey = "test-room-key";
        String ownerPassword = "owner-password";
        String passwordHash = "encoded-password";

        Room room = new Room();
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(passwordHash);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        RoomAccessRequest request = new RoomAccessRequest();
        request.setPassword(ownerPassword);

        Item fileItem = new Item();
        fileItem.setFilePath("test/file.txt");

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, passwordHash))
                .thenReturn(true);
        Mockito.when(itemRepository.findByRoomAndType(room, ItemType.FILE))
                .thenReturn(List.of(fileItem));

        Mockito.doThrow(new IOException("Disk error"))
                .when(fileStorageService).delete("test/file.txt");

        roomService.deleteRoom(roomKey, request);

        Mockito.verify(fileStorageService).delete("test/file.txt");
        Mockito.verify(roomRepository).delete(room);
    }

    /*     createRoom    */
    @Test
    void createRoom_returnRoomResponse() {
        String ownerPassword = "owner-password";
        String encodedOwnerPassword = "encoded-owner-password";
        String password = "room-password";
        String encodedPassword = "encoded-room-password";

        CreateRoomRequest request = new CreateRoomRequest();
        request.setName("My room");
        request.setPassword(password);
        request.setOwnerPassword(ownerPassword);
        request.setExpiresAt(LocalDateTime.now().plusDays(1));

        Mockito.when(passwordEncoder.encode(ownerPassword)).thenReturn(encodedOwnerPassword);
        Mockito.when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        Mockito.when(roomRepository.save(Mockito.any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoomResponse result = roomService.createRoom(request);

        Assertions.assertEquals(request.getName(), result.getName());
        Assertions.assertEquals(request.getExpiresAt(), result.getExpiresAt());
        Assertions.assertTrue(result.isPasswordProtected());
        Assertions.assertNotNull(result.getRoomKey());
        Assertions.assertFalse(result.getRoomKey().isBlank() );

        Mockito.verify(roomRepository).save(Mockito.any(Room.class));
        Mockito.verify(passwordEncoder).encode(ownerPassword);
        Mockito.verify(passwordEncoder).encode(password);
    }

    @Test
    void createRoom_withoutPassword() {
        String ownerPassword = "owner-password";
        String encodedOwnerPassword = "encoded-owner-password";

        CreateRoomRequest request = new CreateRoomRequest();
        request.setName("My room");
        request.setPassword(null);
        request.setOwnerPassword(ownerPassword);
        request.setExpiresAt(LocalDateTime.now().plusDays(1));

        Mockito.when(passwordEncoder.encode(ownerPassword)).thenReturn(encodedOwnerPassword);
        Mockito.when(roomRepository.save(Mockito.any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoomResponse result = roomService.createRoom(request);

        Assertions.assertEquals(request.getName(), result.getName());
        Assertions.assertEquals(request.getExpiresAt(), result.getExpiresAt());
        Assertions.assertFalse(result.isPasswordProtected());
        Assertions.assertNotNull(result.getRoomKey());
        Assertions.assertFalse(result.getRoomKey().isBlank());

        Mockito.verify(roomRepository).save(Mockito.any(Room.class));
        Mockito.verify(passwordEncoder).encode(ownerPassword);
        Mockito.verify(passwordEncoder, Mockito.times(1)).encode(Mockito.anyString());
    }

    /*     renameRoom    */
    @Test
    void renameRoom_success() {
        String roomKey = "test-room-key";
        String roomName = "My room";
        String newRoomName = "New room name";
        String ownerPassword = "owner-password";
        String passwordHash = "encoded-password";
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        Room room = new Room();
        room.setName(roomName);
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(passwordHash);
        room.setUpdatedAt(updatedAt);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        RenameRoomRequest request = new RenameRoomRequest();
        request.setOwnerPassword(ownerPassword);
        request.setNewName(newRoomName);

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, passwordHash))
                .thenReturn(true);

        roomService.renameRoom(roomKey, request);

        Mockito.verify(roomRepository).save(Mockito.any(Room.class));
        Assertions.assertEquals(newRoomName, room.getName());
        Assertions.assertNotEquals(updatedAt, room.getUpdatedAt());
    }

    @Test
    void renameRoom_invalidOwnerPassword() {
        String roomKey = "test-room-key";
        String roomName = "My room";
        String newRoomName = "New room name";
        String ownerPassword = "owner-password";
        String passwordHash = "encoded-password";
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        Room room = new Room();
        room.setName(roomName);
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(passwordHash);
        room.setUpdatedAt(updatedAt);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        RenameRoomRequest request = new RenameRoomRequest();
        request.setOwnerPassword(ownerPassword);
        request.setNewName(newRoomName);

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, passwordHash))
                .thenReturn(false);

        Assertions.assertThrows(
                InvalidPasswordException.class,
                () -> roomService.renameRoom(roomKey, request)
        );

        Mockito.verify(roomRepository, Mockito.never()).save(Mockito.any(Room.class));
        Assertions.assertNotEquals(newRoomName, room.getName());
        Assertions.assertEquals(updatedAt, room.getUpdatedAt());
    }
}
