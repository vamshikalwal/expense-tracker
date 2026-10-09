package com.expenseTracker.service;

import com.expenseTracker.dto.ProfileUpdateRequest;
import com.expenseTracker.entity.User;
import com.expenseTracker.exception.BadRequestException;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getCurrentUser_shouldReturnUserForEmail() {
        User user = new User();
        user.setId(7L);
        user.setName("Alex Smith");
        user.setEmail("alex@example.com");
        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));

        User result = userService.getCurrentUser("alex@example.com");

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getEmail()).isEqualTo("alex@example.com");
    }

    @Test
    void getCurrentUser_shouldThrowWhenUserMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void updateProfile_shouldUpdateNameAndPasswordWhenProvided() {
        User user = new User();
        user.setId(7L);
        user.setName("Old Name");
        user.setEmail("alex@example.com");
        user.setPassword("old-hash");

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("New Name");
        request.setCurrentPassword("old-password");
        request.setNewPassword("new-password");

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile("alex@example.com", request);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getPassword()).isEqualTo("new-hash");
        verify(userRepository).save(user);
    }

    @Test
    void deleteAccount_shouldRejectWrongPassword() {
        User user = new User();
        user.setId(7L);
        user.setEmail("alex@example.com");
        user.setPassword("encoded-password");

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteAccount("alex@example.com", "wrong-password"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Password is incorrect");
    }
}
