package com.nectar.core.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nectar.core.pubsub.InMemoryRefPubSub;
import com.nectar.core.websocket.parsing.NectarWebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatSocket implements WebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final InMemoryRefPubSub<String, String> pubSub = new InMemoryRefPubSub<>();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Mono<Void> inbound = session
                .receive()
                .doOnNext(webSocketMessage -> log.info(webSocketMessage.getPayloadAsText()))
                .map(WebSocketMessage::getPayload)
                .map((Function<DataBuffer, NectarWebSocketMessage<String>>) NectarWebSocketMessage::fromDataBuffer)
                .flatMap(NectarWebSocketMessage::parse)
                .doOnNext(log::info)
                .flatMap(s -> pubSub.publish(s, session.getId()))
                .then();

        Flux<WebSocketMessage> outbound = pubSub.subscribe(session.getId())
                .map(session::textMessage);

        return Mono.zip(
                inbound,
                session.send(outbound)
        ).then();
    }

    @SuppressWarnings("unchecked")
    private <T> Mono<T> read(ObjectMapper mapper, DataBuffer buffer, Class<T> clazz) {
        int typeDelimiterLocationInByteArray = DataBufferUtils.matcher("|".getBytes()).match(buffer);

        String messageType = buffer.toString(0, typeDelimiterLocationInByteArray, Charset.defaultCharset());

        if (messageType.contains("STRING") && String.class.isAssignableFrom(clazz)) {
            return Mono.just(
                    (T) buffer.toString(
                            typeDelimiterLocationInByteArray + 1,
                            buffer.readableByteCount()-typeDelimiterLocationInByteArray -1,
                            Charset.defaultCharset()
                    )
            );
        } else {
            throw new IllegalArgumentException("invalid message Type spotted in Message Header");
        }
    }
}
