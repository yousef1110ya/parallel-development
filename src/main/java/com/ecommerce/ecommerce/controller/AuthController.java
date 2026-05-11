package com.ecommerce.ecommerce.controller;


import com.ecommerce.ecommerce.dto.AuthResponse;
import com.ecommerce.ecommerce.dto.LoginRequest;
import com.ecommerce.ecommerce.dto.RegisterRequest;
import com.ecommerce.ecommerce.model.User;
import com.ecommerce.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }
    
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    
   
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}