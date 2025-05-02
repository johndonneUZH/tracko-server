package ch.uzh.ifi.hase.soprafs24.exceptions;

import org.springframework.http.HttpStatus;

public class AnthropicTimeoutException extends AnthropicApiException {
    public AnthropicTimeoutException(String message) {
        super(message, HttpStatus.GATEWAY_TIMEOUT, "API_TIMEOUT");
    }
}