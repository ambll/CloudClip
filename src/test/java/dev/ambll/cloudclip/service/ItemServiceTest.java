package dev.ambll.cloudclip.service;

import dev.ambll.cloudclip.repository.ItemRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import dev.ambll.cloudclip.dto.request.CreateFileItemRequest;
import dev.ambll.cloudclip.dto.request.CreateTextItemRequest;
import dev.ambll.cloudclip.dto.response.FileDownload;
import dev.ambll.cloudclip.dto.response.ItemResponse;
import dev.ambll.cloudclip.entity.Item;
import dev.ambll.cloudclip.entity.Room;
import dev.ambll.cloudclip.enums.ItemType;
import dev.ambll.cloudclip.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private RoomService roomService;
    @Mock
    private FileStorageService fileStorageService;
    @InjectMocks
    private ItemService itemService;

    /*     createTextItem    */
    @Test
    void createTextItem_success() {
        Room room = new Room();
        room.setRoomKey("test-room");

        CreateTextItemRequest request = new CreateTextItemRequest();
        request.setText("Hello world!");

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(itemRepository.save(Mockito.any(Item.class)))
                .thenAnswer(invocation -> {
                    Item item = invocation.getArgument(0);
                    item.setId(1L);
                    return item;
                });

        ItemResponse response = itemService.createTextItem("test-room", request);

        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals(ItemType.TEXT, response.getType());
        Assertions.assertEquals("Hello world!", response.getText());
        Assertions.assertNull(response.getFileName());
        Assertions.assertNull(response.getDownloadUrl());

        Mockito.verify(itemRepository).save(Mockito.any(Item.class));
        Mockito.verify(roomService).getActiveRoom("test-room");
    }

    /*     createFileItem    */
    @Test
    void createFileItem_success() throws IOException {
        Room room = new Room();
        room.setRoomKey("test-room");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello world!".getBytes()
        );

        CreateFileItemRequest request = new CreateFileItemRequest();
        request.setFile(file);

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(fileStorageService.save(file))
                .thenReturn("files/uuid.txt");

        Mockito.when(itemRepository.save(Mockito.any(Item.class)))
                .thenAnswer(invocation -> {
                    Item item = invocation.getArgument(0);
                    item.setId(1L);
                    return item;
                });

        ItemResponse response = itemService.createFileItem("test-room", request);

        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals(ItemType.FILE, response.getType());
        Assertions.assertEquals("test.txt", response.getFileName());
        Assertions.assertEquals(
                "/rooms/test-room/items/1/file",
                response.getDownloadUrl()
        );

        Mockito.verify(fileStorageService).save(file);
        Mockito.verify(itemRepository).save(Mockito.any(Item.class));
    }

    @Test
    void createFileItem_emptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new byte[0]
        );

        CreateFileItemRequest request = new CreateFileItemRequest();
        request.setFile(file);

        Assertions.assertThrows(
                FileIsEmptyException.class,
                () -> itemService.createFileItem("test-room", request)
        );

        Mockito.verify(roomService, Mockito.never())
                .getActiveRoom(Mockito.anyString());

        Mockito.verify(fileStorageService, Mockito.never())
                .save(Mockito.any(MultipartFile.class));

        Mockito.verify(itemRepository, Mockito.never())
                .save(Mockito.any(Item.class));
    }

    @Test
    void createFileItem_invalidFileName() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                "text/plain",
                "Some content".getBytes()
        );

        CreateFileItemRequest request = new CreateFileItemRequest();
        request.setFile(file);

        Assertions.assertThrows(
                InvalidFileNameException.class,
                () -> itemService.createFileItem("test-room", request)
        );

        Mockito.verify(fileStorageService, Mockito.never())
                .save(Mockito.any(MultipartFile.class));

        Mockito.verify(itemRepository, Mockito.never())
                .save(Mockito.any(Item.class));
    }

    @Test
    void createFileItem_normalizesFileName() throws IOException {
        Room room = new Room();
        room.setRoomKey("test-room");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../secret.txt",
                "text/plain",
                "Some content".getBytes()
        );

        CreateFileItemRequest request = new CreateFileItemRequest();
        request.setFile(file);

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(fileStorageService.save(file))
                .thenReturn("files/uuid.txt");

        Mockito.when(itemRepository.save(Mockito.any(Item.class)))
                .thenAnswer(invocation -> {
                    Item item = invocation.getArgument(0);
                    item.setId(1L);
                    return item;
                });

        ItemResponse response = itemService.createFileItem("test-room", request);

        Assertions.assertEquals("secret.txt", response.getFileName());

        Mockito.verify(fileStorageService).save(file);
    }

    @Test
    void createFileItem_databaseSaveFails() throws IOException {
        Room room = new Room();
        room.setRoomKey("test-room");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Some content".getBytes()
        );

        CreateFileItemRequest request = new CreateFileItemRequest();
        request.setFile(file);

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(fileStorageService.save(file))
                .thenReturn("files/uuid.txt");

        Mockito.when(itemRepository.save(Mockito.any(Item.class)))
                .thenThrow(new RuntimeException("Database error"));

        Assertions.assertThrows(
                RuntimeException.class,
                () -> itemService.createFileItem("test-room", request)
        );

        Mockito.verify(fileStorageService).save(file);
        Mockito.verify(fileStorageService).delete("files/uuid.txt");
    }

    @Test
    void createFileItem_databaseSaveAndCleanupFail() throws IOException {
        Room room = new Room();
        room.setRoomKey("test-room");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Some content".getBytes()
        );

        CreateFileItemRequest request = new CreateFileItemRequest();
        request.setFile(file);

        RuntimeException databaseException =
                new RuntimeException("Database error");

        IOException cleanupException =
                new IOException("Disk error");

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(fileStorageService.save(file))
                .thenReturn("files/uuid.txt");

        Mockito.when(itemRepository.save(Mockito.any(Item.class)))
                .thenThrow(databaseException);

        Mockito.doThrow(cleanupException)
                .when(fileStorageService).delete("files/uuid.txt");

        RuntimeException thrown = Assertions.assertThrows(
                RuntimeException.class,
                () -> itemService.createFileItem("test-room", request)
        );

        Assertions.assertSame(databaseException, thrown);
        Assertions.assertEquals(1, thrown.getSuppressed().length);
        Assertions.assertSame(cleanupException, thrown.getSuppressed()[0]);

        Mockito.verify(fileStorageService).delete("files/uuid.txt");
    }

    /*     getItems    */
    @Test
    void getItems_success() {
        Room room = new Room();
        room.setId(1L);
        room.setRoomKey("test-room");

        Item textItem = new Item();
        textItem.setId(1L);
        textItem.setRoom(room);
        textItem.setType(ItemType.TEXT);
        textItem.setText("Hello!");
        textItem.setCreatedAt(LocalDateTime.now());

        Item fileItem = new Item();
        fileItem.setId(2L);
        fileItem.setRoom(room);
        fileItem.setType(ItemType.FILE);
        fileItem.setFileName("test.txt");
        fileItem.setFilePath("files/test.txt");
        fileItem.setCreatedAt(LocalDateTime.now());

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(itemRepository.findByRoomOrderByCreatedAtDesc(room))
                .thenReturn(List.of(fileItem, textItem));

        List<ItemResponse> responses = itemService.getItems("test-room");

        Assertions.assertEquals(2, responses.size());

        Assertions.assertEquals(2L, responses.get(0).getId());
        Assertions.assertEquals(ItemType.FILE, responses.get(0).getType());
        Assertions.assertEquals("test.txt", responses.get(0).getFileName());
        Assertions.assertEquals(
                "/rooms/test-room/items/2/file",
                responses.get(0).getDownloadUrl()
        );

        Assertions.assertEquals(1L, responses.get(1).getId());
        Assertions.assertEquals(ItemType.TEXT, responses.get(1).getType());
        Assertions.assertEquals("Hello!", responses.get(1).getText());
        Assertions.assertNull(responses.get(1).getDownloadUrl());

        Mockito.verify(itemRepository)
                .findByRoomOrderByCreatedAtDesc(room);
    }

    @Test
    void getItems_emptyList() {
        Room room = new Room();
        room.setRoomKey("test-room");

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(itemRepository.findByRoomOrderByCreatedAtDesc(room))
                .thenReturn(List.of());

        List<ItemResponse> responses = itemService.getItems("test-room");

        Assertions.assertTrue(responses.isEmpty());
    }

    /*     getFile    */
    @Test
    void getFile_success() throws IOException {
        Room room = new Room();
        room.setId(1L);
        room.setRoomKey("test-room");

        Item item = new Item();
        item.setId(10L);
        item.setRoom(room);
        item.setType(ItemType.FILE);
        item.setFileName("test.txt");
        item.setFilePath("files/test.txt");

        Resource resource = Mockito.mock(Resource.class);

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(itemRepository.findById(10L))
                .thenReturn(Optional.of(item));

        Mockito.when(fileStorageService.load("files/test.txt"))
                .thenReturn(resource);

        FileDownload response = itemService.getFile("test-room", 10L);

        Assertions.assertSame(resource, response.getFile());
        Assertions.assertEquals("test.txt", response.getFileName());
        Assertions.assertEquals(MediaType.TEXT_PLAIN, response.getContentType());

        Mockito.verify(fileStorageService).load("files/test.txt");
    }

    @Test
    void getFile_itemNotFound() {
        Room room = new Room();
        room.setId(1L);

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(itemRepository.findById(10L))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> itemService.getFile("test-room", 10L)
        );

        Mockito.verify(fileStorageService, Mockito.never())
                .load(Mockito.anyString());
    }

    @Test
    void getFile_itemDoesNotBelongToRoom() {
        Room room1 = new Room();
        room1.setId(1L);

        Room room2 = new Room();
        room2.setId(2L);

        Item item = new Item();
        item.setRoom(room2);
        item.setType(ItemType.FILE);
        item.setFilePath("files/test.txt");

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room1);

        Mockito.when(itemRepository.findById(10L))
                .thenReturn(Optional.of(item));

        Assertions.assertThrows(
                ItemNotBelongToRoomException.class,
                () -> itemService.getFile("test-room", 10L)
        );

        Mockito.verify(fileStorageService, Mockito.never())
                .load(Mockito.anyString());
    }

    @Test
    void getFile_itemIsNotFile() {
        Room room = new Room();
        room.setId(1L);

        Item item = new Item();
        item.setRoom(room);
        item.setType(ItemType.TEXT);

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(itemRepository.findById(10L))
                .thenReturn(Optional.of(item));

        Assertions.assertThrows(
                ItemNotFileException.class,
                () -> itemService.getFile("test-room", 10L)
        );

        Mockito.verify(fileStorageService, Mockito.never())
                .load(Mockito.anyString());
    }

    @Test
    void getFile_unknownContentType() throws IOException {
        Room room = new Room();
        room.setId(1L);
        room.setRoomKey("test-room");

        Item item = new Item();
        item.setRoom(room);
        item.setType(ItemType.FILE);
        item.setFileName("test.unknownextension");
        item.setFilePath("files/test");

        Resource resource = Mockito.mock(Resource.class);

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(itemRepository.findById(10L))
                .thenReturn(Optional.of(item));

        Mockito.when(fileStorageService.load("files/test"))
                .thenReturn(resource);

        FileDownload response = itemService.getFile("test-room", 10L);

        Assertions.assertEquals(
                MediaType.APPLICATION_OCTET_STREAM,
                response.getContentType()
        );
    }

    /*     deleteItem    */
    @Test
    void deleteItem_textItem() throws IOException {
        Room room = new Room();
        room.setId(1L);

        Item item = new Item();
        item.setRoom(room);
        item.setType(ItemType.TEXT);

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(itemRepository.findById(10L))
                .thenReturn(Optional.of(item));

        itemService.deleteItem("test-room", 10L);

        Mockito.verify(itemRepository).delete(item);

        Mockito.verify(fileStorageService, Mockito.never())
                .delete(Mockito.anyString());
    }

    @Test
    void deleteItem_fileItem() throws IOException {
        Room room = new Room();
        room.setId(1L);

        Item item = new Item();
        item.setRoom(room);
        item.setType(ItemType.FILE);
        item.setFilePath("files/test.txt");

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(itemRepository.findById(10L))
                .thenReturn(Optional.of(item));

        itemService.deleteItem("test-room", 10L);

        Mockito.verify(itemRepository).delete(item);
        Mockito.verify(fileStorageService).delete("files/test.txt");
    }

    @Test
    void deleteItem_itemNotFound() {
        Room room = new Room();
        room.setId(1L);

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(itemRepository.findById(10L))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> itemService.deleteItem("test-room", 10L)
        );

        Mockito.verify(itemRepository, Mockito.never())
                .delete(Mockito.any(Item.class));
    }

    @Test
    void deleteItem_itemDoesNotBelongToRoom() throws IOException {
        Room room1 = new Room();
        room1.setId(1L);

        Room room2 = new Room();
        room2.setId(2L);

        Item item = new Item();
        item.setRoom(room2);
        item.setType(ItemType.FILE);
        item.setFilePath("files/test.txt");

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room1);

        Mockito.when(itemRepository.findById(10L))
                .thenReturn(Optional.of(item));

        Assertions.assertThrows(
                ItemNotBelongToRoomException.class,
                () -> itemService.deleteItem("test-room", 10L)
        );

        Mockito.verify(itemRepository, Mockito.never())
                .delete(Mockito.any(Item.class));

        Mockito.verify(fileStorageService, Mockito.never())
                .delete(Mockito.anyString());
    }

    @Test
    void deleteItem_fileDeletionFails() throws IOException {
        Room room = new Room();
        room.setId(1L);

        Item item = new Item();
        item.setRoom(room);
        item.setType(ItemType.FILE);
        item.setFilePath("files/test.txt");

        Mockito.when(roomService.getActiveRoom("test-room"))
                .thenReturn(room);

        Mockito.when(itemRepository.findById(10L))
                .thenReturn(Optional.of(item));

        Mockito.doThrow(new IOException("Disk error"))
                .when(fileStorageService).delete("files/test.txt");

        Assertions.assertDoesNotThrow(
                () -> itemService.deleteItem("test-room", 10L)
        );

        Mockito.verify(itemRepository).delete(item);
        Mockito.verify(fileStorageService).delete("files/test.txt");
    }
}
