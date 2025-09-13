package tracko.exceptions;

import org.springframework.http.HttpStatus;

public class AnthropicApiException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public AnthropicApiException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public AnthropicApiException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}