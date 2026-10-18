
package com.example.ec2volume.controller;

import com.example.ec2volume.dto.FileDetails;
import com.example.ec2volume.enums.SupportedFileSystems;
import com.example.ec2volume.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Files;

@RestController
@Slf4j
@RequestMapping("/api")
public class StorageController {

    private final FileService fileService;

    public StorageController(FileService fileService) {
        this.fileService = fileService;
    }

    // Upload a file to EBS or EFS
    @PostMapping(
            value = "/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<FileDetails> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("filesystem") SupportedFileSystems filesystem) {

        try {
            FileDetails details = fileService.writeFile(file, filesystem);
            return ResponseEntity.status(HttpStatus.CREATED).body(details);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (IOException e) {
            log.error("Failed to store file on filesystem: {}", filesystem, e);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to store file");
        }
    }

    // Download a file from EBS or EFS
    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String filename,
            @RequestParam("filesystem") SupportedFileSystems filesystem) {

        try {
            Path path = fileService.getFile(filename, filesystem);
            Resource resource = new FileSystemResource(path);

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(Files.size(path))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + path.getFileName() + "\""
                    )
                    .body(resource);

        } catch (NoSuchFileException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "File not found");

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve file");
        }
    }

    // Retrieve file metadata from EBS or EFS
    @GetMapping("/files/{filename}/details")
    public ResponseEntity<FileDetails> getFileMetadata(
            @PathVariable("filename") String filename,
            @RequestParam("filesystem") SupportedFileSystems filesystem) {

        try {
            Path path = fileService.getFile(filename, filesystem);

            FileDetails details = new FileDetails(
                    path.getFileName().toString(),
                    Files.size(path),
                    Files.probeContentType(path),
                    path.toString()
            );

            return ResponseEntity.ok(details);

        } catch (NoSuchFileException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "File not found");

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve file metadata");
        }
    }
}
