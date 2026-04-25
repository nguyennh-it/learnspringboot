package com.example.demo.controller;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.demo.dto.request.UserCreationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

/**
 * Integration test dùng Testcontainers (MySQL Docker container).
 *
 * <p>Để chạy test này cần Docker Desktop hoạt động đúng với Testcontainers. Nếu Docker Desktop 29.x
 * không tương thích, vào Docker Desktop Settings → General → bật "Expose daemon on
 * tcp://localhost:2375 without TLS" rồi chạy: mvnw test -Dtest=UserControllerIntegrationTest
 * -Dintegration.test=true
 */
@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext
@EnabledIfSystemProperty(named = "integration.test", matches = "true")
public class UserControllerIntegrationTest {

  // Khai báo Container MySQL giả lập (Docker)
  @Container
  static MySQLContainer<?> mysqlContainer =
      new MySQLContainer<>("mysql:8.0")
          .withDatabaseName("testdb")
          .withUsername("testuser")
          .withPassword("testpass");

  // Ép Spring dùng cấu hình DB từ Docker thay vì DB thật trên máy
  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
    registry.add("spring.datasource.username", mysqlContainer::getUsername);
    registry.add("spring.datasource.password", mysqlContainer::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    registry.add("server.servlet.context-path", () -> "");
  }

  @Autowired private MockMvc mockMvc;

  private UserCreationRequest request;
  private LocalDate dob;

  @BeforeEach
  void initData() {
    dob = LocalDate.of(1990, 1, 1);
    request =
        UserCreationRequest.builder()
            .username("john_integration")
            .firstName("John")
            .lastName("Doe")
            .password("12345678")
            .dob(dob)
            .build();
  }

  @Test
  void createUser_validRequest_success() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    String content = objectMapper.writeValueAsString(request);

    var response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(content))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1000))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.result.username").value("john_integration"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.result.firstName").value("John"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.result.lastName").value("Doe"));
    log.info("Result: {}", response.andReturn().getResponse().getContentAsString());
  }
}
