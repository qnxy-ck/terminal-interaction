package com.qnxy.terminal.message.client;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Qnxy
 */
public enum GoodsTagsType {
    RFID,
    QR_CODE,
    NOTHING;

    public static Optional<GoodsTagsType> typeNumberOf(byte typeNumber) {
        return Arrays.stream(values())
                .filter(it -> it.ordinal() == typeNumber)
                .findFirst();
    }

}
