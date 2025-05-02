package ch.uzh.ifi.hase.soprafs24.exceptions;

import org.springframework.http.HttpStatus;

public class AnthropicRateLimitException extends AnthropicApiException {
    public AnthropicRateLimitException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
    }
}