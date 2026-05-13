package com.ecommerce.ecommerce.users;


import com.ecommerce.ecommerce.transaction.Transaction;
import com.ecommerce.ecommerce.transaction.TransactionRepository;
import com.ecommerce.ecommerce.transaction.TransactionType;
import com.ecommerce.ecommerce.users.dto.DepositRequestDTO;
import com.ecommerce.ecommerce.users.dto.UpdateUserRequestDTO;
import com.ecommerce.ecommerce.users.dto.UserResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

 // Add these to the constructor injection in UserService:
    private final TransactionRepository transactionRepository;

    public UserService(UserRepository userRepository,
                       TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
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
        userRepository.save(user);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(dto.getAmount());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        return toDTO(user);
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