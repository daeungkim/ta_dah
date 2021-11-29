package com.tadah.location.controllers;

import com.tadah.auth.domain.entities.Role;
import com.tadah.auth.domain.repositories.RoleRepository;
import com.tadah.auth.domain.repositories.infra.JpaRoleRepository;
import com.tadah.auth.utils.JwtUtil;
import com.tadah.user.domains.UserType;
import com.tadah.user.domains.entities.User;
import com.tadah.user.domains.repositories.UserRepository;
import com.tadah.user.domains.repositories.infra.JpaUserRepository;
import com.tadah.utils.LoginFailTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.tadah.user.domains.entities.UserTest.USER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ExtendWith(SpringExtension.class)
@DisplayName("LocationController 클래스")
public final class LocationControllerTest {
    private static final String LOCATIONS_URL = "/locations";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaRoleRepository jpaRoleRepository;

    @AfterEach
    private void afterEach() {
        jpaUserRepository.deleteAll();
        jpaRoleRepository.deleteAll();
    }

    @Nested
    @DisplayName("create 메서드는")
    public final class Describe_create extends LoginFailTest {
        private String token;
        private User savedUser;

        public Describe_create() {
            super(mockMvc, post(LOCATIONS_URL));
        }

        private ResultActions subject(final String token) throws Exception {
            return mockMvc.perform(
                post(LOCATIONS_URL)
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(AUTHORIZATION_HEADER, TOKEN_PREFIX + token));
        }

        @BeforeEach
        private void beforeEach() {
            this.savedUser = userRepository.save(USER);
            this.token = jwtUtil.encode(savedUser.getId());
        }

        @Nested
        @DisplayName("권한이 없는 경우")
        public final class Context_emptyAuthority {
            @Test
            @DisplayName("권한이 필요함을 알려준다.")
            public void it_informs_that_authority_is_required() throws Exception {
                subject(token)
                    .andExpect(status().isForbidden());
            }
        }

        @Nested
        @DisplayName("권한이 올바르지 않은 경우")
        public final class Context_invalidAuthority {
            @BeforeEach
            private void beforeEach() {
                roleRepository.save(new Role(savedUser.getId(), UserType.RIDER.name()));
            }

            @Test
            @DisplayName("잘못된 권한임을 알려준다.")
            public void it_informs_that_authority_is_invalid() throws Exception {
                subject(token)
                    .andExpect(status().isForbidden());
            }
        }

        @Nested
        @DisplayName("올바른 토큰이 입력된 경우")
        public final class Context_validToken {
            @BeforeEach
            private void beforeEach() {
                roleRepository.save(new Role(savedUser.getId(), UserType.DRIVER.name()));
            }

            @Test
            public void test() throws Exception {
                subject(token)
                    .andExpect(status().isCreated());
            }
        }
    }
}
