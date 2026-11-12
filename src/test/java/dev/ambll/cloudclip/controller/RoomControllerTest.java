package dev.ambll.cloudclip.controller;

import dev.ambll.cloudclip.dto.request.*;
import dev.ambll.cloudclip.dto.response.RoomResponse;
import dev.ambll.cloudclip.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RoomControllerTest {
    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roomController).build();
        objectMapper = new ObjectMapper();
    }

    /*  createRoom  */
    @Test
    void createRoom_success() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest();
        request.setName("test-room");
        request.setOwnerPassword("test-owner-password");

        RoomResponse response = new RoomResponse();
        response.setName("test-room");
        response.setRoomKey("test-room-key");

        Mockito.when(roomService.createRoom(Mockito.any(CreateRoomRequest.class))).thenReturn(response);

        String requestJson = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("test-room"))
                .andExpect(jsonPath("$.roomKey").value("test-room-key"));

        Mockito.verify(roomService).createRoom(Mockito.any(CreateRoomRequest.class));
    }

    @Test
    void createRoom_invalidArguments() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest();
        request.setName("test-room");
        request.setOwnerPassword("test-owner-password");

        RoomResponse response = new RoomResponse();
        response.setName("test-room");
        response.setRoomKey("test-room-key");

        Mockito.when(roomService.createRoom(Mockito.any(CreateRoomRequest.class))).thenReturn(response);

        String requestJson = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("test-room"))
                .andExpect(jsonPath("$.roomKey").value("test-room-key"));

        Mockito.verify(roomService).createRoom(Mockito.any(CreateRoomRequest.class));
    }

    /*  getRoomByKey  */
    @Test
    void getRoomByKey_success() throws Exception {
        String testRoomKey = "test-room-key";
        RoomResponse response = new RoomResponse();
        response.setName("test-room");
        response.setRoomKey(testRoomKey);

        Mockito.when(roomService.getRoom(testRoomKey)).thenReturn(response);

        mockMvc.perform(get("/rooms/{roomKey}", testRoomKey))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("test-room"))
                .andExpect(jsonPath("$.roomKey").value("test-room-key"));

        Mockito.verify(roomService).getRoom(Mockito.eq(testRoomKey));
    }

    /*  deleteRoom  */
    @Test
    void deleteRoom_success() throws Exception {
        String testRoomKey = "test-room-key";
        String ownerPassword = "owner-password";
        RoomAccessRequest request = new RoomAccessRequest();
        request.setPassword(ownerPassword);

        String requestJson = objectMapper.writeValueAsString(request);
        mockMvc.perform(delete("/rooms/{roomKey}", testRoomKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());

        Mockito.verify(roomService).deleteRoom(Mockito.eq(testRoomKey), Mockito.any(RoomAccessRequest.class));
    }

    /*  renameRoom  */
    @Test
    void renameRoom_success() throws Exception {
        String testRoomKey = "test-room-key";
        String ownerPassword = "owner-password";
        String newRoomName = "new-room-name";
        RenameRoomRequest request = new RenameRoomRequest();
        request.setOwnerPassword(ownerPassword);
        request.setNewName(newRoomName);

        String requestJson = objectMapper.writeValueAsString(request);
        mockMvc.perform(patch("/rooms/{roomKey}/name", testRoomKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());

        Mockito.verify(roomService).renameRoom(Mockito.eq(testRoomKey), Mockito.any(RenameRoomRequest.class));
    }

    /*  updateRoomPassword  */
    @Test
    void updateRoomPassword_success() throws Exception {
        String testRoomKey = "test-room-key";
        String ownerPassword = "owner-password";
        String newRoomPassword = "new-room-password";
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOwnerPassword(ownerPassword);
        request.setNewPassword(newRoomPassword);

        String requestJson = objectMapper.writeValueAsString(request);
        mockMvc.perform(patch("/rooms/{roomKey}/password", testRoomKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());

        Mockito.verify(roomService).updateRoomPassword(Mockito.eq(testRoomKey), Mockito.any(UpdatePasswordRequest.class));
    }

    /*  updateOwnerRoomPassword  */
    @Test
    void updateOwnerRoomPassword_success() throws Exception {
        String testRoomKey = "test-room-key";
        String ownerPassword = "owner-password";
        String newOwnerPassword = "new-owner-password";
        UpdateOwnerPasswordRequest request = new UpdateOwnerPasswordRequest();
        request.setOwnerPassword(ownerPassword);
        request.setNewPassword(newOwnerPassword);

        String requestJson = objectMapper.writeValueAsString(request);
        mockMvc.perform(patch("/rooms/{roomKey}/owner-password", testRoomKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());

        Mockito.verify(roomService).updateOwnerRoomPassword(Mockito.eq(testRoomKey), Mockito.any(UpdateOwnerPasswordRequest.class));
    }

    /*  updateRoomExpiration  */
    @Test
    void updateRoomExpiration_success() throws Exception {
        String testRoomKey = "test-room-key";
        String ownerPassword = "owner-password";
        LocalDateTime newRoomExpiration = LocalDateTime.now().plusDays(1);
        UpdateExpirationRequest request = new UpdateExpirationRequest();
        request.setOwnerPassword(ownerPassword);
        request.setExpiresAt(newRoomExpiration);

        String requestJson = objectMapper.writeValueAsString(request);
        mockMvc.perform(patch("/rooms/{roomKey}/expiration", testRoomKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());

        Mockito.verify(roomService).updateRoomExpiration(Mockito.eq(testRoomKey), Mockito.any(UpdateExpirationRequest.class));
    }

    /* accessRoom */
    @Test
    void accessRoom_success() throws Exception {
        String testRoomKey = "test-room-key";
        String password = "room-password";

        RoomAccessRequest request = new RoomAccessRequest();
        request.setPassword(password);

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/rooms/{roomKey}/access", testRoomKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("room:" + testRoomKey, true));

        Mockito.verify(roomService).accessRoom(
                Mockito.eq(testRoomKey),
                Mockito.any(RoomAccessRequest.class)
        );
    }
}
