package com.qnxy.terminal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * 协议版本
 *
 * @author Qnxy
 */
@RequiredArgsConstructor
@Getter
public enum ProtocolVersion {

    VERSION_1((byte) 1),

    ;


    private final byte versionNumber;

    public static Optional<ProtocolVersion> valueOf(byte versionNumber) {
        return Arrays.stream(values())
                .filter(v -> v.getVersionNumber() == versionNumber)
                .findFirst();
    }
}
