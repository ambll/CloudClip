
package dev.ambll.cloudclip.controller;

import dev.ambll.cloudclip.dto.request.CreateFileItemRequest;
import dev.ambll.cloudclip.dto.request.CreateTextItemRequest;
import dev.ambll.cloudclip.dto.response.FileDownload;
import dev.ambll.cloudclip.dto.response.ItemResponse;
import dev.ambll.cloudclip.enums.ItemType;
import dev.ambll.cloudclip.exception.GlobalExceptionHandler;
import dev.ambll.cloudclip.exception.RoomAccessDeniedException;
import dev.ambll.cloudclip.service.ItemService;
import dev.ambll.cloudclip.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private ItemController itemController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final String testRoomKey = "test-room";
    private final Long testItemId = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void createTextItem_success() throws Exception {
        CreateTextItemRequest request = new CreateTextItemRequest();
        request.setText("Hello world!");

        ItemResponse response = new ItemResponse();
        response.setId(testItemId);
        response.setType(ItemType.TEXT);
        response.setText("Hello world!");
        response.setCreatedAt(LocalDateTime.now());

        when(itemService.createTextItem(eq(testRoomKey), any(CreateTextItemRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/rooms/{roomKey}/items/text", testRoomKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testItemId))
                .andExpect(jsonPath("$.type").value("TEXT"))
                .andExpect(jsonPath("$.text").value("Hello world!"));

        verify(roomService).requireAccess(eq(testRoomKey), any());
        verify(itemService).createTextItem(eq(testRoomKey), any(CreateTextItemRequest.class));
    }

    @Test
    void createTextItem_accessDenied() throws Exception {
        CreateTextItemRequest request = new CreateTextItemRequest();
        request.setText("Hello world!");

        doThrow(RoomAccessDeniedException.class)
                .when(roomService).requireAccess(eq(testRoomKey), any());

        mockMvc.perform(post("/rooms/{roomKey}/items/text", testRoomKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(roomService).requireAccess(eq(testRoomKey), any());
        verifyNoInteractions(itemService);
    }

    @Test
    void createFileItem_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                "text/plain",
                "Hello from file!".getBytes()
        );

        ItemResponse response = new ItemResponse();
        response.setId(testItemId);
        response.setType(ItemType.FILE);
        response.setFileName("hello.txt");
        response.setDownloadUrl("/rooms/" + testRoomKey + "/items/" + testItemId + "/file");
        response.setCreatedAt(LocalDateTime.now());

        when(itemService.createFileItem(eq(testRoomKey), any(CreateFileItemRequest.class)))
                .thenReturn(response);

        mockMvc.perform(multipart("/rooms/{roomKey}/items/file", testRoomKey)
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testItemId))
                .andExpect(jsonPath("$.type").value("FILE"))
                .andExpect(jsonPath("$.fileName").value("hello.txt"))
                .andExpect(jsonPath("$.downloadUrl")
                        .value("/rooms/" + testRoomKey + "/items/" + testItemId + "/file"));

        verify(roomService).requireAccess(eq(testRoomKey), any());
        verify(itemService).createFileItem(eq(testRoomKey), any(CreateFileItemRequest.class));
    }

    @Test
    void getItems_success() throws Exception {
        ItemResponse firstItem = new ItemResponse();
        firstItem.setId(1L);
        firstItem.setType(ItemType.TEXT);
        firstItem.setText("First item");

        ItemResponse secondItem = new ItemResponse();
        secondItem.setId(2L);
        secondItem.setType(ItemType.FILE);
        secondItem.setFileName("image.png");

        when(itemService.getItems(testRoomKey))
                .thenReturn(List.of(firstItem, secondItem));

        mockMvc.perform(get("/rooms/{roomKey}/items", testRoomKey))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("First item"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].fileName").value("image.png"));

        verify(roomService).requireAccess(eq(testRoomKey), any());
        verify(itemService).getItems(testRoomKey);
    }

    @Test
    void deleteItem_success() throws Exception {
        mockMvc.perform(delete("/rooms/{roomKey}/items/{itemId}", testRoomKey, testItemId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(roomService).requireAccess(eq(testRoomKey), any());
        verify(itemService).deleteItem(testRoomKey, testItemId);
    }

    @Test
    void getFile_success() throws Exception {
        byte[] fileContent = "Hello from downloaded file!".getBytes();
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        FileDownload fileDownload = new FileDownload();
        fileDownload.setFile(resource);
        fileDownload.setFileName("hello.txt");
        fileDownload.setContentType(MediaType.TEXT_PLAIN);

        when(itemService.getFile(testRoomKey, testItemId))
                .thenReturn(fileDownload);

        mockMvc.perform(get("/rooms/{roomKey}/items/{itemId}/file", testRoomKey, testItemId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("hello.txt")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(header().longValue("Content-Length", fileContent.length))
                .andExpect(content().bytes(fileContent));

        verify(roomService).requireAccess(eq(testRoomKey), any());
        verify(itemService).getFile(testRoomKey, testItemId);
    }
}
