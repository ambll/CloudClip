package dev.ambll.cloudclip.service;

import dev.ambll.cloudclip.exception.FileNotSavedException;
import dev.ambll.cloudclip.exception.StoredFileNotFoundException;
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
            throw new IllegalStateException(
                    "Could not initialize file storage at: " + this.storagePath,
                    e
            );
        }
    }

    public String save(MultipartFile file) {
        try{
            String fileName = UUID.randomUUID().toString();

            Path targetPath = storagePath.resolve(fileName);

            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath);
            }

            return targetPath.toString();
        } catch (IOException e) {
            throw new FileNotSavedException(e);
        }
    }

    public Resource load(String filePath) {
        if (!Files.exists(Path.of(filePath))) {
            throw new StoredFileNotFoundException();
        }

        Resource file = new FileSystemResource(Path.of(filePath));
        return file;
    }

    public void delete(String filePath) throws IOException {
        Files.deleteIfExists(Path.of(filePath));
    }
}