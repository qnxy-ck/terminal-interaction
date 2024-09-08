package com.qnxy.terminal.api.data;

/**
 * @param errorCodes      错误码数组, 如果没有发生错误则为空
 * @param transactionCode 货物交易码
 * @param alreadyTakenOut 当前货物是否已经被取出
 * @param goodsTagsType   当前货物标签的类型
 * @param tagsCode        当前货物的编码
 * @author Qnxy
 */
public record SwipeCardCallbackReq(
        ErrorCode[] errorCodes,
        long transactionCode,
        boolean alreadyTakenOut,
        GoodsTagsType goodsTagsType,
        String tagsCode
) {


    public static SwipeCardCallbackReq withErrorCode(long transactionCode, ErrorCode[] errorCodes) {
        return new SwipeCardCallbackReq(errorCodes, transactionCode, false, null, null);
    }

    public static SwipeCardCallbackReq withSuccess(long transactionCode, String tagsCode, boolean alreadyTakenOut, GoodsTagsType goodsTagsType) {
        return new SwipeCardCallbackReq(null, transactionCode, alreadyTakenOut, goodsTagsType, tagsCode);
    }
}
