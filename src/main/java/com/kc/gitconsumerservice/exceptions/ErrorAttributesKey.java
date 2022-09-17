package com.kc.gitconsumerservice.exceptions;

import lombok.Getter;

@Getter
public enum ErrorAttributesKey {

    CODE("status"),
    MESSAGE("message"),
    TIME("timestamp");

    private final String key;
    ErrorAttributesKey(String key) {
        this.key = key;
    }
}
