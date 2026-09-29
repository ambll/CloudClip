package dev.ambll.cloudclip.dto.request;

import dev.ambll.cloudclip.enums.Visibility;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateRoomRequest {
    @NotBlank
    private String name;
    private String password;
    @NotNull
    private Visibility visibility;
    @Future
    private LocalDateTime expiresAt;
}
