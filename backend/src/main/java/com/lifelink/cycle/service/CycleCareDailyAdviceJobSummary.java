package com.lifelink.cycle.service;

import lombok.Getter;

@Getter
public class CycleCareDailyAdviceJobSummary {

    private int total;

    private int success;

    private int failed;

    private int skipped;

    public void incrementTotal() {
        total++;
    }

    public void incrementSuccess() {
        success++;
    }

    public void incrementFailed() {
        failed++;
    }

    public void incrementSkipped() {
        skipped++;
    }
}
