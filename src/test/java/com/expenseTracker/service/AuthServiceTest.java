package com.expenseTracker.service;

import com.expenseTracker.dto.AuthResponse;
import com.expenseTracker.dto.LoginRequest;
import com.expenseTracker.dto.RegisterRequest;
import com.expenseTracker.entity.User;
import com.expenseTracker.exception.DuplicateResourceException;
import com.expenseTracker.exception.UnauthorizedException;
import com.expenseTracker.repository.UserRepository;
import com.expenseTracker.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldSaveEncodedPasswordAndReturnUserDetails() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alex Smith");
        request.setEmail("alex@example.com");
        request.setPassword("plain-password");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;
        });

        AuthResponse response = authService.register(request);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getName()).isEqualTo("Alex Smith");
        assertThat(response.getEmail()).isEqualTo("alex@example.com");
        assertThat(response.getToken()).isEqualTo("User registered successfully. Please login.");
        verify(passwordEncoder).encode("plain-password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldRejectAnExistingEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("alex@example.com");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already in use");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void login_shouldReturnTokenAndUserDetails() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alex@example.com");
        request.setPassword("plain-password");
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        User user = new User();
        user.setId(7L);
        user.setName("Alex Smith");
        user.setEmail(request.getEmail());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getName()).isEqualTo("Alex Smith");
        assertThat(response.getEmail()).isEqualTo("alex@example.com");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_shouldTranslateAuthenticationFailureToUnauthorizedException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alex@example.com");
        request.setPassword("wrong-password");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository, never()).findByEmail(any());
    }
}