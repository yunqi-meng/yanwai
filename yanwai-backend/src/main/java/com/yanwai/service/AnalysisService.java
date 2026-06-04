package com.yanwai.service;

import com.yanwai.dto.*;

public interface AnalysisService {
    DecodeResponse decode(Long userId, String text);
    DecodeResponse decodeWithImage(Long userId, String base64Image, String dialogText);
}
