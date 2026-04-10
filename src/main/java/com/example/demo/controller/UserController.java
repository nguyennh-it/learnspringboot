package com.example.demo.controller;

import com.example.demo.dto.request.ApiResponse;
import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.request.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserController {
    UserService userService;
    @PostMapping
    ApiResponse<User> createUser(@RequestBody  @Valid  UserCreationRequest request ){
        ApiResponse<User> apiResponse=new ApiResponse<>();
        apiResponse.setResult(userService.createUser(request));
        return  apiResponse ;
    }
    @GetMapping
    List<User> getUsers(){
     return  userService.getUser();
    }
    @GetMapping("/{userId}")
    UserResponse getUser(@PathVariable("userId") String userId){
    return userService.getUser(userId);
    }
    @PutMapping("/{userId}")
    UserResponse updateUser(@PathVariable String userId,  @RequestBody UserUpdateRequest request){
        return userService.updateUser(userId,request);
    }
    @DeleteMapping("/{userId}")
    String deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
        return  "User has been deleted";
    }
}

