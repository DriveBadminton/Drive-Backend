package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameAssignmentPreviewResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class CreateFreeGameAssignmentPreviewResponseMapperTest {

    private final CreateFreeGameAssignmentPreviewResponseMapper mapper =
            new CreateFreeGameAssignmentPreviewResponseMapper();

    @Test
    @DisplayName("assignment preview result를 assignment preview response로 변환한다")
    void toResponse_mapsResultToResponse() {
        // given
        CreateFreeGameAssignmentPreviewResult result =
                new CreateFreeGameAssignmentPreviewResult(
                        List.of(round(1, List.of(court(1, "p1", "p2", null, null)))),
                        List.of(warning("PARTNER_CONSTRAINT_PARTIAL", "일부 파트너 조합을 완전히 반영하지 못했습니다."))
                );

        // when
        CreateFreeGameAssignmentPreviewResponse response = mapper.toResponse(result);

        // then
        then(response.rounds()).hasSize(1);
        then(response.rounds().get(0).roundNumber()).isEqualTo(1);
        then(response.rounds().get(0).courts()).hasSize(1);
        then(response.rounds().get(0).courts().get(0).courtNumber()).isEqualTo(1);
        then(response.rounds().get(0).courts().get(0).slots())
                .containsExactly("p1", "p2", null, null);

        then(response.warnings()).hasSize(1);
        then(response.warnings().get(0).code()).isEqualTo("PARTNER_CONSTRAINT_PARTIAL");
        then(response.warnings().get(0).message()).isEqualTo("일부 파트너 조합을 완전히 반영하지 못했습니다.");
    }

    @Test
    @DisplayName("warnings가 비어 있으면 빈 리스트로 변환한다")
    void toResponse_whenWarningsEmpty_returnsEmptyList() {
        // given
        CreateFreeGameAssignmentPreviewResult result =
                new CreateFreeGameAssignmentPreviewResult(
                        List.of(round(1, List.of(court(1, "p1", "p2", "p3", "p4")))),
                        List.of()
                );

        // when
        CreateFreeGameAssignmentPreviewResponse response = mapper.toResponse(result);

        // then
        then(response.warnings()).isEmpty();
    }

    @Test
    @DisplayName("여러 라운드가 있으면 순서를 유지해서 변환한다")
    void toResponse_withMultipleRounds_preservesOrder() {
        // given
        CreateFreeGameAssignmentPreviewResult result =
                new CreateFreeGameAssignmentPreviewResult(
                        List.of(
                                round(1, List.of(court(1, "p1", "p2", "p3", "p4"))),
                                round(2, List.of(court(1, "p5", "p6", "p7", "p8")))
                        ),
                        List.of()
                );

        // when
        CreateFreeGameAssignmentPreviewResponse response = mapper.toResponse(result);

        // then
        then(response.rounds()).hasSize(2);
        then(response.rounds().get(0).roundNumber()).isEqualTo(1);
        then(response.rounds().get(1).roundNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("한 라운드에 여러 코트가 있으면 각 코트 슬롯을 그대로 유지한다")
    void toResponse_withMultipleCourts_preservesCourtSlots() {
        // given
        CreateFreeGameAssignmentPreviewResult result =
                new CreateFreeGameAssignmentPreviewResult(
                        List.of(
                                round(
                                        1,
                                        List.of(
                                                court(1, "p1", "p2", "p3", "p4"),
                                                court(2, "p5", "p6", "p7", "p8")
                                        )
                                )
                        ),
                        List.of()
                );

        // when
        CreateFreeGameAssignmentPreviewResponse response = mapper.toResponse(result);

        // then
        then(response.rounds().get(0).courts()).hasSize(2);
        then(response.rounds().get(0).courts().get(0).slots())
                .containsExactly("p1", "p2", "p3", "p4");
        then(response.rounds().get(0).courts().get(1).slots())
                .containsExactly("p5", "p6", "p7", "p8");
    }

    @Test
    @DisplayName("slots의 null 값은 그대로 유지한다")
    void toResponse_preservesNullSlots() {
        // given
        CreateFreeGameAssignmentPreviewResult result =
                new CreateFreeGameAssignmentPreviewResult(
                        List.of(round(1, List.of(court(1, "p1", null, "p3", null)))),
                        List.of()
                );

        // when
        CreateFreeGameAssignmentPreviewResponse response = mapper.toResponse(result);

        // then
        then(response.rounds().get(0).courts().get(0).slots())
                .containsExactly("p1", null, "p3", null);
    }

    @Test
    @DisplayName("여러 warning이 있으면 순서와 값을 유지해서 변환한다")
    void toResponse_withMultipleWarnings_preservesOrderAndValues() {
        // given
        CreateFreeGameAssignmentPreviewResult result =
                new CreateFreeGameAssignmentPreviewResult(
                        List.of(round(1, List.of(court(1, "p1", "p2", "p3", "p4")))),
                        List.of(
                                warning("PARTIAL_ASSIGNMENT", "일부 슬롯은 비어 있습니다."),
                                warning("PARTNER_CONSTRAINT_PARTIAL", "일부 파트너 조합을 완전히 반영하지 못했습니다.")
                        )
                );

        // when
        CreateFreeGameAssignmentPreviewResponse response = mapper.toResponse(result);

        // then
        then(response.warnings()).hasSize(2);
        then(response.warnings().get(0).code()).isEqualTo("PARTIAL_ASSIGNMENT");
        then(response.warnings().get(0).message()).isEqualTo("일부 슬롯은 비어 있습니다.");
        then(response.warnings().get(1).code()).isEqualTo("PARTNER_CONSTRAINT_PARTIAL");
        then(response.warnings().get(1).message()).isEqualTo("일부 파트너 조합을 완전히 반영하지 못했습니다.");
    }

    private CreateFreeGameAssignmentPreviewResult.Round round(
            Integer roundNumber,
            List<CreateFreeGameAssignmentPreviewResult.Court> courts
    ) {
        return new CreateFreeGameAssignmentPreviewResult.Round(roundNumber, courts);
    }

    private CreateFreeGameAssignmentPreviewResult.Court court(
            Integer courtNumber,
            String slot1,
            String slot2,
            String slot3,
            String slot4
    ) {
        return new CreateFreeGameAssignmentPreviewResult.Court(
                courtNumber,
                Arrays.asList(slot1, slot2, slot3, slot4)
        );
    }

    private CreateFreeGameAssignmentPreviewResult.Warning warning(String code, String message) {
        return new CreateFreeGameAssignmentPreviewResult.Warning(code, message);
    }
}
