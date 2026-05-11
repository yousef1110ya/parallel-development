package com.ecommerce.ecommerce.users;


import com.ecommerce.ecommerce.users.dto.DepositRequestDTO;
import com.ecommerce.ecommerce.users.dto.UpdateUserRequestDTO;
import com.ecommerce.ecommerce.users.dto.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users/me
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    // PUT /api/users/me
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserRequestDTO dto) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), dto));
    }

    // POST /api/users/me/deposit
    @PostMapping("/me/deposit")
    public ResponseEntity<UserResponseDTO> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DepositRequestDTO dto) {
        return ResponseEntity.ok(userService.deposit(userDetails.getUsername(), dto));
    }

    // GET /api/users  (Admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // DELETE /api/users/{id}  (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}