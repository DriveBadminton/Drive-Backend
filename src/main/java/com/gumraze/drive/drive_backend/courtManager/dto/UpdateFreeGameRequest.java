package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.courtManager.constants.MatchRecordMode;
import com.gumraze.drive.drive_backend.user.constants.GradeType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class UpdateFreeGameRequest {
    private String title;
    private MatchRecordMode matchRecordMode;
    private GradeType gradeType;
    private List<Long> managerIds;
}
