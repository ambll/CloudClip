package dev.ambll.cloudclip.dto.response;

import dev.ambll.cloudclip.enums.ItemType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ItemResponse {
    private Long id;
    private ItemType type;
    private String text;
    private String fileName;
    private String downloadUrl;
    private LocalDateTime createdAt;
}
