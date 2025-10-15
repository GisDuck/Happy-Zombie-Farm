package game.Happy_Zombie_Farm.api;

import game.Happy_Zombie_Farm.exception.NoPlayerException;
import org.springframework.http.*;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoPlayerException.class)
    public ResponseEntity<ErrorResponse> handleNoPlayer(NoPlayerException ex, WebRequest req) {
        String path = req.getDescription(false).replace("uri=", "");
        ErrorResponse body = ErrorResponse.of(404, ex.getMessage(), path);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ErrorResponseException ex, WebRequest req) {
        String path = req.getDescription(false).replace("uri=", "");
        int code = ex.getStatusCode().value();
        ErrorResponse body = ErrorResponse.of(code, ex.getMessage(), path);
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
        String path = req.getDescription(false).replace("uri=", "");
        String msg = ex.getBindingResult().getAllErrors().stream()
                .findFirst().map(e -> e.getDefaultMessage()).orElse("Validation error");
        ErrorResponse body = ErrorResponse.of(400, msg, path);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, WebRequest req) {
        String path = req.getDescription(false).replace("uri=", "");
        ErrorResponse body = ErrorResponse.of(500, "Internal server error", path);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
