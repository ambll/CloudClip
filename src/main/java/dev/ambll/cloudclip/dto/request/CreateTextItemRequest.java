package dev.ambll.cloudclip.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateTextItemRequest {
    @NotBlank
    private String text;
}
