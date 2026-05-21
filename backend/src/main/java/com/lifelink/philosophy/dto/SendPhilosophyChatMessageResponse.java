package com.lifelink.philosophy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendPhilosophyChatMessageResponse {

    private PhilosophyChatMessageResponse userMessage;

    private PhilosophyChatMessageResponse assistantMessage;
}
