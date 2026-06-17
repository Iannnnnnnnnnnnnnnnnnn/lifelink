package com.lifelink.file.service;

import com.lifelink.config.MinioProperties;
import com.lifelink.file.entity.FileResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FileUrlService {

    private final MinioProperties minioProperties;

    public String buildPublicUrl(FileResource resource) {
        if (resource == null) {
            return null;
        }
        return buildPublicUrl(resource.getBucket(), resource.getObjectKey());
    }

    public String buildPublicUrl(String bucket, String objectKey) {
        if (!StringUtils.hasText(bucket) || !StringUtils.hasText(objectKey)) {
            return null;
        }

        String endpoint = resolvePublicEndpoint();
        String normalizedEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        String normalizedObjectKey = objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
        if (!StringUtils.hasText(normalizedEndpoint)) {
            return "/" + bucket + "/" + normalizedObjectKey;
        }
        return normalizedEndpoint + "/" + bucket + "/" + normalizedObjectKey;
    }

    private String resolvePublicEndpoint() {
        if (StringUtils.hasText(minioProperties.getPublicEndpoint())) {
            return minioProperties.getPublicEndpoint().trim();
        }
        return "";
    }
}
