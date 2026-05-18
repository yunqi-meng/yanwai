package com.yanwai.service;

import com.yanwai.dto.*;

public interface AnalysisService {
    DecodeResponse decode(Long userId, String text);
}
