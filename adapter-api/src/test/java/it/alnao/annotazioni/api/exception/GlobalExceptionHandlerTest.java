package it.alnao.annotazioni.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    @Test
    void handleValidationErrors_shouldReturnBadRequest() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(java.util.Collections.emptyList());
        MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
        Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);
        ResponseEntity<?> response = handler.handleValidationErrors(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
