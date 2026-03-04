package tax.simulator.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import tax.simulator.api.error.ApiError;
import tax.simulator.api.error.ApiErrorBuilder;
import tax.simulator.api.error.ApiErrorBuilderImpl;

import java.time.LocalDateTime;

/**
 * Classe gérant les exceptions globalement et renvoyant un format d'erreur normalisé {@link ApiError}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Gère les erreurs de validation (paramètres invalides).
     *
     * @param ex exception à gérer
     * @return réponse normalisée avec un code 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        ApiErrorBuilder builder = new ApiErrorBuilderImpl();
        builder.setTimestamp(LocalDateTime.now());
        builder.setStatus(HttpStatus.BAD_REQUEST.value());
        builder.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        builder.setMessage(ex.getMessage());
        return ResponseEntity.badRequest().body(builder.build());
    }

    /**
     * Gère toute exception non prévue.
     *
     * @param ex exception à gérer
     * @return réponse normalisée avec un code 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception ex) {
        logger.error("An unhandled exception occurred.", ex);
        ApiErrorBuilder builder = new ApiErrorBuilderImpl();
        builder.setTimestamp(LocalDateTime.now());
        builder.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        builder.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        builder.setMessage("An unexpected error occurred: " + ex.getMessage());
        return ResponseEntity.internalServerError().body(builder.build());
    }
}
