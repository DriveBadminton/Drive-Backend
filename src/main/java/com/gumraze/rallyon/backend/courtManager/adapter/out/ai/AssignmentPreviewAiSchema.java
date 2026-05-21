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
                "additionalProperties": false,
                "required": ["roundNumber", "courts"],
                "properties": {
                  "roundNumber": {
                    "type": "integer"
                  },
                  "courts": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "additionalProperties": false,
                      "required": ["courtNumber", "slots"],
                      "properties": {
                        "courtNumber": {
                          "type": "integer"
                        },
                        "slots": {
                          "type": "array",
                          "minItems": 4,
                          "maxItems": 4,
                          "items": {
                            "type": ["integer", "null"]
                          }
                        }
                      }
                    }
                  }
                }
              }
            },
            "warnings": {
              "type": "array",
              "items": {
                "type": "object",
                "additionalProperties": false,
                "required": ["code", "message"],
                "properties": {
                  "code": {
                    "type": "string"
                  },
                  "message": {
                    "type": "string"
                  }
                }
              }
            }
          }
        }
        """;
}
