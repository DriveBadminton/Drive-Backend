package com.gumraze.rallyon.backend.persistence;

import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameRepository;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameManager;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.AccountRepository;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.LocalCredentialRepository;
import com.gumraze.rallyon.backend.identity.entity.Account;
import com.gumraze.rallyon.backend.identity.entity.LocalCredential;
import com.gumraze.rallyon.backend.user.adapter.out.persistence.repository.UserProfileRepository;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
@Transactional
class AuditPersistenceCompatibilityTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private LocalCredentialRepository localCredentialRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("UserProfile 저장/수정 시 audit timestamp가 자동으로 관리된다")
    void userProfile_managesAuditTimestampsAutomatically() {
        Account account = accountRepository.save(Account.create());
        UserProfile profile = userProfileRepository.save(UserProfile.create(
                account.getId(),
                "initial",
                null,
                null,
                null,
                LocalDateTime.of(1998, 9, 25, 0, 0),
                Gender.MALE,
                "TAG1",
                LocalDateTime.now()
        ));

        entityManager.flush();
        entityManager.clear();

        UserProfile persisted = userProfileRepository.findById(account.getId()).orElseThrow();
        LocalDateTime createdAt = persisted.getCreatedAt();
        LocalDateTime updatedAt = persisted.getUpdatedAt();

        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();

        persisted.changeNickname("updated");
        entityManager.flush();
        entityManager.clear();

        UserProfile updatedProfile = userProfileRepository.findById(account.getId()).orElseThrow();
        assertThat(updatedProfile.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updatedProfile.getUpdatedAt()).isAfterOrEqualTo(updatedAt);
    }

    @Test
    @DisplayName("LocalCredential 저장/수정 시 audit timestamp가 자동으로 관리된다")
    void localCredential_managesAuditTimestampsAutomatically() {
        Account account = accountRepository.save(Account.create());
        LocalCredential credential = localCredentialRepository.save(LocalCredential.issue(
                account,
                "audit@rallyon.local",
                "hashed-password"
        ));

        entityManager.flush();
        entityManager.clear();

        LocalCredential persisted = localCredentialRepository.findById(account.getId()).orElseThrow();
        LocalDateTime createdAt = persisted.getCreatedAt();
        LocalDateTime updatedAt = persisted.getUpdatedAt();

        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();

        persisted.changePasswordHash("updated-hash");
        entityManager.flush();
        entityManager.clear();

        LocalCredential updated = localCredentialRepository.findById(account.getId()).orElseThrow();
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updatedAt);
    }

    @Test
    @DisplayName("GameManager 저장 시 createdAt이 자동으로 세팅된다")
    void gameManager_setsCreatedAtAutomatically() {
        Account organizer = accountRepository.save(Account.create());
        Account manager = accountRepository.save(Account.create());
        FreeGame freeGame = gameRepository.save(FreeGame.create(
                "audit-game",
                organizer.getId(),
                GradeType.NATIONAL,
                MatchRecordMode.RESULT,
                null,
                "잠실 배드민턴장"
        ));

        GameManager gameManager = GameManager.assign(freeGame, manager.getId());

        entityManager.persist(gameManager);
        entityManager.flush();

        assertThat(gameManager.getCreatedAt()).isNotNull();
    }
}
