package com.qnxy.terminal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Qnxy
 */
@RequiredArgsConstructor
@Getter
public enum ProtocolVersion {

    VERSION_1(1),

    ;


    private final int versionNumber;

    public static Optional<ProtocolVersion> valueOf(int versionNumber) {
        return Arrays.stream(values())
                .filter(v -> v.getVersionNumber() == versionNumber)
                .findFirst();
    }
}
