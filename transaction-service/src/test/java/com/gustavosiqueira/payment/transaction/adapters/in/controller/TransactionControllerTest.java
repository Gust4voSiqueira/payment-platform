package com.gustavosiqueira.payment.transaction.adapters.in.controller;

import com.gustavosiqueira.payment.transaction.adapters.in.controller.dto.CreateTransactionRequest;
import com.gustavosiqueira.payment.transaction.application.use_case.CreateTransactionUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @InjectMocks
    TransactionController transactionController;

    @Mock
    CreateTransactionUseCase createTransactionUseCase;

    @Test
    @DisplayName("Deve retornar 201 quando a transação for criada com sucesso")
    void shouldReturn201WhenTransactionIsCreatedSuccessfully() {
        var request = mock(CreateTransactionRequest.class);

        var response = transactionController.createTransaction(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(createTransactionUseCase, times(1))
                .execute(request);
    }

    @Test
    @DisplayName("Deve retornar 400 quando ocorrer erro ao criar a transação")
    void shouldReturn400WhenUseCaseThrowsException() {
        var request = mock(CreateTransactionRequest.class);
        var errorMessage = "Invalid transaction";

        doThrow(new RuntimeException(errorMessage))
                .when(createTransactionUseCase)
                .execute(request);

        var response = transactionController.createTransaction(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(createTransactionUseCase, times(1))
                .execute(request);
    }
}
