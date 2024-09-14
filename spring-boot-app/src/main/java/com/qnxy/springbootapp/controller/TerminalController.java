package com.qnxy.springbootapp.controller;

import com.qnxy.terminal.CallingFlow;
import com.qnxy.terminal.message.client.AuthorizedMoveOutGoodsReceipt;
import com.qnxy.terminal.message.server.AuthorizedMoveOutGoods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;


/**
 * @author Qnxy
 */
@RequestMapping("/test")
@RestController
@Slf4j
public class TerminalController {


    @GetMapping(value = "/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ParallelFlux<AuthorizedMoveOutGoodsReceipt> authMoveOutGoods(@PathVariable Long id) {
        return Flux.range(0, 10)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(it -> CallingFlow.authorizedMoveOutGoods(
                        id,
                        new AuthorizedMoveOutGoods(it.byteValue(), true)
                ));
    }
}
