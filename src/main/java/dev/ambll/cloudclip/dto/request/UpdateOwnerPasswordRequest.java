package dev.ambll.cloudclip.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOwnerPasswordRequest {
    @NotBlank
    private String ownerPassword;
    @NotBlank
    private String newPassword;
}
