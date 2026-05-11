package com.lifelink.anniversary.service;

import com.lifelink.anniversary.dto.AnniversaryDetailResponse;
import com.lifelink.anniversary.dto.AnniversaryResponse;
import com.lifelink.anniversary.dto.CreateAnniversaryRequest;
import com.lifelink.anniversary.dto.UpdateAnniversaryRequest;

import java.util.List;

public interface AnniversaryService {

    AnniversaryDetailResponse createAnniversary(CreateAnniversaryRequest request, Long userId);

    List<AnniversaryResponse> listAnniversaries(Long relationshipId, String repeatType, String displayType, String keyword, Integer page, Integer size, Long userId);

    AnniversaryDetailResponse getAnniversaryDetail(Long id, Long userId);

    AnniversaryDetailResponse updateAnniversary(Long id, UpdateAnniversaryRequest request, Long userId);

    void deleteAnniversary(Long id, Long userId);
}
