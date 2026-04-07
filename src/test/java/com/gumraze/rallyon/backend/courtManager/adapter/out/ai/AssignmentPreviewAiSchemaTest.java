package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class AssignmentPreviewAiSchemaTest {

    @Test
    @DisplayName("assignment preview schema는 루프 필수 필드를 포함한다")
    void schema_containsRequiredRootProperties() {
        // given: assignment preview json schema 준비
        String schema = AssignmentPreviewAiSchema.ASSIGNMENT_PREVIEW_JSON_SCHEMA;

        // then: 루트 필수 필드 포함 검증
        then(schema).contains("\"rounds\"");
        then(schema).contains("\"warnings\"");
        then(schema).contains("\"type\": \"object\"");
        then(schema).contains("\"required\": [\"rounds\", \"warnings\"]");
        then(schema).contains("\"additionalProperties\": false");
        then(schema).contains("\"roundNumber\"");
        then(schema).contains("\"courts\"");
        then(schema).contains("\"required\": [\"roundNumber\", \"courts\"]");
        then(schema).contains("\"items\"");
        then(schema).contains("\"type\": \"object\"");

    }
}
