package com.ecommerce.ecommerce.transaction;

import com.ecommerce.ecommerce.transaction.dto.TransactionResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // GET /api/transactions/me
    @GetMapping("/me")
    public ResponseEntity<List<TransactionResponseDTO>> getMyTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                transactionService.getMyTransactions(userDetails.getUsername()));
    }
}