package com.qnxy.terminal.message.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * 刷卡方式
 *
 * @author Qnxy
 */
@RequiredArgsConstructor
@Getter
public enum SwipeCardMethod {

    /**
     * ID卡识别
     */
    ID_CARD_READING,

    /**
     * 普通卡片上的二维码扫描
     */
    SCAN_QR_CODE_ON_CARD,
    ;


    public static Optional<SwipeCardMethod> typeNumberOf(byte typeNumber) {
        return Arrays.stream(values())
                .filter(value -> value.ordinal() == typeNumber)
                .findFirst();
    }
}
