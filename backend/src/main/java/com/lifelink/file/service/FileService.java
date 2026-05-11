package com.lifelink.file.service;

import com.lifelink.file.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadResponse uploadDailyImage(MultipartFile file, Long userId);
}
