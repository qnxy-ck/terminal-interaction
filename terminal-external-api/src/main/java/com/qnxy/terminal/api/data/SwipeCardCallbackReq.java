package com.qnxy.terminal.api.data;

import java.util.Arrays;

/**
 * 刷卡出货后的回调信息
 *
 * @param errorCodes      错误码数组, 如果没有发生错误则为空
 * @param transactionCode 货物交易码
 * @param alreadyTakenOut 当前货物是否已经被取出
 * @param tagsCode        当前货物的编码
 * @author Qnxy
 */
public record SwipeCardCallbackReq(
        ErrorCode[] errorCodes,
        long transactionCode,
        Boolean alreadyTakenOut,
        String tagsCode
) {


    public static SwipeCardCallbackReq withErrorCode(long transactionCode, ErrorCode[] errorCodes) {
        return new SwipeCardCallbackReq(errorCodes, transactionCode, null, null);
    }

    public static SwipeCardCallbackReq withSuccess(long transactionCode, String tagsCode, boolean alreadyTakenOut) {
        return new SwipeCardCallbackReq(null, transactionCode, alreadyTakenOut, tagsCode);
    }

    @Override
    public String toString() {
        return "SwipeCardCallbackReq{" +
                "errorCodes=" + Arrays.toString(errorCodes) +
                ", transactionCode=" + transactionCode +
                ", alreadyTakenOut=" + alreadyTakenOut +
                ", tagsCode='" + tagsCode + '\'' +
                '}';
    }
}
