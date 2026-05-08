package com.ecommerce.ecommerce.services;

import org.springframework.dao.OptimisticLockingFailureException;

public @interface Retryable {

    Class<OptimisticLockingFailureException> value();

    int maxAttempts();

}
