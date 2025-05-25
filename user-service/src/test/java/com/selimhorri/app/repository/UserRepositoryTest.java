package com.selimhorri.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.domain.User;


@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find user by username")
    void testFindByCredentialUsername() {
        // Arrange
        Credential credential = Credential.builder()
                .username("testuser")
                .password("password123")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .build();

        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email("test.user@example.com")
                .phone("123-456-7890")
                .imageUrl("http://example.com/test.jpg")
                .credential(credential)
                .build();

        credential.setUser(user);
        
        entityManager.persist(user);
        entityManager.flush();

        // Act
        Optional<User> foundUser = userRepository.findByCredentialUsername("testuser");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFirstName()).isEqualTo("Test");
        assertThat(foundUser.get().getLastName()).isEqualTo("User");
        assertThat(foundUser.get().getEmail()).isEqualTo("test.user@example.com");
        assertThat(foundUser.get().getCredential().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should return empty when username not found")
    void testFindByCredentialUsernameNotFound() {
        // Act
        Optional<User> foundUser = userRepository.findByCredentialUsername("nonexistentuser");

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        // Arrange
        Credential credential1 = Credential.builder()
                .username("user1")
                .password("password1")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .build();

        User user1 = User.builder()
                .firstName("First")
                .lastName("User")
                .email("first.user@example.com")
                .phone("111-111-1111")
                .imageUrl("http://example.com/first.jpg")
                .credential(credential1)
                .build();

        credential1.setUser(user1);

        Credential credential2 = Credential.builder()
                .username("user2")
                .password("password2")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .build();

        User user2 = User.builder()
                .firstName("Second")
                .lastName("User")
                .email("second.user@example.com")
                .phone("222-222-2222")
                .imageUrl("http://example.com/second.jpg")
                .credential(credential2)
                .build();

        credential2.setUser(user2);
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertThat(users).isNotEmpty();
        assertThat(users.size()).isGreaterThanOrEqualTo(2);
        // Check if our test users are in the result
        assertThat(users.stream().anyMatch(u -> u.getCredential().getUsername().equals("user1"))).isTrue();
        assertThat(users.stream().anyMatch(u -> u.getCredential().getUsername().equals("user2"))).isTrue();
    }

    @Test
    @DisplayName("Should save user")
    void testSave() {
        // Arrange
        Credential credential = Credential.builder()
                .username("newuser")
                .password("newpassword")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .build();

        User newUser = User.builder()
                .firstName("New")
                .lastName("User")
                .email("new.user@example.com")
                .phone("333-333-3333")
                .imageUrl("http://example.com/new.jpg")
                .credential(credential)
                .build();

        credential.setUser(newUser);

        // Act
        User savedUser = userRepository.save(newUser);

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUserId()).isNotNull();
        assertThat(savedUser.getFirstName()).isEqualTo("New");
        assertThat(savedUser.getLastName()).isEqualTo("User");
        assertThat(savedUser.getEmail()).isEqualTo("new.user@example.com");
        assertThat(savedUser.getCredential().getUsername()).isEqualTo("newuser");
    }
} 