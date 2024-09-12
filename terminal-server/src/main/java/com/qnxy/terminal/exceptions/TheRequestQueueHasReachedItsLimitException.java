package com.qnxy.terminal.exceptions;

/**
 * 服务端发出主动消息时, 如果达到积压上限将调用失败直接抛出该异常
 *
 * @author Qnxy
 */
public class TheRequestQueueHasReachedItsLimitException extends RuntimeException {
}
