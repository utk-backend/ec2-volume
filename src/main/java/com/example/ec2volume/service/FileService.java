package com.example.ec2volume.service;

import com.example.ec2volume.dto.FileDetails;

public interface FileService {
    FileDetails writeFile(String content, String filename);
}
