package com.example.AIHealth.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    // Spring Security에서는 권한 코드에 항상 "ROLE_" 접두사가 앞에 붙어야 합니다.
    // 각 Enum 상수에 key 값을 부여합니다.
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String title;
}