package com.example.ec2volume.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileDetails {
    private String filename;
    private long filesize;
    private String contentType;
    private String filepath;
}
