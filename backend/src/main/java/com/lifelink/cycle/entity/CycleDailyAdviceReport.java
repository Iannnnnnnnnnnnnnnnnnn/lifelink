package com.lifelink.cycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lifelink.common.typehandler.JsonbStringTypeHandler;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName(value = "cycle_daily_advice_reports", autoResultMap = true)
public class CycleDailyAdviceReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long loverSpaceId;

    private LocalDate reportDate;

    private String phase;

    private String phaseLabel;

    @TableField("is_predicted_phase")
    private Boolean predictedPhase;

    private String summary;

    private String bodyStatusSummary;

    private String flowSummary;

    private String painSummary;

    private String moodSummary;

    private String symptomSummary;

    private String clothingAdvice;

    private String foodAdvice;

    private String restAdvice;

    private String moodAdvice;

    private String partnerAdvice;

    private String warningSummary;

    private String riskLevel;

    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String warningTypes;

    private String shareLevelSnapshot;

    private String partnerVisibleSummary;

    private String sourceType;

    private Boolean aiGenerated;

    private String aiModel;

    private String promptVersion;

    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String rawAiResponse;

    private String status;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
