package com.example.demo.controller;

import com.example.demo.dto.request.UserCreationRequest;

import com.example.demo.dto.request.response.UserResponse;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.boot.test.mock.mockito.MockBean;


import java.time.LocalDate;



@Slf4j
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource("/test.properties") //Dùng file config riêng cho test
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    private UserCreationRequest request;    //DTO dùng chung với code thật
    private UserResponse userResponse;
    private LocalDate dob;

    @BeforeEach             //Chạy trước mỗi test
    void initDate() {
        dob = LocalDate.of(1990, 1, 1); //tạo dữ liệu ngày sinh
        request = UserCreationRequest.builder()         //tạo request giống client gửi lên API
                .username("join")
                .firstname("John")
                .lastname("Doe")
                .password("12345678")
                .dob(dob)
                .build();
        userResponse = UserResponse.builder()   //giả lập dữ liệu trả về từ service
                .id("f6df1ce5e08f")
                .username("join")
                .firstname("John")
                .lastname("Doe")
                .dob(dob)
                .build();
    }

    @Test
    void createUser_validRequest_success() throws Exception {       //Test API /user với dữ liệu hợp lệ
        //Given
        ObjectMapper objectMapper = new ObjectMapper();         //convert object → JSON(Là để giao tiếp giữa các hệ thống / client – server
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(request);
        Mockito.when(userService.createUser(ArgumentMatchers.any())).thenReturn(userResponse);// KHÔNG chạy thật
                                                                                                  //trả về userResponse fake

        //when
        mockMvc.perform(MockMvcRequestBuilders               //giả lập POST /user
                        .post("/user")
                        .contentType(MediaType.APPLICATION_JSON_VALUE) //Dữ liệu tôi gửi lên có định dạng là JSON
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())     //mong đợi HTTP 200
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")//kiểm tra JSON response
                        .value(1000))
                .andExpect((MockMvcResultMatchers.jsonPath("result.id")
                        .value("f6df1ce5e08f"))
                );
        //then
    }

    @Test
    void createUser_usernameInvalid_fail() throws Exception {
        //Given
        request.setUsername("joh");         //username < 4 ký tự //  vi phạm validation
        ObjectMapper objectMapper = new ObjectMapper();     //convert JSON
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(request);


        //when
        mockMvc.perform(MockMvcRequestBuilders.post("/user")      //gọi API
                        .contentType(MediaType.APPLICATION_JSON_VALUE)      //mong đợi HTTP 400//Đảm bảo hệ thống trả về đúng HTTP status và response theo rule mình đã định nghĩa
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")//code lỗi validation
                        .value(1003))
                .andExpect((MockMvcResultMatchers.jsonPath("message")
                        .value("User must be at least 4 characters"))
                );
    }
}