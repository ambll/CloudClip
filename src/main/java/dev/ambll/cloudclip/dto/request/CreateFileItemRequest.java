package dev.ambll.cloudclip.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateFileItemRequest {
    private MultipartFile file;
}
