package dev.ambll.cloudclip.controller;

import dev.ambll.cloudclip.dto.request.CreateRoomRequest;
import dev.ambll.cloudclip.dto.request.RoomAccessRequest;
import dev.ambll.cloudclip.dto.response.RoomResponse;
import dev.ambll.cloudclip.service.RoomService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

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
    public RoomResponse createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return roomService.createRoom(request);
    }

    @GetMapping("/rooms/{roomKey}")
    public RoomResponse getRoomByKey(@PathVariable String roomKey) {
        return roomService.getRoom(roomKey);
    }

    @DeleteMapping("/rooms/{roomKey}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomKey, @Valid @RequestBody RoomAccessRequest request) {
        roomService.deleteRoom(roomKey, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rooms/{roomKey}/access")
    public void accessRoom(
            @PathVariable String roomKey,
            @Valid @RequestBody RoomAccessRequest request,
            HttpSession session
    ) {
        roomService.accessRoom(roomKey, request);

        session.setAttribute(
                "room:" + roomKey,
                true
        );
    }
}
