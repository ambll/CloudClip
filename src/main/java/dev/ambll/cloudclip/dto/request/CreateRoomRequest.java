package dev.ambll.cloudclip.dto.request;

import dev.ambll.cloudclip.enums.Visibility;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateRoomRequest {
    private String name;
    private String password;
    private Visibility visibility;
    private LocalDateTime expiresAt;
}
