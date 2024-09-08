package com.qnxy.terminal.api;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Qnxy
 */
public enum ErrorCode {

    /**
     * 出货摸块不存在
     */
    MOVE_OUT_GOODS_MODULE_DOES_NOT_EXIST,

    /**
     * ID卡读取模块不存在
     */
    ID_READ_MODULE_DOES_NOT_EXIST,

    /**
     * 电机编号不可用
     */
    MOTOR_NUMBER_IS_NOT_AVAILABLE,

    /**
     * 光幕检查超时
     */
    LIGHT_CURTAIN_CHECK_TIMED_OUT,

    /**
     * 启动电机超时
     */
    START_MOTOR_TIMED_OUT,

    /**
     * 检测商品编号超时
     */
    DETECT_GOODS_ID_TIMEOUT,

    /**
     * 开门超时
     */
    DOOR_OPEN_TIMEOUT,

    /**
     * 关门超时
     */
    DOOR_CLOSE_TIMEOUT,


    ;

    public static Optional<ErrorCode> errCodeOf(byte errorCode) {
        return Arrays.stream(values())
                .filter(it -> it.ordinal() == errorCode)
                .findFirst();
    }
}
