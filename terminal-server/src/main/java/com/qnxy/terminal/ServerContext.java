package com.qnxy.terminal;

import com.qnxy.terminal.external.TerminalExternalService;

/**
 * @author Qnxy
 */
public record ServerContext(
        TerminalExternalService terminalExternalService,
        ServerConfiguration serverConfiguration
) {

}
