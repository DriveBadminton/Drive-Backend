package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.user.entity.UserGradeHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserGradeHistoryRepositoryImpl implements UserGradeHistoryRepository {
    private final JpaUserGradeHistoryRepository jpaUserGradeHistoryRepository;

    @Override
    public void save(UserGradeHistory userGradeHistory) {
        jpaUserGradeHistoryRepository.save(userGradeHistory);
    }
}
