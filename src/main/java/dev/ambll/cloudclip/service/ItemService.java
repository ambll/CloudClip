package dev.ambll.cloudclip.service;

import dev.ambll.cloudclip.dto.request.CreateFileItemRequest;
import dev.ambll.cloudclip.dto.request.CreateTextItemRequest;
import dev.ambll.cloudclip.dto.response.FileDownload;
import dev.ambll.cloudclip.dto.response.ItemResponse;
import dev.ambll.cloudclip.entity.Item;
import dev.ambll.cloudclip.entity.Room;
import dev.ambll.cloudclip.enums.ItemType;
import dev.ambll.cloudclip.repository.ItemRepository;
import dev.ambll.cloudclip.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final RoomRepository roomRepository;
    private final FileStorageService fileStorageService;

    public ItemResponse createTextItem(
            String roomKey,
            CreateTextItemRequest request
    ) {
        Room room = roomRepository.findByRoomKey(roomKey)
                .orElseThrow(() ->
                        new RuntimeException("Room not found")
                );

        Item item = new Item();
        item.setRoom(room);
        item.setType(ItemType.TEXT);
        item.setText(request.getText());
        item.setFileName(null);
        item.setFilePath(null);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        Item savedItem = itemRepository.save(item);

        ItemResponse response = toItemResponse(savedItem);

        return response;
    }

    public ItemResponse createFileItem(
            String roomKey,
            CreateFileItemRequest request
    ) {
        Room room = roomRepository.findByRoomKey(roomKey)
                .orElseThrow(() ->
                        new RuntimeException("Room not found")
                );
        String filePath = fileStorageService.save(request.getFile());

        Item item = new Item();
        item.setRoom(room);
        item.setType(ItemType.FILE);
        item.setText(null);
        item.setFileName(request.getFile().getOriginalFilename());
        item.setFilePath(filePath);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        Item savedItem = itemRepository.save(item);

        ItemResponse response = toItemResponse(savedItem);

        return response;
    }

    public List<ItemResponse> getItems(String roomKey) {
        Room room = roomRepository.findByRoomKey(roomKey)
                .orElseThrow(() ->
                        new RuntimeException("Room not found")
                );

        return itemRepository
                .findByRoomOrderByCreatedAtDesc(room)
                .stream()
                .map(this::toItemResponse)
                .toList();

    }

    public FileDownload getFile(String roomKey, Long itemId) {
        Room room = roomRepository.findByRoomKey(roomKey)
                .orElseThrow(() ->
                        new RuntimeException("Room not found")
                );
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new RuntimeException("Item not found")
                );

        if (!Objects.equals(item.getRoom().getId(), room.getId())) {
            throw new RuntimeException("Item does not belong to this room");
        }

        if (item.getType() != ItemType.FILE) {
            throw new RuntimeException("Item is not a file");
        }

        Resource file = fileStorageService.load(item.getFilePath());

        MediaType contentType = MediaTypeFactory
                .getMediaType(item.getFileName())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        FileDownload fileDownload = new FileDownload();
        fileDownload.setFile(file);
        fileDownload.setFileName(item.getFileName());
        fileDownload.setContentType(contentType);
        return fileDownload;
    }

    public void deleteItem(String roomKey, Long itemId) throws IOException {
        Room room = roomRepository.findByRoomKey(roomKey)
                .orElseThrow(() ->
                        new RuntimeException("Room not found")
                );
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new RuntimeException("Item not found")
                );

        if (!Objects.equals(item.getRoom().getId(), room.getId())) {
            throw new RuntimeException("Item does not belong to this room");
        }

        if(item.getType() == ItemType.FILE) {
            fileStorageService.delete(item.getFilePath());
        }

        itemRepository.delete(item);
    }

    private ItemResponse toItemResponse(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setType(item.getType());
        response.setText(item.getText());
        response.setFileName(item.getFileName());
        if (item.getType() == ItemType.FILE) {
            response.setDownloadUrl(
                    "/room/" + item.getRoom().getRoomKey()
                            + "/item/" + item.getId() + "/file"
            );
        }
        response.setCreatedAt(item.getCreatedAt());

        return response;
    }
}
