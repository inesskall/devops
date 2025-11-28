package dev.yerassyl.aliyev.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Exception Handler for the Event Reservation API
 */
@Slf4j
@ControllerAdvice
public class EventApiExceptionHandler {

    /**
     * Exception handler for invalid requests.
     *
     * @param e InvalidRequestException
     */
    @ExceptionHandler(value = InvalidRequestException.class)
    public ResponseEntity<ApiErrorMessage> handleInvalidRequest(InvalidRequestException e) {
        return handleBadRequest(e);
    }

    /**
     * Exception handler for all other exceptions.
     *
     * @param e Exception
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiErrorMessage> handleGenericException(Exception e) {
        log.error("Internal server error: ", e); // get full stacktrace
        String message = e.getMessage() != null ? e.getMessage() : "Internal server error";
        return new ResponseEntity<>(
            new ApiErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, message), 
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ApiErrorMessage> handleBadRequest(Exception e) {
        log.error(HttpStatus.BAD_REQUEST.getReasonPhrase(), e); // get full stacktrace
        return new ResponseEntity<>(new ApiErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
