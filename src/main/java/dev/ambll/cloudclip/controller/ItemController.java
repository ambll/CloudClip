package dev.ambll.cloudclip.controller;

import dev.ambll.cloudclip.dto.request.CreateFileItemRequest;
import dev.ambll.cloudclip.dto.request.CreateTextItemRequest;
import dev.ambll.cloudclip.dto.response.FileDownload;
import dev.ambll.cloudclip.dto.response.ItemResponse;
import dev.ambll.cloudclip.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping(value = "/room/{roomKey}/item/text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ItemResponse createTextItem(@PathVariable String roomKey, @RequestBody CreateTextItemRequest request) {
        return itemService.createTextItem(roomKey, request);
    }

    @PostMapping(value = "/room/{roomKey}/item/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ItemResponse createFileItem(@PathVariable String roomKey, @ModelAttribute CreateFileItemRequest request) {
        return itemService.createFileItem(roomKey, request);
    }

    @GetMapping("/room/{roomKey}/items")
    public List<ItemResponse> getItems(@PathVariable String roomKey) {
        return itemService.getItems(roomKey);
    }

    @DeleteMapping("room/{roomKey}/item/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable String roomKey, @PathVariable Long itemId) throws IOException {
        itemService.deleteItem(roomKey, itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/room/{roomKey}/item/{itemId}/file")
    public ResponseEntity<Resource> getFile(@PathVariable String roomKey, @PathVariable Long itemId) throws IOException {
        FileDownload file = itemService.getFile(roomKey, itemId);

        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(file.getFileName())
                .build();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .contentType(file.getContentType())
                .contentLength(file.getFile().contentLength())
                .body(file.getFile());
    }
}
