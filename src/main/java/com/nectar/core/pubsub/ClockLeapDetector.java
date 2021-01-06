package com.nectar.core.pubsub;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.slf4j.LoggerFactory.getLogger;

@UtilityClass
public class ClockLeapDetector {
    private static final Logger log = getLogger("pubsub.clockleap.log");
    private static final Duration LOG_INTERVAL = Duration.ofSeconds(30);
    private static final long MAX_ACCEPTABLE_OFFSET = LOG_INTERVAL.toSeconds() + 2;
    public static <ID, VALUE> void detectLeapsAndLogProblems(
            final ConcurrentHashMap<ID, Function<VALUE, ?>> registry
    ) {
        Flux.interval(LOG_INTERVAL).timed()
                .subscribe(timed -> {
                    log.info("{} triggers registered at {} | running since {} | {} since last check | log interval is {}",
                            registry.size(),
                            timed.timestamp(),
                            DurationFormatUtils.formatDurationWords(timed.elapsedSinceSubscription().toMillis(), true, true),
                            DurationFormatUtils.formatDurationWords(timed.elapsed().toMillis(), true, true),
                            DurationFormatUtils.formatDurationWords(LOG_INTERVAL.toMillis(), true, true)
                    );
                    if (timed.elapsed().toSeconds() > MAX_ACCEPTABLE_OFFSET) {
                        log.warn("the elapsed time ({}) was greater than the maximum accetable offset {}, possible clock leap or thread starvation!",
                                timed.elapsed().toSeconds(),
                                MAX_ACCEPTABLE_OFFSET
                        );
                    }
                    registry.forEach((key, value) -> log.info("REGISTERED ID: {} | TRIGGER: {}", key, value));
                });
    }
}
