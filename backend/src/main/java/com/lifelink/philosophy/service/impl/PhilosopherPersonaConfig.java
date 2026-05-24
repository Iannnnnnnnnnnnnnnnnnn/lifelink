package com.lifelink.philosophy.service.impl;

public record PhilosopherPersonaConfig(
        String code,
        String roleType,
        String responseLayout,
        String identityBriefZh,
        String identityBriefEn,
        String sourceBackgroundZh,
        String sourceBackgroundEn,
        String coreIdeasZh,
        String coreIdeasEn,
        String representativeWorksZh,
        String representativeWorksEn,
        String thinkingStyleZh,
        String thinkingStyleEn,
        String speakingStyleZh,
        String speakingStyleEn,
        String commonAnglesZh,
        String commonAnglesEn,
        String avoidRulesZh,
        String avoidRulesEn,
        String multiPerspectivePromptZh,
        String multiPerspectivePromptEn,
        String chatPromptZh,
        String chatPromptEn
) {
    String identityBrief(boolean zh) {
        return zh ? identityBriefZh : identityBriefEn;
    }

    String sourceBackground(boolean zh) {
        return zh ? sourceBackgroundZh : sourceBackgroundEn;
    }

    String coreIdeas(boolean zh) {
        return zh ? coreIdeasZh : coreIdeasEn;
    }

    String representativeWorks(boolean zh) {
        return zh ? representativeWorksZh : representativeWorksEn;
    }

    String thinkingStyle(boolean zh) {
        return zh ? thinkingStyleZh : thinkingStyleEn;
    }

    String speakingStyle(boolean zh) {
        return zh ? speakingStyleZh : speakingStyleEn;
    }

    String commonAngles(boolean zh) {
        return zh ? commonAnglesZh : commonAnglesEn;
    }

    String avoidRules(boolean zh) {
        return zh ? avoidRulesZh : avoidRulesEn;
    }

    String multiPerspectivePrompt(boolean zh) {
        return zh ? multiPerspectivePromptZh : multiPerspectivePromptEn;
    }

    String chatPrompt(boolean zh) {
        return zh ? chatPromptZh : chatPromptEn;
    }
}
