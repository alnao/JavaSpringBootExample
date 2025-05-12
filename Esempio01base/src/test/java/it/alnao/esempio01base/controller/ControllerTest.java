package it.alnao.esempio01base.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ControllerTest {

    @InjectMocks
    private Controller controller;

    @Test
    void hello_ShouldReturnHelloWorldMessage() {
        // Given
        String expectedMessage = "Hello World from Example01 by AlNao!";

        // When
        String actualMessage = controller.hello();

        // Then
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void response_ShouldReturnOkResponseWithCorrectJson() {
        // Given
        String expectedBody = "{\"response\":\"ok\"}";
        HttpStatus expectedStatus = HttpStatus.OK;

        // When
        ResponseEntity<String> response = controller.response();

        // Then
        assertEquals(expectedStatus, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
    }
}