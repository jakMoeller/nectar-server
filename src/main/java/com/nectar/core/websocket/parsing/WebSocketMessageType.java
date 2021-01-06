package com.nectar.core.websocket.parsing;

import lombok.Getter;

@Getter
public enum WebSocketMessageType {
    JSON("JSON"),
    STRING("STRING");

    private final String type;

    WebSocketMessageType(String type) {
        this.type = type;
    }
}
