package dev.ambll.cloudclip.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomAccessRequest {
    @NotBlank
    private String password;
}
