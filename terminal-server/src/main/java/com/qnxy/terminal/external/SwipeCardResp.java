package com.qnxy.terminal.external;

/**
 * @param transactionCode 交易码
 * @param cargoLocation   货物位置
 * @param readTags        是否读取货物上的标签
 * @author qnxy
 */
public record SwipeCardResp(
        long transactionCode,
        byte cargoLocation,
        boolean readTags
) {
}
