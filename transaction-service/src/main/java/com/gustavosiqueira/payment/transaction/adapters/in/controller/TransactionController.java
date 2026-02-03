package com.gustavosiqueira.payment.transaction.adapters.in.controller;

import com.gustavosiqueira.payment.transaction.adapters.in.controller.dto.CreateTransactionRequest;
import com.gustavosiqueira.payment.transaction.application.use_case.CreateTransactionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;

    @PostMapping
    public ResponseEntity<Object> createTransaction(@RequestBody CreateTransactionRequest createTransactionRequest) {
        try {
            createTransactionUseCase.execute(createTransactionRequest);
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
