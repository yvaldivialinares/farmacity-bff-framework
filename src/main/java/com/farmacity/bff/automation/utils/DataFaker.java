package com.farmacity.bff.automation.utils;

import net.datafaker.Faker;

import java.util.Locale;

/**
 * Generates synthetic Argentine test data for use in scenario steps.
 *
 * All generated values are unique per call (random seed), which ensures
 * that no two scenarios collide on the same identifier even when run in parallel.
 *
 * DNI range: 10,000,000 – 39,999,999 (valid Argentine civilian range)
 * Emails: use a test-only domain to avoid conflicts with real accounts
 */
public final class DataFaker {

    private static final Faker FAKER = new Faker(new Locale("es", "AR"));

    private DataFaker() {}

    /** Returns a random 8-digit Argentine DNI (e.g. "27453218"). */
    public static String argentineDni() {
        return String.valueOf(FAKER.number().numberBetween(10_000_000L, 39_999_999L));
    }

    /** Returns a unique QA email that will never collide with a production account. */
    public static String email() {
        return "qa-auto+" + FAKER.number().digits(10) + "@farmacity-test.com";
    }

    public static String firstName()     { return FAKER.name().firstName(); }
    public static String lastName()      { return FAKER.name().lastName(); }
    public static String streetAddress() { return FAKER.address().streetName() + " " + FAKER.address().buildingNumber(); }
    public static String city()          { return "Buenos Aires"; }
    public static String zipCode()       { return "C" + FAKER.number().digits(4) + "AAA"; }
    public static String phone()         { return "011-" + FAKER.number().numberBetween(1000_0000L, 9999_9999L); }

    /** Generates a password that satisfies typical complexity requirements. */
    public static String password() {
        return "Test@" + FAKER.number().digits(6);
    }
}
