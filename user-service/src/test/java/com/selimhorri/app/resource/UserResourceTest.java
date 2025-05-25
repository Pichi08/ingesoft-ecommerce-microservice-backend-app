package com.selimhorri.app.resource;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.service.UserService;

@WebMvcTest(UserResource.class)
class UserResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto userDto1;
    private UserDto userDto2;
    private List<UserDto> userDtos;

    @BeforeEach
    void setUp() {
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

        userDtos = Arrays.asList(userDto1, userDto2);
    }

    @Test
    @DisplayName("Should return all users")
    void testFindAll() throws Exception {
        when(userService.findAll()).thenReturn(userDtos);

        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection").isArray())
                .andExpect(jsonPath("$.collection.length()").value(2))
                .andExpect(jsonPath("$.collection[0].userId").value(1))
                .andExpect(jsonPath("$.collection[0].firstName").value("John"))
                .andExpect(jsonPath("$.collection[1].userId").value(2))
                .andExpect(jsonPath("$.collection[1].firstName").value("Jane"));

        verify(userService, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return user by ID")
    void testFindById() throws Exception {
        when(userService.findById(1)).thenReturn(userDto1);

        mockMvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("john.smith@example.com"));

        verify(userService, times(1)).findById(1);
    }


    @Test
    @DisplayName("Should save user")
    void testSave() throws Exception {
        when(userService.save(any(UserDto.class))).thenReturn(userDto1);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        verify(userService, times(1)).save(any(UserDto.class));
    }

    @Test
    @DisplayName("Should update user")
    void testUpdate() throws Exception {
        UserDto updatedUserDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Smith-Updated")
                .email("john.updated@example.com")
                .phone("123-456-7890")
                .imageUrl("http://example.com/image1.jpg")
                .credentialDto(userDto1.getCredentialDto())
                .build();

        when(userService.update(any(UserDto.class))).thenReturn(updatedUserDto);

        mockMvc.perform(put("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.lastName").value("Smith-Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));

        verify(userService, times(1)).update(any(UserDto.class));
    }

    @Test
    @DisplayName("Should update user with ID")
    void testUpdateWithId() throws Exception {
        UserDto updatedUserDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Smith-Updated")
                .email("john.updated@example.com")
                .phone("123-456-7890")
                .imageUrl("http://example.com/image1.jpg")
                .credentialDto(userDto1.getCredentialDto())
                .build();

        when(userService.update(eq(1), any(UserDto.class))).thenReturn(updatedUserDto);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.lastName").value("Smith-Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));

        verify(userService, times(1)).update(eq(1), any(UserDto.class));
    }

    @Test
    @DisplayName("Should delete user by ID")
    void testDeleteById() throws Exception {
        doNothing().when(userService).deleteById(1);

        mockMvc.perform(delete("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername() throws Exception {
        when(userService.findByUsername("johnsmith")).thenReturn(userDto1);

        mockMvc.perform(get("/api/users/username/johnsmith")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.credential.username").value("johnsmith"));

        verify(userService, times(1)).findByUsername("johnsmith");
    }
} 