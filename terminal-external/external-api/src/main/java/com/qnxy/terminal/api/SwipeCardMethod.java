package com.qnxy.terminal.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Qnxy
 */
@RequiredArgsConstructor
@Getter
public enum SwipeCardMethod {

    ID_CARD_READING,
    SCAN_QR_CODE_ON_CARD,
    ;


    public static Optional<SwipeCardMethod> typeNumberOf(byte typeNumber) {
        return Arrays.stream(values())
                .filter(value -> value.ordinal() == typeNumber)
                .findFirst();
    }
}
