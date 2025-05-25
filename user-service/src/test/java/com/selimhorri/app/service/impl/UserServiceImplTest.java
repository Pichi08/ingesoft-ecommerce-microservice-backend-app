package com.selimhorri.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user1;
    private User user2;
    private UserDto userDto1;
    private UserDto userDto2;

    @BeforeEach
    void setUp() {
        // Create test data
        Credential credential1 = Credential.builder()
                .credentialId(1)
                .username("johnsmith")
                .password("password123")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        user1 = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("123-456-7890")
                .imageUrl("http://example.com/image1.jpg")
                .credential(credential1)
                .build();
        credential1.setUser(user1);

        Credential credential2 = Credential.builder()
                .credentialId(2)
                .username("janedoe")
                .password("password456")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        user2 = User.builder()
                .userId(2)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("098-765-4321")
                .imageUrl("http://example.com/image2.jpg")
                .credential(credential2)
                .build();
        credential2.setUser(user2);

        CredentialDto credentialDto1 = CredentialDto.builder()
                .credentialId(1)
                .username("johnsmith")
                .password("password123")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        userDto1 = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("123-456-7890")
                .imageUrl("http://example.com/image1.jpg")
                .credentialDto(credentialDto1)
                .build();

        CredentialDto credentialDto2 = CredentialDto.builder()
                .credentialId(2)
                .username("janedoe")
                .password("password456")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        userDto2 = UserDto.builder()
                .userId(2)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("098-765-4321")
                .imageUrl("http://example.com/image2.jpg")
                .credentialDto(credentialDto2)
                .build();
    }

    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<UserDto> result = userService.findAll();

        // Assert
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
        assertThat(result.get(1).getUserId()).isEqualTo(2);
        assertThat(result.get(1).getFirstName()).isEqualTo("Jane");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find user by ID")
    void testFindById() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));

        // Act
        UserDto result = userService.findById(1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getEmail()).isEqualTo("john.smith@example.com");
        assertThat(result.getCredentialDto().getUsername()).isEqualTo("johnsmith");
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw exception when user ID not found")
    void testFindByIdNotFound() {
        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.findById(999))
            .isInstanceOf(UserObjectNotFoundException.class)
            .hasMessageContaining("User with id: 999 not found");
        verify(userRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should save user")
    void testSave() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(user1);
        
        // Act
        UserDto result = userService.save(userDto1);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should update existing user")
    void testUpdate() {
        // Arrange
        User updatedUser = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Smith-Updated")
                .email("john.updated@example.com")
                .phone("123-456-7890")
                .imageUrl("http://example.com/image1.jpg")
                .credential(user1.getCredential())
                .build();
        
        UserDto updatedUserDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Smith-Updated")
                .email("john.updated@example.com")
                .phone("123-456-7890")
                .imageUrl("http://example.com/image1.jpg")
                .credentialDto(userDto1.getCredentialDto())
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        
        // Act
        UserDto result = userService.update(updatedUserDto);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getLastName()).isEqualTo("Smith-Updated");
        assertThat(result.getEmail()).isEqualTo("john.updated@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user with ID")
    void testUpdateWithId() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);
        
        // Act
        UserDto result = userService.update(1, userDto1);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user by ID")
    void testDeleteById() {
        // Arrange
        doNothing().when(userRepository).deleteById(1);
        
        // Act
        userService.deleteById(1);
        
        // Assert
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername() {
        // Arrange
        when(userRepository.findByCredentialUsername("johnsmith")).thenReturn(Optional.of(user1));
        
        // Act
        UserDto result = userService.findByUsername("johnsmith");
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getCredentialDto().getUsername()).isEqualTo("johnsmith");
        verify(userRepository, times(1)).findByCredentialUsername("johnsmith");
    }

    @Test
    @DisplayName("Should throw exception when username not found")
    void testFindByUsernameNotFound() {
        // Arrange
        when(userRepository.findByCredentialUsername(anyString())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> userService.findByUsername("nonexistent"))
            .isInstanceOf(UserObjectNotFoundException.class)
            .hasMessageContaining("User with username: nonexistent not found");
        verify(userRepository, times(1)).findByCredentialUsername("nonexistent");
    }
} 