package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

/**
 * OpenAI structured output schema를 제공하는 유틸리티다.
 */
public final class AssignmentPreviewAiSchema {

    private AssignmentPreviewAiSchema() {
    }

    public static final String ASSIGNMENT_PREVIEW_JSON_SCHEMA = """
            {
              "type": "object",
              "additionalProperties": false,
              "required": ["rounds", "warnings"],
              "properties": {
                "rounds": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "required": ["roundNumber", "courts"],
                    "properties": {
                      "roundNumber": {
                        "type": "integer"
                        },
                      "courts": {
                        "type": "array",
                        }
                      }
                    }
                  },
                  "warnings": {
                    "type": "array"
                  }
                }
              }
            """;


}
