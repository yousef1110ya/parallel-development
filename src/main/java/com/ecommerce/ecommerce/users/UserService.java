package com.ecommerce.ecommerce.users;


import com.ecommerce.ecommerce.users.dto.DepositRequestDTO;
import com.ecommerce.ecommerce.users.dto.UpdateUserRequestDTO;
import com.ecommerce.ecommerce.users.dto.UserResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDTO getProfile(String email) {
        User user = findByEmailOrThrow(email);
        return toDTO(user);
    }

    @Transactional
    public UserResponseDTO updateProfile(String email, UpdateUserRequestDTO dto) {
        User user = findByEmailOrThrow(email);
        user.setName(dto.getName());
        return toDTO(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO deposit(String email, DepositRequestDTO dto) {
        User user = findByEmailOrThrow(email);
        user.setBalance(user.getBalance().add(dto.getAmount()));
        return toDTO(userRepository.save(user));
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // ---- Helpers ----

    private User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    private UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getBalance(),
                user.getCreatedAt()
        );
    }
}