package com.qnxy.terminal;

import com.qnxy.terminal.api.TerminalExternalService;

/**
 * @author Qnxy
 */
public record ServerContext(
        TerminalExternalService terminalExternalService,
        ServerConfiguration serverConfiguration
) {

}
