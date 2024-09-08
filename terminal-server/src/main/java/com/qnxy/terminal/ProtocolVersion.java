package com.qnxy.terminal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * @author Qnxy
 */
@RequiredArgsConstructor
@Getter
public enum ProtocolVersion {

    VERSION_1(1),

    ;


    private final int versionNumber;

    public static ProtocolVersion valueOf(int versionNumber) {
        return Arrays.stream(values())
                .filter(v -> v.getVersionNumber() == versionNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown protocol version: " + versionNumber));
    }
}
