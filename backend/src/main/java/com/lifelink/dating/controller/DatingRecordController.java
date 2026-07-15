package com.lifelink.dating.controller;

import com.lifelink.common.Result;
import com.lifelink.dating.dto.CreateDatingRecordRequest;
import com.lifelink.dating.dto.DatingRecordListResponse;
import com.lifelink.dating.dto.DatingRecordResponse;
import com.lifelink.dating.dto.UpdateDatingRecordRequest;
import com.lifelink.dating.service.DatingRecordService;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DatingRecordController {

    private final DatingRecordService datingRecordService;

    @PostMapping("/api/dating-records")
    public Result<DatingRecordResponse> createDatingRecord(
            @Valid @RequestBody CreateDatingRecordRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(datingRecordService.createDatingRecord(request, loginUser.getId()));
    }

    @GetMapping("/api/relationships/{relationshipId}/dating-records")
    public Result<DatingRecordListResponse> listDatingRecords(
            @PathVariable Long relationshipId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(datingRecordService.listDatingRecords(relationshipId, loginUser.getId()));
    }

    @GetMapping("/api/dating-records/{id}")
    public Result<DatingRecordResponse> getDatingRecord(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(datingRecordService.getDatingRecord(id, loginUser.getId()));
    }

    @PutMapping("/api/dating-records/{id}")
    public Result<DatingRecordResponse> updateDatingRecord(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDatingRecordRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(datingRecordService.updateDatingRecord(id, request, loginUser.getId()));
    }

    @DeleteMapping("/api/dating-records/{id}")
    public Result<Void> deleteDatingRecord(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        datingRecordService.deleteDatingRecord(id, loginUser.getId());
        return Result.success();
    }
}
