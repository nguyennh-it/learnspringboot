package com.example.demo.controller;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.request.response.UserResponse;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource("/test.properties")
public class UserControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private UserService userService;

  private UserCreationRequest request;
  private UserResponse userResponse;
  private LocalDate dob;

  @BeforeEach
  void initDate() {
    dob = LocalDate.of(1990, 1, 1);
    request =
        UserCreationRequest.builder()
            .username("join")
            .firstName("John")
            .lastName("Doe")
            .password("12345678")
            .dob(dob)
            .build();
    userResponse =
        UserResponse.builder()
            .id("f6df1ce5e08f")
            .username("join")
            .firstName("John")
            .lastName("Doe")
            .dob(dob)
            .build();
  }

  @Test
  void createUser_validRequest_success() throws Exception {
    // Given
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    String content = objectMapper.writeValueAsString(request);

    Mockito.when(userService.createUser(ArgumentMatchers.any())).thenReturn(userResponse);

    // When
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(content))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1000))
        .andExpect(MockMvcResultMatchers.jsonPath("$.result.id").value("f6df1ce5e08f"));
  }

  @Test
  void createUser_usernameInvalid_fail() throws Exception {
    // Given
    request.setUsername("jo"); // username < 3 ký tự → vi phạm validation
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    String content = objectMapper.writeValueAsString(request);

    // When
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(content))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1003))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.message")
                .value("User must be at least 3 characters"));
  }
}
