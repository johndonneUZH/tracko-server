package ch.uzh.ifi.hase.soprafs24.constant;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum UserStatus {
    OFFLINE,
    ONLINE;

    @JsonCreator
    public static UserStatus fromString(String value) {
        return value == null ? null : UserStatus.valueOf(value.toUpperCase());
    }
}