package com.gumraze.rallyon.backend.identity.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.AccountRepository;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.LocalCredentialRepository;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.OAuthLinkRepository;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.OAuthUserInfo;
import com.gumraze.rallyon.backend.identity.entity.Account;
import com.gumraze.rallyon.backend.identity.entity.LocalCredential;
import com.gumraze.rallyon.backend.identity.entity.OAuthLink;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
@Transactional
class AccountPersistenceCompatibilityTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OAuthLinkRepository oauthLinkRepository;

    @Autowired
    private LocalCredentialRepository localCredentialRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("GOOGLE identity_oauth_links row를 저장하고 조회할 수 있다")
    void loads_google_oauth_link_rows() {
        Account account = accountRepository.save(Account.create());
        OAuthLink link = OAuthLink.link(account, AuthProvider.GOOGLE, "google-user-1");
        link.applySnapshot(new OAuthUserInfo(
                "google-user-1",
                "google-user-1@rallyon.test",
                "google-user-1",
                null,
                null,
                null,
                null,
                null,
                true,
                false
        ));

        oauthLinkRepository.save(link);

        entityManager.flush();
        entityManager.clear();

        OAuthLink loaded = oauthLinkRepository
                .findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-user-1")
                .orElseThrow();

        assertThat(loaded.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(loaded.getProviderUserId()).isEqualTo("google-user-1");
        assertThat(loaded.getAccount().getId()).isEqualTo(account.getId());
        assertThat(loaded.getNickname()).isEqualTo("google-user-1");
    }

    @Test
    @DisplayName("identity_local_credentials row를 저장하고 이메일로 조회할 수 있다")
    void loads_local_credentials_by_normalized_email() {
        Account account = accountRepository.save(Account.create());

        localCredentialRepository.save(LocalCredential.issue(
                account,
                "user@rallyon.test",
                "hashed-password"
        ));

        entityManager.flush();
        entityManager.clear();

        LocalCredential loaded = localCredentialRepository
                .findByEmailNormalized("user@rallyon.test")
                .orElseThrow();

        assertThat(loaded.getAccountId()).isEqualTo(account.getId());
        assertThat(loaded.getPasswordHash()).isEqualTo("hashed-password");
    }
}
