package com.kyf.mp.server.modules.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.kyf.mp.server.common.auth.LoginRateLimiter;
import com.kyf.mp.server.modules.user.business.UsersBusiness;
import com.kyf.mp.server.modules.user.entity.Users;
import com.kyf.mp.server.utils.JwtUtils;

@ExtendWith(MockitoExtension.class)
class UsersServiceImplTest {

    private static final String LEGACY_SHA256 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    @Mock
    private UsersBusiness usersBusiness;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private LoginRateLimiter loginRateLimiter;

    private BCryptPasswordEncoder passwordEncoder;
    private UsersServiceImpl usersService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(4);
        usersService = new UsersServiceImpl(usersBusiness, passwordEncoder, jwtUtils, loginRateLimiter);
    }

    @Test
    void migratesMatchingLegacySha256PasswordToBcrypt() {
        Users user = new Users();
        user.setPassword(LEGACY_SHA256);

        assertThat(usersService.matchesPassword(LEGACY_SHA256, user)).isTrue();
        assertThat(user.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches(LEGACY_SHA256, user.getPassword())).isTrue();
        verify(usersBusiness).updateById(user);
    }

    @Test
    void rejectsNonMatchingLegacySha256PasswordWithoutMigrating() {
        Users user = new Users();
        user.setPassword(LEGACY_SHA256);

        assertThat(usersService.matchesPassword("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", user))
                .isFalse();
        assertThat(user.getPassword()).isEqualTo(LEGACY_SHA256);
        verifyNoInteractions(usersBusiness);
    }

    @Test
    void acceptsBcryptPasswordWithoutUnnecessaryUpdate() {
        Users user = new Users();
        user.setPassword(passwordEncoder.encode(LEGACY_SHA256));

        assertThat(usersService.matchesPassword(LEGACY_SHA256, user)).isTrue();
        verifyNoInteractions(usersBusiness);
    }
}