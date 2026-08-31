package be.kdg.banditgamesbackend.user.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String address) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public Email {
        Objects.requireNonNull(address, "Email address cannot be null");
        if (!isValid(address)) {
            throw new IllegalArgumentException("Invalid email address: " + address);
        }
        // Normalize email to lowercase for case-insensitive comparison
        address = address.toLowerCase();
    }

    private static boolean isValid(String address) {
        return address != null && !address.isBlank() && EMAIL_PATTERN.matcher(address).matches();
    }

    @Override
    public String toString() {
        return address;
    }
}