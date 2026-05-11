package com.lifelink.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_resources")
public class FileResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String bucket;

    private String objectKey;

    private String originalName;

    private String contentType;

    private Long fileSize;

    private String fileUrl;

    private LocalDateTime createdAt;
}
