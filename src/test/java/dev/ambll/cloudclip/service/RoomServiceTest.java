package dev.ambll.cloudclip.service;

import dev.ambll.cloudclip.dto.request.*;
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
        String encodedOwnerPassword = "encoded-owner-password";
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        Room room = new Room();
        room.setName(roomName);
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(encodedOwnerPassword);
        room.setUpdatedAt(updatedAt);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        RenameRoomRequest request = new RenameRoomRequest();
        request.setOwnerPassword(ownerPassword);
        request.setNewName(newRoomName);

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, encodedOwnerPassword))
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
        String encodedOwnerPassword = "encoded-owner-password";
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        Room room = new Room();
        room.setName(roomName);
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(encodedOwnerPassword);
        room.setUpdatedAt(updatedAt);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        RenameRoomRequest request = new RenameRoomRequest();
        request.setOwnerPassword(ownerPassword);
        request.setNewName(newRoomName);

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, encodedOwnerPassword))
                .thenReturn(false);

        Assertions.assertThrows(
                InvalidPasswordException.class,
                () -> roomService.renameRoom(roomKey, request)
        );

        Mockito.verify(roomRepository, Mockito.never()).save(Mockito.any(Room.class));
        Assertions.assertNotEquals(newRoomName, room.getName());
        Assertions.assertEquals(updatedAt, room.getUpdatedAt());
    }

    /*  updateRoomPassword  */
    @Test
    void updateRoomPassword_success() {
        String roomKey = "test-room-key";
        String ownerPassword = "owner-password";
        String encodedOwnerPassword = "encoded-owner-password";
        String encodedPassword = "encoded-room-password";
        String newPassword = "new-room-password";
        String encodedNewPassword = "encoded-new-room-password";
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        Room room = new Room();
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(encodedOwnerPassword);
        room.setPasswordHash(encodedPassword);
        room.setUpdatedAt(updatedAt);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOwnerPassword(ownerPassword);
        request.setNewPassword(newPassword);

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, encodedOwnerPassword))
                .thenReturn(true);
        Mockito.when(passwordEncoder.encode(newPassword))
                .thenReturn(encodedNewPassword);


        roomService.updateRoomPassword(roomKey, request);

        Mockito.verify(roomRepository).save(Mockito.any(Room.class));
        Mockito.verify(passwordEncoder).encode(newPassword);
        Assertions.assertNotEquals(updatedAt, room.getUpdatedAt());
        Assertions.assertEquals(encodedNewPassword, room.getPasswordHash());
    }

    @Test
    void updateRoomPassword_nullPassword() {
        String roomKey = "test-room-key";
        String ownerPassword = "owner-password";
        String encodedOwnerPassword = "encoded-owner-password";
        String encodedPassword = "encoded-room-password";
        String newPassword = null;
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        Room room = new Room();
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(encodedOwnerPassword);
        room.setPasswordHash(encodedPassword);
        room.setUpdatedAt(updatedAt);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOwnerPassword(ownerPassword);
        request.setNewPassword(newPassword);

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, encodedOwnerPassword))
                .thenReturn(true);


        roomService.updateRoomPassword(roomKey, request);

        Mockito.verify(roomRepository).save(Mockito.any(Room.class));
        Mockito.verify(passwordEncoder, Mockito.never()).encode(Mockito.any(String.class));
        Assertions.assertNotEquals(updatedAt, room.getUpdatedAt());
        Assertions.assertNull(room.getPasswordHash());
    }

    @Test
    void updateRoomPassword_invalidOwnerPassword() {
        String roomKey = "test-room-key";
        String ownerPassword = "owner-password";
        String encodedOwnerPassword = "encoded-owner-password";
        String encodedPassword = "encoded-room-password";
        String newPassword = "new-room-password";
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        Room room = new Room();
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(encodedOwnerPassword);
        room.setPasswordHash(encodedPassword);
        room.setUpdatedAt(updatedAt);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOwnerPassword(ownerPassword);
        request.setNewPassword(newPassword);

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, encodedOwnerPassword))
                .thenReturn(false);

        Assertions.assertThrows(
                InvalidPasswordException.class,
                () -> roomService.updateRoomPassword(roomKey, request)
        );

        Mockito.verify(roomRepository, Mockito.never()).save(Mockito.any(Room.class));
        Assertions.assertEquals(updatedAt, room.getUpdatedAt());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(Mockito.any(String.class));
        Assertions.assertEquals(encodedPassword, room.getPasswordHash());
    }

    /*  updateOwnerRoomPassword  */
    @Test
    void updateOwnerRoomPassword_success() {
        String roomKey = "test-room-key";
        String ownerPassword = "owner-password";
        String encodedOwnerPassword = "encoded-owner-password";
        String newOwnerPassword = "new-owner-password";
        String encodedNewOwnerPassword = "encoded-new-owner-password";
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        Room room = new Room();
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(encodedOwnerPassword);
        room.setUpdatedAt(updatedAt);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        UpdateOwnerPasswordRequest request = new UpdateOwnerPasswordRequest();
        request.setOwnerPassword(ownerPassword);
        request.setNewPassword(newOwnerPassword);

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, encodedOwnerPassword))
                .thenReturn(true);
        Mockito.when(passwordEncoder.encode(newOwnerPassword))
                .thenReturn(encodedNewOwnerPassword);


        roomService.updateOwnerRoomPassword(roomKey, request);

        Mockito.verify(roomRepository).save(Mockito.any(Room.class));
        Mockito.verify(passwordEncoder).encode(newOwnerPassword);
        Assertions.assertNotEquals(updatedAt, room.getUpdatedAt());
        Assertions.assertEquals(encodedNewOwnerPassword, room.getOwnerPasswordHash());
    }

    @Test
    void updateOwnerRoomPassword_invalidOwnerPassword() {
        String roomKey = "test-room-key";
        String ownerPassword = "owner-password";
        String encodedOwnerPassword = "encoded-owner-password";
        String newOwnerPassword = "new-owner-password";
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        Room room = new Room();
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(encodedOwnerPassword);
        room.setUpdatedAt(updatedAt);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        UpdateOwnerPasswordRequest request = new UpdateOwnerPasswordRequest();
        request.setOwnerPassword(ownerPassword);
        request.setNewPassword(newOwnerPassword);

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, encodedOwnerPassword))
                .thenReturn(false);

        Assertions.assertThrows(
                InvalidPasswordException.class,
                () -> roomService.updateOwnerRoomPassword(roomKey, request)
        );

        Mockito.verify(roomRepository, Mockito.never()).save(Mockito.any(Room.class));
        Assertions.assertEquals(updatedAt, room.getUpdatedAt());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(Mockito.any(String.class));
        Assertions.assertEquals(encodedOwnerPassword, room.getOwnerPasswordHash());
    }

    /*  updateRoomExpiration  */
    @Test
    void updateRoomExpiration_success() {
        String roomKey = "test-room-key";
        String ownerPassword = "owner-password";
        String encodedOwnerPassword = "encoded-owner-password";

        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime newExpiration = LocalDateTime.now().plusDays(10);

        Room room = new Room();
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(encodedOwnerPassword);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));
        room.setUpdatedAt(updatedAt);

        UpdateExpirationRequest request = new UpdateExpirationRequest();
        request.setOwnerPassword(ownerPassword);
        request.setExpiresAt(newExpiration);

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, encodedOwnerPassword))
                .thenReturn(true);

        roomService.updateRoomExpiration(roomKey, request);

        Mockito.verify(roomRepository).save(Mockito.any(Room.class));
        Assertions.assertEquals(newExpiration, room.getExpiresAt());
        Assertions.assertNotEquals(updatedAt, room.getUpdatedAt());
    }

    @Test
    void updateRoomExpiration_nullExpiration() {
        String roomKey = "test-room-key";
        String ownerPassword = "owner-password";
        String encodedOwnerPassword = "encoded-owner-password";

        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        Room room = new Room();
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(encodedOwnerPassword);
        room.setExpiresAt(LocalDateTime.now().plusDays(1));
        room.setUpdatedAt(updatedAt);

        UpdateExpirationRequest request = new UpdateExpirationRequest();
        request.setOwnerPassword(ownerPassword);
        request.setExpiresAt(null);

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, encodedOwnerPassword))
                .thenReturn(true);

        roomService.updateRoomExpiration(roomKey, request);

        Mockito.verify(roomRepository).save(Mockito.any(Room.class));
        Assertions.assertNull(room.getExpiresAt());
        Assertions.assertNotEquals(updatedAt, room.getUpdatedAt());
    }

    @Test
    void updateRoomExpiration_invalidOwnerPassword() {
        String roomKey = "test-room-key";
        String ownerPassword = "wrong-password";
        String encodedOwnerPassword = "encoded-owner-password";

        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime oldExpiration = LocalDateTime.now().plusDays(1);

        Room room = new Room();
        room.setRoomKey(roomKey);
        room.setOwnerPasswordHash(encodedOwnerPassword);
        room.setExpiresAt(oldExpiration);
        room.setUpdatedAt(updatedAt);

        UpdateExpirationRequest request = new UpdateExpirationRequest();
        request.setOwnerPassword(ownerPassword);
        request.setExpiresAt(LocalDateTime.now().plusDays(10));

        Mockito.when(roomRepository.findByRoomKey(roomKey))
                .thenReturn(Optional.of(room));
        Mockito.when(passwordEncoder.matches(ownerPassword, encodedOwnerPassword))
                .thenReturn(false);

        Assertions.assertThrows(
                InvalidPasswordException.class,
                () -> roomService.updateRoomExpiration(roomKey, request)
        );

        Mockito.verify(roomRepository, Mockito.never())
                .save(Mockito.any(Room.class));

        Assertions.assertEquals(oldExpiration, room.getExpiresAt());
        Assertions.assertEquals(updatedAt, room.getUpdatedAt());
    }

    /*  getRoom  */
    @Test
    void getRoom_success() {
        Room room = new Room();
        room.setRoomKey("test-room-key");
        room.setName("Test Room");
        room.setPasswordHash("encodedPassword");
        room.setCreatedAt(LocalDateTime.now().minusDays(1));
        room.setUpdatedAt(LocalDateTime.now());
        room.setExpiresAt(LocalDateTime.now().plusDays(1));

        Mockito.when(roomRepository.findByRoomKey("test-room-key"))
                .thenReturn(Optional.of(room));

        RoomResponse response = roomService.getRoom("test-room-key");

        Assertions.assertEquals("test-room-key", response.getRoomKey());
        Assertions.assertEquals("Test Room", response.getName());
        Assertions.assertEquals(room.getCreatedAt(), response.getCreatedAt());
        Assertions.assertEquals(room.getUpdatedAt(), response.getUpdatedAt());
        Assertions.assertEquals(room.getExpiresAt(), response.getExpiresAt());
        Assertions.assertTrue(response.isPasswordProtected());
    }

    @Test
    void getRoom_withoutPassword() {
        Room room = new Room();
        room.setRoomKey("test-room-key");
        room.setName("Test Room");
        room.setPasswordHash(null);

        Mockito.when(roomRepository.findByRoomKey("test-room-key"))
                .thenReturn(Optional.of(room));

        RoomResponse response = roomService.getRoom("test-room-key");

        Assertions.assertFalse(response.isPasswordProtected());
    }

    @Test
    void getRoom_roomNotFound() {
        Mockito.when(roomRepository.findByRoomKey("test-room-key"))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(
                RoomNotFoundException.class,
                () -> roomService.getRoom("test-room-key")
        );
    }

    @Test
    void getRoom_roomExpired() {
        Room room = new Room();
        room.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        Mockito.when(roomRepository.findByRoomKey("test-room-key"))
                .thenReturn(Optional.of(room));

        Assertions.assertThrows(
                RoomExpiredException.class,
                () -> roomService.getRoom("test-room-key")
        );
    }

    /*  accessRoom  */
    @Test
    void accessRoom_withoutPassword() {
        Room room = new Room();
        room.setPasswordHash(null);

        RoomAccessRequest request = new RoomAccessRequest();
        request.setPassword(null);

        Mockito.when(roomRepository.findByRoomKey("test-room-key"))
                .thenReturn(Optional.of(room));

        Assertions.assertDoesNotThrow(
                () -> roomService.accessRoom("test-room-key", request)
        );

        Mockito.verify(passwordEncoder, Mockito.never())
                .matches(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void accessRoom_correctPassword() {
        Room room = new Room();
        room.setPasswordHash("encodedPassword");

        RoomAccessRequest request = new RoomAccessRequest();
        request.setPassword("correctPassword");

        Mockito.when(roomRepository.findByRoomKey("test-room-key"))
                .thenReturn(Optional.of(room));

        Mockito.when(passwordEncoder.matches(
                "correctPassword",
                "encodedPassword"
        )).thenReturn(true);

        Assertions.assertDoesNotThrow(
                () -> roomService.accessRoom("test-room-key", request)
        );

        Mockito.verify(passwordEncoder).matches(
                "correctPassword",
                "encodedPassword"
        );
    }

    @Test
    void accessRoom_incorrectPassword() {
        Room room = new Room();
        room.setPasswordHash("encodedPassword");

        RoomAccessRequest request = new RoomAccessRequest();
        request.setPassword("wrongPassword");

        Mockito.when(roomRepository.findByRoomKey("test-room-key"))
                .thenReturn(Optional.of(room));

        Mockito.when(passwordEncoder.matches(
                "wrongPassword",
                "encodedPassword"
        )).thenReturn(false);

        Assertions.assertThrows(
                InvalidPasswordException.class,
                () -> roomService.accessRoom("test-room-key", request)
        );
    }

    /*  cleanupExpiredRooms  */
    @Test
    void cleanupExpiredRooms_success() throws IOException {
        Room room1 = new Room();
        Room room2 = new Room();

        Item item1 = new Item();
        item1.setFilePath("files/file1.txt");

        Item item2 = new Item();
        item2.setFilePath("files/file2.txt");

        Mockito.when(roomRepository.findExpiresRooms())
                .thenReturn(List.of(room1, room2));

        Mockito.when(itemRepository.findByRoomAndType(room1, ItemType.FILE))
                .thenReturn(List.of(item1));

        Mockito.when(itemRepository.findByRoomAndType(room2, ItemType.FILE))
                .thenReturn(List.of(item2));

        roomService.cleanupExpiredRooms();

        Mockito.verify(fileStorageService).delete("files/file1.txt");
        Mockito.verify(fileStorageService).delete("files/file2.txt");

        Mockito.verify(roomRepository).delete(room1);
        Mockito.verify(roomRepository).delete(room2);
    }

    @Test
    void cleanupExpiredRooms_fileDeletionFails() throws IOException {
        Room room1 = new Room();
        Room room2 = new Room();

        Item item1 = new Item();
        item1.setFilePath("files/file1.txt");

        Item item2 = new Item();
        item2.setFilePath("files/file2.txt");

        Mockito.when(roomRepository.findExpiresRooms())
                .thenReturn(List.of(room1, room2));

        Mockito.when(itemRepository.findByRoomAndType(room1, ItemType.FILE))
                .thenReturn(List.of(item1));

        Mockito.when(itemRepository.findByRoomAndType(room2, ItemType.FILE))
                .thenReturn(List.of(item2));

        Mockito.doThrow(new IOException("Disk error"))
                .when(fileStorageService).delete("files/file1.txt");

        roomService.cleanupExpiredRooms();

        Mockito.verify(fileStorageService).delete("files/file1.txt");
        Mockito.verify(fileStorageService).delete("files/file2.txt");

        Mockito.verify(roomRepository).delete(room1);
        Mockito.verify(roomRepository).delete(room2);
    }

    @Test
    void cleanupExpiredRooms_noExpiredRooms() {
        Mockito.when(roomRepository.findExpiresRooms())
                .thenReturn(List.of());

        Assertions.assertDoesNotThrow(
                () -> roomService.cleanupExpiredRooms()
        );

        Mockito.verify(roomRepository, Mockito.never())
                .delete(Mockito.any(Room.class));

        Mockito.verify(itemRepository, Mockito.never())
                .findByRoomAndType(
                        Mockito.any(Room.class),
                        Mockito.any(ItemType.class)
                );
    }
}
