package com.sanosysalvos.auth.service;

import com.sanosysalvos.auth.config.JwtUtils;
import com.sanosysalvos.auth.domain.RefreshToken;
import com.sanosysalvos.auth.domain.User;
import com.sanosysalvos.auth.domain.User.RolUsuario;
import com.sanosysalvos.auth.dto.*;
import com.sanosysalvos.auth.repository.RefreshTokenRepository;
import com.sanosysalvos.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private EmailService emailService;
    @Mock private RateLimitService rateLimitService;
    
    private PasswordEncoder passwordEncoder;
    private JwtUtils jwtUtils;
    
    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtUtils = new JwtUtils(
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
            86400000L,
            604800000L
        );
        
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .nombre("Test User")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .rol(RolUsuario.USER)
                .isActive(true)
                .isLocked(false)
                .emailVerified(true)
                .failedAttempts(0)
                .build();
    }

    @Test
    void register_success() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .nombre("Nuevo Usuario")
                .email("nuevo@example.com")
                .password("password123")
                .rol(RolUsuario.USER)
                .build();

        when(rateLimitService.isRateLimited(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("nuevo@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        AuthResponseDTO response = authService.register(request, "127.0.0.1");

        assertThat(response).isNotNull();
        assertThat(response.getNombre()).isEqualTo("Nuevo Usuario");
        verify(emailService).sendVerificationEmail(eq("nuevo@example.com"), anyString());
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .nombre("Test")
                .email("existing@example.com")
                .password("password123")
                .build();

        when(rateLimitService.isRateLimited(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, "127.0.0.1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("email ya está registrado");
    }

    @Test
    void register_rateLimited_throwsException() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .nombre("Test")
                .email("test@example.com")
                .password("password123")
                .build();

        when(rateLimitService.isRateLimited("127.0.0.1")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, "127.0.0.1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Demasiadas solicitudes");
    }

    @Test
    void login_success() {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(rateLimitService.isRateLimited(anyString())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        AuthResponseDTO response = authService.login(request, "127.0.0.1");

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).resetFailedAttempts(userId);
    }

    @Test
    void login_invalidPassword_throwsException() {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        when(rateLimitService.isRateLimited(anyString())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Credenciales inválidas");

        verify(userRepository).incrementFailedAttempts(userId);
    }

    @Test
    void login_lockedAccount_throwsException() {
        testUser.setLocked(true);
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(rateLimitService.isRateLimited(anyString())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cuenta está bloqueada");
    }

    @Test
    void login_emailNotVerified_throwsException() {
        testUser.setEmailVerified(false);
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(rateLimitService.isRateLimited(anyString())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("verificar su email");
    }

    @Test
    void login_userNotFound_throwsException() {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();

        when(rateLimitService.isRateLimited(anyString())).thenReturn(false);
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    @Test
    void refreshToken_success() {
        RefreshTokenRequestDTO request = RefreshTokenRequestDTO.builder()
                .refreshToken("valid-refresh-token")
                .build();

        RefreshToken existingToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .token("valid-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(existingToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        AuthResponseDTO response = authService.refreshToken(request);

        assertThat(response.getAccessToken()).isNotEmpty();
        verify(refreshTokenRepository).delete(existingToken);
    }

    @Test
    void refreshToken_expired_throwsException() {
        RefreshTokenRequestDTO request = RefreshTokenRequestDTO.builder()
                .refreshToken("expired-token")
                .build();

        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expirado");

        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    void verifyEmail_success() {
        testUser.setEmailVerified(false);
        testUser.setVerificationToken("valid-token");
        testUser.setVerificationTokenExpiry(LocalDateTime.now().plusHours(12));

        when(userRepository.findByVerificationToken("valid-token")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String result = authService.verifyEmail("valid-token");

        assertThat(result).contains("verificado exitosamente");
        assertThat(testUser.isEmailVerified()).isTrue();
        assertThat(testUser.getVerificationToken()).isNull();
    }

    @Test
    void verifyEmail_expiredToken_throwsException() {
        testUser.setVerificationToken("expired-token");
        testUser.setVerificationTokenExpiry(LocalDateTime.now().minusHours(1));

        when(userRepository.findByVerificationToken("expired-token")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.verifyEmail("expired-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void login_fiveFailedAttempts_locksAccount() {
        testUser.setFailedAttempts(4);
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        when(rateLimitService.isRateLimited(anyString())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(RuntimeException.class);

        verify(userRepository).lockAccount(userId);
    }
}
