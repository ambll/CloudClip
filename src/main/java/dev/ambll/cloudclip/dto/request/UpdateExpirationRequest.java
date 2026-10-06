package dev.ambll.cloudclip.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateExpirationRequest {
    @NotBlank
    private String ownerPassword;
    @Future
    private LocalDateTime expiresAt;
}
