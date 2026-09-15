package dev.ambll.cloudclip.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

@Getter
@Setter
public class FileDownload {
    Resource file;
    String fileName;
    MediaType contentType;
}
