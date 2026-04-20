package com.example.demo.service;

import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.request.response.UserResponse;

import com.example.demo.entity.User;
import com.example.demo.exception.AppException;

import com.example.demo.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import  static  org.mockito.Mockito.when;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest               //Thông báo cho JUnit biết đây là một bài Integration Test
@TestPropertySource("/test.properties")
public class UserServiceTest {
    @Autowired
    private UserService userService; //Inject "đối tượng thật" mà bạn muốn kiểm tra.
    @MockBean //tạo ra một ông UserRepository giả (Mock)
    private UserRepository userRepository;
    @MockBean
    private UserCreationRequest request;
    private UserResponse userResponse;
    private User user;
    private LocalDate dob;

    @BeforeEach //Hàm này sẽ chạy trước mỗi lần một phương thức @Test được thực thi.
    void initDate() {
        dob = LocalDate.of(1990, 1, 1);
        request = UserCreationRequest.builder()
                .username("join")
                .firstname("John")
                .lastname("Doe")
                .password("12345678")
                .dob(dob)
                .build();
        userResponse = UserResponse.builder()
                .id("f6df1ce5e08f")
                .username("join")
                .firstname("John")
                .lastname("Doe")
                .dob(dob)
                .build();
        user = User.builder()
                .id("f6df1ce5e08f")
                .username("join")
                .firstname("John")
                .lastname("Doe")
                .dob(dob)
                .build();
    }

    @Test
    void createUser_validRequest_success() {
        //Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);//có ai kiểm tra username này tồn tại chưa, hãy trả lời là Chưa (false)".
        when(userRepository.save(any())).thenReturn(user);                         //Nếu có ai gọi lệnh lưu vào DB, hãy trả về đúng đối tượng user có ID f6df1ce5e08f mà tôi đã chuẩn bị"
        when(roleRepository.findById(anyString()))
                .thenReturn(Optional.of(
                        new Role("ADMIN", "Admin role", new HashSet<>())
                ));

        var response = userService.createUser(request);                            //Đây chính là lúc code thật của bạn chạy.
        //Then
        assertThat(response.getId()).isEqualTo("f6df1ce5e08f");                 //Dùng assertThat để so sánh kết quả trả về.
                                                                            // Nếu id đúng là f6df1ce5e08f và username là join thì bài test Pass.
        assertThat(response.getUsername()).isEqualTo("join");
    }


    @Test
    void createUser_userExisted_fail() {     //  mục đích của nó là kiểm tra xem hệ thống có "chặn" lỗi đúng không:
        //Given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        //WhenA
        var exception= assertThrows(AppException.class,
                () -> userService.createUser(request));
        assertThat(exception.getErrorCode().getCode()).isEqualTo(1002);
    }


    @Test
    @WithMockUser(username = "john")
    void getMyInfo_valid_success(){
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        var response = userService.getMyInfo();
        assertThat(response.getUsername()).isEqualTo("join");
        assertThat(response.getId()).isEqualTo("f6df1ce5e08f");
    }
    @Test
    @WithMockUser(username = "john")
    void getMyInfo_userNotFound_error(){
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.ofNullable(null));
        var exception= assertThrows(AppException.class,
                () -> userService.getMyInfo());
        assertThat(exception.getErrorCode().getCode()).isEqualTo(1005);

    }
}