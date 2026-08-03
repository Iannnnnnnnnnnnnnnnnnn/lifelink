package com.lifelink.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemoryBuildResponse {

    private int sourceCount;

    private int chunkCount;
}
