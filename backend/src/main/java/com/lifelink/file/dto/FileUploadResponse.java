package com.lifelink.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    private Long fileId;

    private String url;

    private String objectKey;

    private String originalName;

    private String contentType;

    private Long fileSize;
}
