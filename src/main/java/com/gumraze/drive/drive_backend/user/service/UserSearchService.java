package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.dto.UserSearchResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserSearchService {
    // 닉네임 부분 일치 검색 (nickname 대소문자 구분)
    List<UserSearchResponse> searchByNickname(String nickname);

    // 닉네임 + 태그 정확 검색 (tag는 대소문자 무시)
    UserSearchResponse searchByNicknameAndTag(String nickname, String tags);


}
