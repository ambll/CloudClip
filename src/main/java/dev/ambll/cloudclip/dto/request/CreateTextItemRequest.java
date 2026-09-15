package dev.ambll.cloudclip.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateTextItemRequest {
    private String text;
}
