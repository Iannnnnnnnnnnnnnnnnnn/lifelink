package com.lifelink.dating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.common.BusinessException;
import com.lifelink.dating.dto.CreateDatingRecordRequest;
import com.lifelink.dating.dto.DatingRecordListResponse;
import com.lifelink.dating.dto.DatingRecordResponse;
import com.lifelink.dating.dto.UpdateDatingRecordRequest;
import com.lifelink.dating.entity.DatingRecord;
import com.lifelink.dating.mapper.DatingRecordMapper;
import com.lifelink.dating.service.DatingRecordService;
import com.lifelink.relationship.entity.Relationship;
import com.lifelink.relationship.mapper.RelationshipMapper;
import com.lifelink.relationship.service.RelationshipPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DatingRecordServiceImpl implements DatingRecordService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DELETED_STATUS = "DELETED";

    private final DatingRecordMapper datingRecordMapper;
    private final RelationshipMapper relationshipMapper;
    private final RelationshipPermissionService relationshipPermissionService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public DatingRecordResponse createDatingRecord(CreateDatingRecordRequest request, Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(request.getRelationshipId(), userId);
        LocalDateTime now = LocalDateTime.now();

        DatingRecord record = new DatingRecord();
        record.setRelationshipId(request.getRelationshipId());
        record.setDatingDate(request.getDatingDate());
        record.setActivities(writeActivities(normalizeActivities(request.getActivities())));
        record.setLocation(trimToNull(request.getLocation()));
        record.setNote(trimToNull(request.getNote()));
        record.setCreatedBy(userId);
        record.setUpdatedBy(userId);
        record.setStatus(ACTIVE_STATUS);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        datingRecordMapper.insert(record);

        return toResponse(record, calculateSequenceNumber(record));
    }

    @Override
    public DatingRecordListResponse listDatingRecords(Long relationshipId, Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(relationshipId, userId);
        List<DatingRecord> records = datingRecordMapper.selectList(new LambdaQueryWrapper<DatingRecord>()
                .eq(DatingRecord::getRelationshipId, relationshipId)
                .eq(DatingRecord::getStatus, ACTIVE_STATUS)
                .orderByAsc(DatingRecord::getDatingDate)
                .orderByAsc(DatingRecord::getId));

        List<DatingRecordResponse> responses = new ArrayList<DatingRecordResponse>();
        for (int index = 0; index < records.size(); index++) {
            responses.add(toResponse(records.get(index), index + 1));
        }
        Collections.reverse(responses);
        return new DatingRecordListResponse(records.size(), responses);
    }

    @Override
    public DatingRecordResponse getDatingRecord(Long id, Long userId) {
        DatingRecord record = requireActiveRecord(id);
        relationshipPermissionService.requireActiveRelationshipMember(record.getRelationshipId(), userId);
        return toResponse(record, calculateSequenceNumber(record));
    }

    @Override
    @Transactional
    public DatingRecordResponse updateDatingRecord(Long id, UpdateDatingRecordRequest request, Long userId) {
        DatingRecord record = requireActiveRecord(id);
        relationshipPermissionService.requireActiveRelationshipMember(record.getRelationshipId(), userId);

        record.setDatingDate(request.getDatingDate());
        record.setActivities(writeActivities(normalizeActivities(request.getActivities())));
        record.setLocation(trimToNull(request.getLocation()));
        record.setNote(trimToNull(request.getNote()));
        record.setUpdatedBy(userId);
        record.setUpdatedAt(LocalDateTime.now());
        datingRecordMapper.updateById(record);

        return toResponse(record, calculateSequenceNumber(record));
    }

    @Override
    @Transactional
    public void deleteDatingRecord(Long id, Long userId) {
        DatingRecord record = requireActiveRecord(id);
        relationshipPermissionService.requireActiveRelationshipMember(record.getRelationshipId(), userId);
        record.setStatus(DELETED_STATUS);
        record.setUpdatedBy(userId);
        record.setUpdatedAt(LocalDateTime.now());
        datingRecordMapper.updateById(record);
    }

    private DatingRecord requireActiveRecord(Long id) {
        DatingRecord record = datingRecordMapper.selectById(id);
        if (record == null || !ACTIVE_STATUS.equals(record.getStatus())) {
            throw new BusinessException(404, "Dating record not found");
        }
        return record;
    }

    private int calculateSequenceNumber(DatingRecord record) {
        Long count = datingRecordMapper.selectCount(new LambdaQueryWrapper<DatingRecord>()
                .eq(DatingRecord::getRelationshipId, record.getRelationshipId())
                .eq(DatingRecord::getStatus, ACTIVE_STATUS)
                .and(wrapper -> wrapper
                        .lt(DatingRecord::getDatingDate, record.getDatingDate())
                        .or(sameDate -> sameDate
                                .eq(DatingRecord::getDatingDate, record.getDatingDate())
                                .le(DatingRecord::getId, record.getId()))));
        return count.intValue();
    }

    private DatingRecordResponse toResponse(DatingRecord record, int sequenceNumber) {
        Relationship relationship = relationshipMapper.selectById(record.getRelationshipId());
        return new DatingRecordResponse(
                record.getId(),
                record.getRelationshipId(),
                relationship == null ? null : relationship.getName(),
                sequenceNumber,
                record.getDatingDate(),
                readActivities(record.getActivities()),
                record.getLocation(),
                record.getNote(),
                record.getCreatedBy(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private List<String> normalizeActivities(List<String> activities) {
        List<String> normalized = new ArrayList<String>();
        for (String activity : activities) {
            String value = activity == null ? null : activity.trim();
            if (StringUtils.hasText(value) && !normalized.contains(value)) {
                normalized.add(value);
            }
        }
        if (normalized.isEmpty()) {
            throw new BusinessException(400, "At least one activity is required");
        }
        return normalized;
    }

    private String writeActivities(List<String> activities) {
        try {
            return objectMapper.writeValueAsString(activities);
        } catch (Exception ex) {
            throw new BusinessException(500, "Failed to save dating activities");
        }
    }

    private List<String> readActivities(String activities) {
        if (!StringUtils.hasText(activities)) {
            return new ArrayList<String>();
        }
        try {
            return objectMapper.readValue(activities, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            return new ArrayList<String>();
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
