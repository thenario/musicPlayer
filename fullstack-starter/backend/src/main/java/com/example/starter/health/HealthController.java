package com.example.starter.health;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.starter.common.ApiResponse;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    @GetMapping
    public ApiResponse<Map<String, String>> health() { return ApiResponse.success(Map.of("status", "up")); }
}
