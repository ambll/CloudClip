package dev.ambll.cloudclip.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordRequest {
    @NotBlank
    private String ownerPassword;
    private String newPassword;
}
