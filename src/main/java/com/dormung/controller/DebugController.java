package com.dormung.controller;

import com.dormung.service.VisitJejuApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * API 클라이언트 디버깅용 컨트롤러
 */
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class DebugController {
    private final VisitJejuApiClient visitJejuApiClient;

    @GetMapping("/status")
    public Map<String, Object> getApiStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", System.currentTimeMillis());
        result.put("status", visitJejuApiClient.getApiStatus());
        return result;
    }

    @GetMapping("/connection")
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", System.currentTimeMillis());
        result.put("connected", visitJejuApiClient.testConnection());
        result.put("apiKeyValid", visitJejuApiClient.validateApiKey());
        return result;
    }

    @GetMapping("/content/{contentId}")
    public Map<String, Object> getContent(@PathVariable String contentId) {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", System.currentTimeMillis());
        result.put("contentId", contentId);
        result.put("data", visitJejuApiClient.getContentById(contentId));
        return result;
    }
}