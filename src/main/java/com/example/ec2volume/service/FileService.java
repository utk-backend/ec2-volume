package com.example.ec2volume.service;

import com.example.ec2volume.dto.FileDetails;
import com.example.ec2volume.enums.SupportedFileSystems;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileService {
    FileDetails writeFile(MultipartFile file, SupportedFileSystems filesystem) throws IOException;
    FileDetails writeFile(String content, String filename, SupportedFileSystems filesystem);
    Path getFile(String filename, SupportedFileSystems fileSystems) throws IOException;
}
