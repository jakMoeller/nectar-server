package com.nectar.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@ToString
@AllArgsConstructor
@Jacksonized @SuperBuilder
public class Profile {
    String ID;
    String firstName;
    String lastName;
    String streetAddress;
}
