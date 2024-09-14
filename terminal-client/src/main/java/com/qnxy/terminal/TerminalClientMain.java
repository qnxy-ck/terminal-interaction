package com.qnxy.terminal;

import com.qnxy.terminal.message.ErrorCode;
import com.qnxy.terminal.message.ServerMessage;
import com.qnxy.terminal.message.server.AuthorizationApplicationPassed;
import com.qnxy.terminal.message.server.AuthorizedMoveOutGoods;
import com.qnxy.terminal.message.server.VolumeAdjustment;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Qnxy
 */
@Slf4j
public class TerminalClientMain {

    private final Sinks.Many<ByteBuf> sendByteBufSinks = Sinks.many().unicast().onBackpressureBuffer();
    private final AtomicBoolean setHeartbeat = new AtomicBoolean(false);

    private final ByteBufAllocator alloc;

    public TerminalClientMain(Connection connection) {
        connection.addHandlerLast(new LengthFieldBasedFrameDecoder(
                Short.MAX_VALUE - Short.BYTES,
                0,
                Short.BYTES,
                0,
                Short.BYTES
        ));


        alloc = connection.outbound().alloc();

        connection.inbound()
                .receive()
                .map(MessageDecoder::decode)
                .flatMap(this::handlerMessage)
                .subscribe();


        sendByteBufSinks.asFlux()
                .map(it -> {
                    CompositeByteBuf out = alloc.compositeBuffer();
                    int size = it.readableBytes();

                    out.writeShort(size);
                    out.writeBytes(it);
                    return Mono.just(out);
                })
                .flatMap(it -> connection.outbound().send(it))
                .subscribe();
    }

    @SneakyThrows
    public static void main(String[] args) {
        TerminalClientMain clientMain = TcpClient.create()
                .host("localhost")
                .port(9900)
                .connect()
                .map(TerminalClientMain::new)
                .block();

        if (clientMain == null) {
            return;
        }

        clientMain.sendAuthorizationApplication("test_imei");


//        Thread.sleep(1000);
//        clientMain.sendSwipeCard("我是刷卡编码");

        new CountDownLatch(1).await();
    }

    private void send(Consumer<ByteBuf> message) {
        ByteBuf byteBuf = this.alloc.ioBuffer();
        message.accept(byteBuf);

        String s = ByteBufUtil.prettyHexDump(byteBuf);
        log.info("发送消息\n{}", s);
        this.sendByteBufSinks.tryEmitNext(byteBuf);

    }

    private void sendHeartbeat() {
        this.send(buf -> buf.writeByte('P'));
    }

    private void sendSetupSuccessful() {
        this.send(buf -> buf.writeByte('S'));
    }

    private void sendAuthorizationApplication(String imei) {
        this.send(buf -> {
            buf.writeByte('A');
            buf.writeByte(1);
            buf.writeCharSequence(imei, StandardCharsets.UTF_8);
        });
    }

    public void sendAuthorizedMoveOutGoodsReceipt(boolean alreadyTakenOut, String tagsCode) {
        this.send(buf -> {
            buf.writeByte('M');
            buf.writeBoolean(alreadyTakenOut);
            buf.writeCharSequence(tagsCode, StandardCharsets.UTF_8);
        });
    }

    private void sendSwipeCard(String cardCode) {
        this.send(buf -> {
            buf.writeByte('c');
            buf.writeByte(1);
            buf.writeCharSequence(cardCode, StandardCharsets.UTF_8);
        });
    }

    private void sendErrorMessage(ErrorCode... errorCodes) {
        this.send(buf -> {
            buf.writeByte('E');
            buf.writeByte(errorCodes.length);
            for (ErrorCode errorCode : errorCodes) {
                buf.writeByte(errorCode.ordinal());
            }
        });
    }

    private Mono<Void> handlerMessage(ServerMessage serverMessage) {
        log.info("收到消息内容: {}", serverMessage);

        if (serverMessage instanceof AuthorizationApplicationPassed passed) {
            // 授权申请通过
            if (this.setHeartbeat.compareAndSet(false, true)) {
                Flux.interval(passed.heartbeatInterval())
                        .doOnNext(it -> this.sendHeartbeat())
                        .subscribeOn(Schedulers.single())
                        .subscribe();

                this.sendSetupSuccessful();
            } else {
                this.sendErrorMessage(ErrorCode.COMMUNICATION_FAILURE);
            }

            return Mono.empty();
        }

        if (serverMessage instanceof AuthorizedMoveOutGoods) {
            // 授权出货
            return Mono.fromRunnable(() -> this.sendAuthorizedMoveOutGoodsReceipt(true, serverMessage.toString()))
                    .delaySubscription(Duration.ofSeconds(1))
                    .then();
        }

        if (serverMessage instanceof VolumeAdjustment) {
            // 音量调节
            return Mono.fromRunnable(this::sendSetupSuccessful)
                    .delayElement(Duration.ofSeconds(1))
                    .then();
        }

        return Mono.empty();
    }


}
