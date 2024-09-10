package com.qnxy.terminal;

import reactor.core.publisher.Mono;

/**
 * @author Qnxy
 */
public final class IllegalEncodingException extends RuntimeException {

    public IllegalEncodingException(String message) {
        super(message);
    }

    public static Mono<IllegalEncodingException> ofErrorMessage(String message) {
        return Mono.just(new IllegalEncodingException(message));
    }

}
