package com.lifelink.focus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FocusStatsResponse {

    private Integer totalFocusMinutes;

    private Integer completedPomodoros;

    private Integer abandonedPomodoros;

    private Double completionRate;

    private Integer currentStreak;

    private Integer weekFocusMinutes;

    private List<TopTodoResponse> topTodos;

    private List<DailyTrendResponse> dailyTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopTodoResponse {

        private Long todoId;

        private String todoTitle;

        private Integer focusMinutes;

        private Integer sessionsCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrendResponse {

        private LocalDate date;

        private Integer focusMinutes;

        private Integer completedPomodoros;
    }
}
