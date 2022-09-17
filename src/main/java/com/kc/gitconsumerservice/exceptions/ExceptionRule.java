package com.kc.gitconsumerservice.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class ExceptionRule {
    private Class<?> exceptionClass;
    private HttpStatus status;
}
