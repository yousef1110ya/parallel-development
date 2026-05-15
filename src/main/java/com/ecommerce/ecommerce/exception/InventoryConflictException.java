package com.ecommerce.ecommerce.exception;

public class InventoryConflictException extends RuntimeException {
    public InventoryConflictException(String message) {
        super(message);
    }
}
