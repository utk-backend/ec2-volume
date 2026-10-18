package com.example.ec2volume.service;

import com.example.ec2volume.dto.FileDetails;
import com.example.ec2volume.enums.SupportedFileSystems;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileServiceImpl implements FileService {
    private final Path ebsStorageDirectory;
    private final Path efsStorageDirectory;

    public FileServiceImpl(@Value("${ebs.mount.directory}") String ebsDirectory, @Value("${efs.mount.directory}") String efsDirectory) {
        this.ebsStorageDirectory = Paths.get(ebsDirectory).toAbsolutePath().normalize();
        this.efsStorageDirectory = Paths.get(efsDirectory).toAbsolutePath().normalize();
    }

    @Override
    public FileDetails writeFile(MultipartFile file, SupportedFileSystems filesystem) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("File name is required");
        }
        // normalizing the filename
        String filename = Paths.get(originalFilename.replace('\\', '/')).getFileName().toString();

        if (filename.isBlank() || filename.equals(".") || filename.equals("..")) {
            throw new IllegalArgumentException("Invalid filename");
        }

        Path storageDirectory = getFileSystemStorage(filesystem);
        Files.createDirectories(storageDirectory);

        Path destination = storageDirectory.resolve(filename).normalize();

        if (!destination.getParent().equals(storageDirectory)) {
            throw new IllegalArgumentException("Invalid filename");
        }

        // Do not silently overwrite an existing file.
        Files.copy(file.getInputStream(), destination);

        return new FileDetails(filename, Files.size(destination), file.getContentType(), destination.toString());
    }

    @Override
    public FileDetails writeFile(String content, String filename, SupportedFileSystems filesystem) {
        return null;
    }

    @Override
    public Path getFile(String filename, SupportedFileSystems filesystem) throws IOException {

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }

        // Reject path traversal and directory components.
        if (filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }

        Path storageDirectory = getFileSystemStorage(filesystem);
        Path filePath = storageDirectory.resolve(filename).normalize();

        if (!filePath.startsWith(storageDirectory)) {
            throw new IllegalArgumentException("Invalid filename");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new java.nio.file.NoSuchFileException(filename);
        }

        return filePath;
    }


    private Path getFileSystemStorage(SupportedFileSystems filesystem) {
        if (filesystem == SupportedFileSystems.EBS) {
            return ebsStorageDirectory;
        } else if (filesystem == SupportedFileSystems.EFS) {
            return efsStorageDirectory;
        } else {
            throw new IllegalArgumentException("Illegal File system requested");
        }
    }

}
