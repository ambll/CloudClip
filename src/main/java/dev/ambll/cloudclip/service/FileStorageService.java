package dev.ambll.cloudclip.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path storagePath;

    public FileStorageService(
            @Value("${file-storage.path}") String storagePath
    ) {
        this.storagePath = Paths.get(storagePath)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.storagePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    public String save(MultipartFile file) {
        try{
            String fileName = UUID.randomUUID().toString();

            Path targetPath = storagePath.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    targetPath
            );

            return targetPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Could not save file", e);
        }
    }

    public Resource load(String filePath) {
        if (!Files.exists(Path.of(filePath))) {
            throw new RuntimeException("File not found");
        }

        Resource file = new FileSystemResource(Path.of(filePath));
        return file;
    }

    public void delete(String filePath) throws IOException {
        Files.deleteIfExists(Path.of(filePath));
    }
}