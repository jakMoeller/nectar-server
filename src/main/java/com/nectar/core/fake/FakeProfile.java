package com.nectar.core.fake;

import com.github.javafaker.Faker;
import com.nectar.core.Profile;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Jacksonized
@SuperBuilder
public class FakeProfile extends Profile {

    FakeProfile(String id, Faker faker) {
        super(
                id,
                faker.name().firstName(),
                faker.name().lastName(),
                faker.address().streetAddress()
        );
    }
}
