package com.codemate.domain.study.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "진행 방식: ONLINE(온라인), OFFLINE(오프라인)")
public enum MeetingType {
    ONLINE,
    OFFLINE
}
