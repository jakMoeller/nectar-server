package com.nectar.core.websocket.parsing;

import lombok.NonNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

public class NectarWebSocketMessage<T> {
    private static final String BUFFER_TYPE_DELIMITER = "|";

    private final DataBuffer buffer;

    public WebSocketMessageType getType() {
        return WebSocketMessageType.valueOf(
                buffer.toString(
                        0,
                        getPositionOfDelimiter(),
                        Charset.defaultCharset()
                )
        );
    }

    private int getPositionOfDelimiter() {
        return DataBufferUtils
                .matcher(BUFFER_TYPE_DELIMITER.getBytes())
                .match(buffer);
    }

    public NectarWebSocketMessage(@NonNull DataBuffer dataBuffer) {
        this.buffer = dataBuffer;
    }


    @SuppressWarnings("unchecked")
    public Mono<T> parse() {
        if (getType().equals(WebSocketMessageType.STRING)) {
            final int bufferTypeDelimiterPosition = getPositionOfDelimiter();
            return Mono.just(
                    (T) buffer.toString(
                            bufferTypeDelimiterPosition + 1,
                            buffer.readableByteCount()-bufferTypeDelimiterPosition -1,
                            Charset.defaultCharset()
                    )
            );
        } else {
            throw new IllegalArgumentException("invalid message Type spotted in Message Header");
        }
    }


    public static <T> NectarWebSocketMessage<T> fromDataBuffer(DataBuffer buffer) {
        return new NectarWebSocketMessage<>(buffer);
    }
}
