package com.example.ec2volume.service;

import com.example.ec2volume.dto.FileDetails;

public class EFSFileService implements FileService{

    @Override
    public FileDetails writeFile(String content, String filename) {
        return new FileDetails();
    }
}
