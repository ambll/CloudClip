package dev.ambll.cloudclip.controller;

import dev.ambll.cloudclip.dto.request.CreateRoomRequest;
import dev.ambll.cloudclip.dto.request.RoomAccessRequest;
import dev.ambll.cloudclip.dto.response.RoomResponse;
import dev.ambll.cloudclip.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping("/rooms")
    public List<RoomResponse> getRooms() {
        return roomService.listRooms();
    }

    @PostMapping("/rooms")
    public RoomResponse createRoom(@RequestBody CreateRoomRequest request) {
        return roomService.createRoom(request);
    }

    @GetMapping("room/{roomKey}")
    public RoomResponse getRoomByKey(@PathVariable String roomKey) {
        return roomService.getRoom(roomKey);
    }

    @DeleteMapping("room/{roomKey}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomKey) {
        roomService.deleteRoom(roomKey);
        return ResponseEntity.noContent().build(); // Возвращает статус 204 No Content
    }

    @PostMapping("room/{roomKey}/access")
    public void accessRoom(@PathVariable String roomKey, @RequestBody RoomAccessRequest request) {
        roomService.accessRoom(roomKey, request);
    }
}
