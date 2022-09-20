package com.kc.gitconsumerservice.exceptions;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    private final List<ExceptionRule> exceptionsRules = List.of(
            new ExceptionRule(UserNotFoundException.class, HttpStatus.NOT_FOUND),
            new ExceptionRule(BadRequestException.class, HttpStatus.BAD_REQUEST),
            new ExceptionRule(RateLimitException.class, HttpStatus.FORBIDDEN),
            new ExceptionRule(RequestNotPermitted.class, HttpStatus.TOO_MANY_REQUESTS)

    );

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = getError(request);
        Optional<ExceptionRule> exceptionRuleOptional = exceptionsRules.stream()
                .map(exceptionRule -> exceptionRule.getExceptionClass().isInstance(error) ? exceptionRule : null)
                .filter(Objects::nonNull)
                .findFirst();

        Map<String, Object> attributes = new HashMap<>();

        if (exceptionRuleOptional.isPresent()) {
            ExceptionRule exceptionRule = exceptionRuleOptional.get();
            attributes.put(ErrorAttributesKey.CODE.getKey(), exceptionRule.getStatus().value());
            attributes.put(ErrorAttributesKey.MESSAGE.getKey(), error.getMessage());
        } else {
            attributes.put(ErrorAttributesKey.CODE.getKey(), determineHttpStatus(error).value());
            attributes.put(ErrorAttributesKey.MESSAGE.getKey(), error.getMessage());
        }

        return attributes;
    }


    private HttpStatus determineHttpStatus(Throwable error) {
        if (error instanceof ResponseStatusException) {
            return ((ResponseStatusException) error).getStatus();
        }
        return MergedAnnotations.from(error.getClass(),
                        MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get(ResponseStatus.class)
                .getValue(ErrorAttributesKey.CODE.getKey(), HttpStatus.class)
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
