package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
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
        then(schema).contains("\"courtNumber\"");
        then(schema).contains("\"slots\"");
        then(schema).contains("\"required\": [\"courtNumber\", \"slots\"]");
        then(schema).contains("\"type\": [\"integer\", \"null\"]");
        then(schema).contains("\"code\"");
        then(schema).contains("\"message\"");
        then(schema).contains("\"required\": [\"code\", \"message\"]");
        then(schema.split("\"additionalProperties\": false")).hasSizeGreaterThan(2);
    }

    @Test
    @DisplayName("assignment preview schema는 유효한 json이다")
    void schema_isValidJson() {
        // given: assignment preview json schema 준비
        String schema = AssignmentPreviewAiSchema.ASSIGNMENT_PREVIEW_JSON_SCHEMA;
        ObjectMapper objectMapper = new ObjectMapper();

        // when & then: json 파싱 가능 검증
        assertThatCode(() -> objectMapper.readTree(schema))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("assignment preview schema는 warning item에 추가 필드를 허용하지 않는다.")
    void schema_disallowsAdditionalPropertiesInWarningItems() throws Exception {
        // given: assignment preview json schema tree 준비
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode schema = objectMapper.readTree(AssignmentPreviewAiSchema.ASSIGNMENT_PREVIEW_JSON_SCHEMA);

        // when: warning item schema 조회
        JsonNode warningItemSchema = schema.path("properties")
                .path("warnings")
                .path("items");

        // then: warning item 추가 필드 금지 검증
        then(warningItemSchema.has("additionalProperties")).isTrue();
        then(warningItemSchema.get("additionalProperties").isBoolean()).isTrue();
        then(warningItemSchema.get("additionalProperties").booleanValue()).isFalse();
    }

    @Test
    @DisplayName("assignment preview schema는 round item에 추가 필드를 허용하지 않는다.")
    void schema_disallowsAdditionalPropertiesInRoundItems() throws Exception {
        // given: assignment preview json schema tree 준비
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode schema = objectMapper.readTree(AssignmentPreviewAiSchema.ASSIGNMENT_PREVIEW_JSON_SCHEMA);

        // when: round item schema 조회
        JsonNode roundItemSchema = schema.path("properties")
                .path("rounds")
                .path("items");

        // then: round item 추가 필드 금지 검증
        then(roundItemSchema.has("additionalProperties")).isTrue();
        then(roundItemSchema.get("additionalProperties").isBoolean()).isTrue();
        then(roundItemSchema.get("additionalProperties").booleanValue()).isFalse();
    }

    @Test
    @DisplayName("assignment preview schema는 court item에 추가 필드를 허용하지 않는다.")
    void schema_disallowsAdditionalPropertiesInCourtItems() throws Exception {
        // given: assignment preview json schema tree 준비
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode schema = objectMapper.readTree(AssignmentPreviewAiSchema.ASSIGNMENT_PREVIEW_JSON_SCHEMA);

        // when: court item schema 조회
        JsonNode courtItemSchema = schema.path("properties")
                .path("rounds")
                .path("items")
                .path("properties")
                .path("courts")
                .path("items");

        // then: court item 추가 필드 금지 검증
        then(courtItemSchema.has("additionalProperties")).isTrue();
        then(courtItemSchema.get("additionalProperties").isBoolean()).isTrue();
        then(courtItemSchema.get("additionalProperties").booleanValue()).isFalse();
    }

}
