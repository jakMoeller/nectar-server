package com.nectar.core.fake;

import com.github.javafaker.Faker;
import com.nectar.core.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.stream.Stream;

@Component
public class FakeDataGenerator {
    private final Faker faker = new Faker();


    public Flux<Profile> getProfiles(Integer dataSize) {

        return Flux.fromStream(
                Stream.generate(UUID::randomUUID)
                        .limit(dataSize)
                        .map(UUID::toString)
        )
                .flatMap(id -> Mono.defer(() -> Mono.just(new FakeProfile(id, faker))));
    }
}
