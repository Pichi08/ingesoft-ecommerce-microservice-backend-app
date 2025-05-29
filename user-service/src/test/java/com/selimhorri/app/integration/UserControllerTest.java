package com.selimhorri.app.integration;

import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.unit.util.UserUtil;
import com.selimhorri.app.domain.RoleBasedAuthority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.profiles.active=test",
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.datasource.driver-class-name=org.h2.Driver"})

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private UserDto testUserDto;

    @BeforeEach
    public void setUp() {
        // Initialize test data
        testUserDto = createTestUserDto();
    }

    // Utility methods for creating test data
    private UserDto createTestUserDto() {
        CredentialDto credentialDto = CredentialDto.builder()
                .username("integrationtest_user_" + System.currentTimeMillis())
                .password("testpass123")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        return UserDto.builder()
                .firstName("Test")
                .lastName("User")
                .imageUrl("http://example.com/test.jpg")
                .email("test.user." + System.currentTimeMillis() + "@example.com")
                .phone("+1234567890")
                .credentialDto(credentialDto)
                .build();
    }

    private UserDto createUpdatedUserDto(UserDto originalUser) {
        return UserDto.builder()
                .userId(originalUser.getUserId())
                .firstName("Updated")
                .lastName("Name")
                .imageUrl("http://example.com/updated.jpg")
                .email("updated.email@example.com")
                .phone("+1111111111")
                .credentialDto(originalUser.getCredentialDto())
                .build();
    }

    @Test
    public void testCreateUser() {
        String url = "http://localhost:" + port + "/user-service/api/users";
        UserDto userDto = UserUtil.getSampleUserDto();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDto> entity = new HttpEntity<>(userDto, headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userDto.getFirstName(), response.getBody().getFirstName());
        assertEquals(userDto.getEmail(), response.getBody().getEmail());
    }

    @Test
    public void testCreateUserWithCustomData() {
        String url = "http://localhost:" + port + "/user-service/api/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDto> entity = new HttpEntity<>(testUserDto, headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testUserDto.getFirstName(), response.getBody().getFirstName());
        assertEquals(testUserDto.getLastName(), response.getBody().getLastName());
        assertEquals(testUserDto.getEmail(), response.getBody().getEmail());
        assertEquals(testUserDto.getPhone(), response.getBody().getPhone());
        assertNotNull(response.getBody().getUserId());
    }

    @Test
    public void testGetAllUsers() {
        String url = "http://localhost:" + port + "/user-service/api/users";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testUpdateUser() {
        // First create a user
        String createUrl = "http://localhost:" + port + "/user-service/api/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDto> createEntity = new HttpEntity<>(testUserDto, headers);
        ResponseEntity<UserDto> createResponse = restTemplate.exchange(
                createUrl,
                HttpMethod.POST,
                createEntity,
                UserDto.class
        );

        UserDto createdUser = createResponse.getBody();
        assertNotNull(createdUser);

        // Update the user
        UserDto updatedUser = createUpdatedUserDto(createdUser);
        String updateUrl = "http://localhost:" + port + "/user-service/api/users";

        HttpEntity<UserDto> updateEntity = new HttpEntity<>(updatedUser, headers);
        ResponseEntity<UserDto> updateResponse = restTemplate.exchange(
                updateUrl,
                HttpMethod.PUT,
                updateEntity,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals(updatedUser.getUserId(), updateResponse.getBody().getUserId());
        assertEquals("Updated", updateResponse.getBody().getFirstName());
        assertEquals("Name", updateResponse.getBody().getLastName());
        assertEquals("updated.email@example.com", updateResponse.getBody().getEmail());
    }
}