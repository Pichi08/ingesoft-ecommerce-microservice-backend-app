package com.selimhorri.app.unit.helper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.helper.UserMappingHelper;

class UserMappingHelperTest {

    private User user;
    private UserDto userDto;
    private Credential credential;
    private CredentialDto credentialDto;

    @BeforeEach
    void setUp() {
        // Create test data
        credential = Credential.builder()
                .credentialId(1)
                .username("johnsmith")
                .password("password123")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        user = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("123-456-7890")
                .imageUrl("http://example.com/image1.jpg")
                .credential(credential)
                .build();
        credential.setUser(user);

        credentialDto = CredentialDto.builder()
                .credentialId(1)
                .username("johnsmith")
                .password("password123")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        userDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("123-456-7890")
                .imageUrl("http://example.com/image1.jpg")
                .credentialDto(credentialDto)
                .build();
    }

    @Test
    @DisplayName("Should map User entity to UserDto")
    void testMapEntityToDto() {
        // Act
        UserDto result = UserMappingHelper.map(user);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(user.getUserId());
        assertThat(result.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(result.getLastName()).isEqualTo(user.getLastName());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getPhone()).isEqualTo(user.getPhone());
        assertThat(result.getImageUrl()).isEqualTo(user.getImageUrl());
        
        // Verify credential mapping
        assertThat(result.getCredentialDto()).isNotNull();
        assertThat(result.getCredentialDto().getCredentialId()).isEqualTo(user.getCredential().getCredentialId());
        assertThat(result.getCredentialDto().getUsername()).isEqualTo(user.getCredential().getUsername());
        assertThat(result.getCredentialDto().getPassword()).isEqualTo(user.getCredential().getPassword());
        assertThat(result.getCredentialDto().getIsEnabled()).isEqualTo(user.getCredential().getIsEnabled());
    }

    @Test
    @DisplayName("Should map UserDto to User entity")
    void testMapDtoToEntity() {
        // Act
        User result = UserMappingHelper.map(userDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userDto.getUserId());
        assertThat(result.getFirstName()).isEqualTo(userDto.getFirstName());
        assertThat(result.getLastName()).isEqualTo(userDto.getLastName());
        assertThat(result.getEmail()).isEqualTo(userDto.getEmail());
        assertThat(result.getPhone()).isEqualTo(userDto.getPhone());
        assertThat(result.getImageUrl()).isEqualTo(userDto.getImageUrl());
        
        // Verify credential mapping
        assertThat(result.getCredential()).isNotNull();
        assertThat(result.getCredential().getCredentialId()).isEqualTo(userDto.getCredentialDto().getCredentialId());
        assertThat(result.getCredential().getUsername()).isEqualTo(userDto.getCredentialDto().getUsername());
        assertThat(result.getCredential().getPassword()).isEqualTo(userDto.getCredentialDto().getPassword());
        assertThat(result.getCredential().getIsEnabled()).isEqualTo(userDto.getCredentialDto().getIsEnabled());
    }
} 