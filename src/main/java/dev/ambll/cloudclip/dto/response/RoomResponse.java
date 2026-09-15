package dev.ambll.cloudclip.dto.response;

import dev.ambll.cloudclip.enums.Visibility;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RoomResponse {
    private String roomKey;
    private String name;
    private Visibility visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
}
