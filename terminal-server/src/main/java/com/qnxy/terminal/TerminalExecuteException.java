package com.qnxy.terminal;

import com.qnxy.terminal.api.data.ErrorCode;
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
