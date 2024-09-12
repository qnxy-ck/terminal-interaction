package com.qnxy.terminal.exceptions;

import com.qnxy.terminal.message.client.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Qnxy
 */
@RequiredArgsConstructor
@Getter
public class TerminalExecuteException extends RuntimeException {

    private final ErrorCode[] errorCodes;

}
