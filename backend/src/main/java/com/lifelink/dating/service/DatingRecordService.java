package com.lifelink.dating.service;

import com.lifelink.dating.dto.CreateDatingRecordRequest;
import com.lifelink.dating.dto.DatingRecordListResponse;
import com.lifelink.dating.dto.DatingRecordResponse;
import com.lifelink.dating.dto.UpdateDatingRecordRequest;

public interface DatingRecordService {

    DatingRecordResponse createDatingRecord(CreateDatingRecordRequest request, Long userId);

    DatingRecordListResponse listDatingRecords(Long relationshipId, Long userId);

    DatingRecordResponse getDatingRecord(Long id, Long userId);

    DatingRecordResponse updateDatingRecord(Long id, UpdateDatingRecordRequest request, Long userId);

    void deleteDatingRecord(Long id, Long userId);
}
