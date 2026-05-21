package com.lifelink.file.service.impl;

import com.lifelink.common.BusinessException;
import com.lifelink.config.MinioProperties;
import com.lifelink.file.dto.FileUploadResponse;
import com.lifelink.file.entity.FileResource;
import com.lifelink.file.mapper.FileResourceMapper;
import com.lifelink.file.service.FileService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<String>(Arrays.asList("jpg", "jpeg", "png", "webp"));
    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<String>(Arrays.asList("image/jpeg", "image/png", "image/webp"));

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final FileResourceMapper fileResourceMapper;

    @Override
    @Transactional
    public FileUploadResponse uploadDailyImage(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "File is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(400, "File size cannot exceed 5MB");
        }

        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "Only jpg, jpeg, png, webp are allowed");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(400, "Unsupported image content type");
        }

        String objectKey = buildObjectKey(extension);
        String bucket = minioProperties.getBucket();
        ensureBucket(bucket);

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception ex) {
            throw new BusinessException(500, "Upload file failed");
        }

        FileResource resource = new FileResource();
        resource.setUserId(userId);
        resource.setBucket(bucket);
        resource.setObjectKey(objectKey);
        resource.setOriginalName(originalName);
        resource.setContentType(contentType);
        resource.setFileSize(file.getSize());
        resource.setFileUrl(buildFileUrl(bucket, objectKey));
        resource.setCreatedAt(LocalDateTime.now());
        fileResourceMapper.insert(resource);

        return new FileUploadResponse(
                resource.getId(),
                resource.getFileUrl(),
                resource.getObjectKey(),
                resource.getOriginalName(),
                resource.getContentType(),
                resource.getFileSize()
        );
    }

    private void ensureBucket(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucket)
                            .config(buildPublicReadPolicy(bucket))
                            .build()
            );
        } catch (Exception ex) {
            throw new BusinessException(500, "Prepare bucket failed");
        }
    }

    private String buildPublicReadPolicy(String bucket) {
        return "{"
                + "\"Version\":\"2012-10-17\","
                + "\"Statement\":[{"
                + "\"Effect\":\"Allow\","
                + "\"Principal\":{\"AWS\":[\"*\"]},"
                + "\"Action\":[\"s3:GetObject\"],"
                + "\"Resource\":[\"arn:aws:s3:::"
                + bucket
                + "/*\"]"
                + "}]"
                + "}";
    }

    private String extractExtension(String originalName) {
        if (!StringUtils.hasText(originalName) || !originalName.contains(".")) {
            throw new BusinessException(400, "Invalid file name");
        }
        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(extension)) {
            throw new BusinessException(400, "Invalid file extension");
        }
        return extension;
    }

    private String buildObjectKey(String extension) {
        LocalDateTime now = LocalDateTime.now();
        return "daily/"
                + now.getYear()
                + "/"
                + String.format("%02d", now.getMonthValue())
                + "/"
                + UUID.randomUUID()
                + "."
                + extension;
    }

    private String buildFileUrl(String bucket, String objectKey) {
        String endpoint = StringUtils.hasText(minioProperties.getPublicEndpoint())
                ? minioProperties.getPublicEndpoint()
                : minioProperties.getEndpoint();
        if (endpoint.endsWith("/")) {
            return endpoint + bucket + "/" + objectKey;
        }
        return endpoint + "/" + bucket + "/" + objectKey;
    }
}
