package com.lifelink.file.controller;

import com.lifelink.common.Result;
import com.lifelink.file.dto.FileUploadResponse;
import com.lifelink.file.service.FileService;
import com.lifelink.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public Result<FileUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(fileService.uploadDailyImage(file, loginUser.getId()));
    }
}
