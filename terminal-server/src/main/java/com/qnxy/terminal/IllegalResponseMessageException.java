package com.qnxy.terminal;

import lombok.NoArgsConstructor;

/**
 * @author Qnxy
 */
@NoArgsConstructor
public class IllegalResponseMessageException extends RuntimeException {

    public IllegalResponseMessageException(String message) {
        super(message);
    }

}
