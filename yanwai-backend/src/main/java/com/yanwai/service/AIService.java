package com.yanwai.service;

import com.yanwai.dto.AnalysisResultDTO;

public interface AIService {
    AnalysisResultDTO analyze(String dialogText);
}
