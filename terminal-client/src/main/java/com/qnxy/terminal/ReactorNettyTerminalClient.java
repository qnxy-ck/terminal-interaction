package com.qnxy.terminal;

import com.qnxy.terminal.message.ClientMessage;
import com.qnxy.terminal.message.ServerMessage;
import com.qnxy.terminal.message.client.AuthorizedMoveOutGoodsReceipt;
import com.qnxy.terminal.message.client.GoodsTagsType;
import com.qnxy.terminal.message.client.SetupSuccessful;
import com.qnxy.terminal.message.server.AuthorizationPassed;
import com.qnxy.terminal.message.server.AuthorizedMoveOutGoods;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;

import java.time.Duration;

/**
 * @author Qnxy
 */

@Slf4j
public class ReactorNettyTerminalClient implements TerminalClient {

    private final Connection connection;
    private final Sinks.Many<ClientMessage> requestSink = Sinks.many().unicast().onBackpressureBuffer();


    public ReactorNettyTerminalClient(Connection connection) {
        this.connection = connection;

        connection.addHandlerLast(new LengthFieldBasedFrameDecoder(
                Short.MAX_VALUE - Short.BYTES,
                0,
                Short.BYTES,
                0,
                Short.BYTES
        ));

        connection.inbound()
                .receive()
                .map(MessageDecoder::decode)
                .doOnNext(it -> log.info("收到消息 {}", it))
                .flatMap(it -> {
                    if (it instanceof AuthorizationPassed) {
                        return Mono.just(SetupSuccessful.INSTANCE);
                    }

                    if (it instanceof AuthorizedMoveOutGoods moveOutGoods) {
//                        return Mono.just(new SwipeCard(SwipeCardMethod.ID_CARD_READING, "卡号xxx"));
//                        return Mono.just(new ErrorMessage(new ErrorCode[]{ErrorCode.DETECT_GOODS_ID_TIMEOUT}));
                        return Mono.just(new AuthorizedMoveOutGoodsReceipt(
                                        true,
                                        moveOutGoods.readTags() ? GoodsTagsType.QR_CODE : GoodsTagsType.NOTHING,
                                        moveOutGoods.readTags() ? "货物标签xxx" : null
                                ))
                                .delaySubscription(Duration.ofSeconds(3))
                                ;
                    }

                    return Mono.empty();
                })
                .doOnNext(this::send)
                .subscribe();


        final MessageEncoder messageEncoder = new MessageEncoder(connection.outbound().alloc());
        requestSink.asFlux()
                .doOnNext(it -> log.info("发送消息 {}", it))
                .map(messageEncoder::encode)
                .flatMap(connection.outbound()::send, 1)
                .subscribe();

    }


    @Override
    public Mono<Void> close() {
        return null;
    }

    @Override
    public Mono<ServerMessage> exchange(ClientMessage message) {
        return null;
    }

    @Override
    public void send(ClientMessage message) {
        this.requestSink.tryEmitNext(message);
    }
}
