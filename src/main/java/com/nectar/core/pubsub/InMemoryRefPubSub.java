package com.nectar.core.pubsub;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
public class InMemoryRefPubSub<PUBLISHER, VALUE> {
    private final ConcurrentHashMap<PUBLISHER, Function<VALUE, ?>> registry = new ConcurrentHashMap<>();

    public InMemoryRefPubSub() {
        ClockLeapDetector.detectLeapsAndLogProblems(registry);
    }

    public Mono<Void> publish(
            final VALUE element,
            final PUBLISHER id
    ) {
        return Mono.fromRunnable(() -> {
            log.info("PUBLISH MESSAGE {} FROM {}", element, id);
            registry.values().forEach(trigger -> trigger.apply(element));
        });
    }

    public Flux<VALUE> subscribe(
            final PUBLISHER id
    ) {
        return Flux.<VALUE>create(sink -> {
            final Function<VALUE, ?> trigger = sink::next;
            final Disposable disposeTrigger = () -> this.registry.remove(id);
            this.registry.put(id, trigger);
            sink.onDispose(disposeTrigger);
            sink.onCancel(disposeTrigger);
        })
                .doOnSubscribe(subscription -> log.info("SUBSCRIBE:{}", id))
                .doOnNext(info -> log.info("DISTRIBUTE {} TO {}", info, id));
    }
}
