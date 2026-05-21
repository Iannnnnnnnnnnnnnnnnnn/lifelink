package com.lifelink.philosophy.service;

import com.lifelink.philosophy.dto.PhilosophyResponseItem;
import com.lifelink.philosophy.entity.Philosopher;

public interface PhilosophyAiService {

    PhilosophyResponseItem generate(String question, Philosopher philosopher, String language);
}
