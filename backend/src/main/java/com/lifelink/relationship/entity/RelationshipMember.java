package com.lifelink.relationship.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("relationship_members")
public class RelationshipMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long relationshipId;

    private Long userId;

    private String role;

    private String nickname;

    private LocalDateTime joinedAt;
}
