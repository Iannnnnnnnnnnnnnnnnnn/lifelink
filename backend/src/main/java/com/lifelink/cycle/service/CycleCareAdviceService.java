package com.lifelink.cycle.service;

import com.lifelink.cycle.entity.CycleDailyLog;
import org.springframework.stereotype.Service;

@Service
public class CycleCareAdviceService {

    public static final String DISCLAIMER = "本功能仅用于生活记录与关怀提醒，不构成医学诊断、治疗建议或避孕/备孕依据。如出现持续异常、剧烈疼痛、大量出血或其他明显不适，请及时咨询医生。";

    public CycleCareAdvice buildAdvice(String phase, CycleDailyLog todayLog) {
        Integer painLevel = todayLog == null ? null : todayLog.getPainLevel();
        if (CycleCarePredictionService.MENSTRUATION.equals(phase)) {
            return new CycleCareAdvice(
                    "今天适合温柔一点",
                    "如果身体有不适，可以先把节奏放慢一些。",
                    "建议选择宽松、保暖、舒适的衣物，注意腹部和腰部保暖，避免长时间穿过紧衣物。",
                    "可以选择温热、清淡、易消化的食物，补充水分，也可以适量选择富含铁和蛋白质的食物。",
                    painLevel != null && painLevel >= 7
                            ? "疼痛明显时建议减少高强度活动，适当休息；如果疼痛剧烈或反复出现，建议咨询医生。"
                            : "减少高强度运动，保持睡眠，给自己一点恢复时间。",
                    "情绪起伏时可以简单记录感受，先不用急着评价自己。",
                    "可以主动分担一些事情，准备热水、暖宝宝或清淡饮食，少催促，多关心。"
            );
        }
        if (CycleCarePredictionService.LUTEAL.equals(phase)) {
            return new CycleCareAdvice(
                    "留意经前变化",
                    "经前期身体和情绪可能更敏感，可以提前给自己留一点余地。",
                    "选择舒适、不压迫腹部的衣物，减少束缚感。",
                    "保持规律饮食，减少过量糖分、咖啡因和重口味，可以增加水果、坚果、全谷物等。",
                    "留意睡眠，做轻量运动或散步，情绪波动时可以做简单记录。",
                    "如果感到烦躁或低落，可以先暂停争执，等状态平稳后再沟通。",
                    "多一点耐心和确认感，主动分担琐事，不用开玩笑化解对方的不舒服。"
            );
        }
        if (CycleCarePredictionService.OVULATION.equals(phase)) {
            return new CycleCareAdvice(
                    "今天是估算排卵期附近",
                    "这只是周期估算，不能作为避孕或备孕依据。",
                    "选择轻便舒适的衣物，关注身体细微信号。",
                    "保持均衡饮食和水分摄入。",
                    "可以安排适度活动，但不用过度消耗。",
                    "观察身体变化即可，不需要因为估算结果过度紧张。",
                    "可以温和提醒对方休息和补水，但不要把估算当成确定结论。"
            );
        }
        if (CycleCarePredictionService.FOLLICULAR.equals(phase)) {
            return new CycleCareAdvice(
                    "适合慢慢恢复节奏",
                    "这段时间通常更适合安排轻量计划。",
                    "舒适、便于活动的衣物即可。",
                    "保持规律作息和均衡饮食。",
                    "可以逐步恢复运动和计划，但仍以身体感受为准。",
                    "记录精力恢复情况，找到适合自己的节奏。",
                    "可以一起安排轻松活动，保持稳定陪伴。"
            );
        }
        return new CycleCareAdvice(
                "先完善周期档案",
                "记录最近一次开始日期后，系统会给出更有参考价值的关怀提醒。",
                "根据天气和体感选择舒适衣物。",
                "保持规律饮食和水分摄入。",
                "先保证睡眠和基本休息。",
                "可以从简单记录今天状态开始。",
                "可以多表达关心，尊重对方是否愿意分享。"
        );
    }
}
