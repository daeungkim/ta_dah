package com.tadah.auth.utils;

import com.tadah.auth.exceptions.InvalidTokenException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("JwtUtil 클래스")
public final class JwtUtilTest {
    private static final String SECRET = "12345678901234567890123456789012";
    private static final Long CLAIM_DATA = 1L;

    public static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjF9.ZZ3CUl0jxeLGvQ1Js5nG2Ty5qGTlqai5ubDMXZOdaDk";
    public static final String VALID_TOKEN_WITHOUT_CLAIMS = "eyJhbGciOiJIUzI1NiJ9..Y3zwinksGMfE9Ym4QHp3jFBeDE_iJdw3F-DDlvMEE9Q";
    public static final String VALID_TOKEN_INVALID_CLAIMS_NAME = "eyJhbGciOiJIUzI1NiJ9.eyJpbnZhbGlkIjoxfQ.QwqsY19u7hBbtd32x31vUX0L6wONcPv9Msh2wlanPoI";
    public static final String INVALID_TOKEN = VALID_TOKEN + "invalid";
    public static final JwtUtil JWT_UTIL = new JwtUtil(SECRET);

    @Nested
    @DisplayName("encode 메서드는")
    public final class Describe_encode {
        private String subject() {
            return JWT_UTIL.encode(CLAIM_DATA);
        }

        @Test
        @DisplayName("속성 정보를 인코딩하여 토큰을 리턴한다.")
        public void it_encodes_claim_data_and_then_returns_the_token() {
            assertThat(subject())
                .isEqualTo(VALID_TOKEN);
        }
    }

    @Nested
    @DisplayName("decode 메서드는")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public final class Describe_decode {
        private Claims subject(final String token) {
            return JWT_UTIL.decode(token);
        }

        private Stream<Arguments> methodSource() {
            return Stream.of(
                Arguments.of(VALID_TOKEN),
                Arguments.of(VALID_TOKEN_INVALID_CLAIMS_NAME)
            );
        }

        @MethodSource("methodSource")
        @ParameterizedTest(name = "token = \"{0}\"")
        @DisplayName("토큰을 디코딩하여 속성 정보를 리턴한다.")
        public void it_decodes_token_and_then_returns_the_claim_data(final String token) {
            assertThat(subject(token))
                .isInstanceOf(Claims.class);
        }

        @Nested
        @DisplayName("토큰이 유효하지 않은 경우")
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        public final class Context_invalidToken {
            private Stream<Arguments> methodSource() {
                return Stream.of(
                    Arguments.of(INVALID_TOKEN),
                    Arguments.of(VALID_TOKEN_WITHOUT_CLAIMS),
                    Arguments.of((Object) null),
                    Arguments.of(" "),
                    Arguments.of("   ")
                );
            }

            @MethodSource("methodSource")
            @ParameterizedTest(name = "token = \"{0}\"")
            @DisplayName("InvalidTokenException을 던진다.")
            public void it_throws_invalid_token_exception(final String token) {
                assertThatThrownBy(() -> subject(token))
                    .isInstanceOf(InvalidTokenException.class);
            }
        }
    }
}
