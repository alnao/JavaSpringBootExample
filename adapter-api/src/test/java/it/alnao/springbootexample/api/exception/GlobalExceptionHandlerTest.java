package it.alnao.springbootexample.api.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() { handler = new GlobalExceptionHandler(); }

    @Test
    void handleValidationErrors_shouldReturnBadRequest() {
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(java.util.Collections.emptyList());
        MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
        Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);
        ResponseEntity<?> response = handler.handleValidationErrors(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleValidationErrors_withFieldErrors_includesMessages() {
        BindingResult br = mock(BindingResult.class);
        when(br.getFieldErrors()).thenReturn(List.of(new FieldError("obj", "field", "required")));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(br);
        ResponseEntity<?> resp = handler.handleValidationErrors(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void handleTypeMismatch_returnsBadRequest() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getMessage()).thenReturn("type mismatch");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleTypeMismatch(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    void handleIllegalArgument_returnsBadRequest() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp =
                handler.handleIllegalArgument(new IllegalArgumentException("bad arg"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("bad arg", resp.getBody().getMessaggio());
    }

    @Test
    void handleRuntimeException_returnsInternalServerError() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp =
                handler.handleRuntimeException(new RuntimeException("runtime err"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp =
                handler.handleGenericException(new Exception("generic err"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    void errorResponse_gettersAndSetters_work() {
        GlobalExceptionHandler.ErrorResponse r = new GlobalExceptionHandler.ErrorResponse("E001", "msg", java.time.LocalDateTime.now());
        assertEquals("E001", r.getCodice());
        assertEquals("msg", r.getMessaggio());
        assertNotNull(r.getTimestamp());
        r.setCodice("E002");
        r.setMessaggio("updated");
        assertEquals("E002", r.getCodice());
        assertEquals("updated", r.getMessaggio());
    }
}
