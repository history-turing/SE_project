package com.whu.treehole.server;

/* 轻量单元测试用于验证领域枚举解析是否正常。 */

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.whu.treehole.domain.enums.AudienceType;
import com.whu.treehole.domain.enums.TopicScope;
import org.junit.jupiter.api.Test;

class DomainSmokeTest {

    @Test
    void shouldParseAudienceAndScope() {
        assertEquals(AudienceType.HOME, AudienceType.fromLabel("首页"));
        assertEquals(TopicScope.ALUMNI, TopicScope.from("alumni"));
    }
}
