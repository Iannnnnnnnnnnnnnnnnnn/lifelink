package com.lifelink.relationship.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("relationship_invites")
public class RelationshipInvite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long relationshipId;

    private String inviteCode;

    private Long inviterId;

    private String status;

    private LocalDateTime expireAt;

    private LocalDateTime createdAt;
}
