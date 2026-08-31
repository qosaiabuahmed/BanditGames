package be.kdg.banditgamesbackend.common.validation;

import org.springframework.util.Assert;

import java.util.List;

public class Validators {
    private Validators() {}

    public static void requireNonBlank(String value, String fieldName) {
        Assert.hasText(value, fieldName + " cannot be null or blank");
    }

    public static void requireNonNull(Object value, String fieldName) {
        Assert.notNull(value, fieldName + " cannot be null");
    }

    public static void requireNonEmpty(List<?> list, String fieldName) {
        Assert.notEmpty(list, fieldName + " cannot be null or empty");
    }

}
