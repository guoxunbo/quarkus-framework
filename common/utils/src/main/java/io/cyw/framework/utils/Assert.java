package io.cyw.framework.utils;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class Assert {

    private Assert() {
    }

    public static void state(boolean state, Supplier<String> messageSupplier) {
        if (!state) {
            throw new IllegalStateException(messageSupplier.get());
        }
    }

    public static void isFalse(boolean expression, Supplier<String> messageSupplier) {
        isTrue(!expression, messageSupplier);
    }

    public static void isTrue(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalArgumentException(messageSupplier.get());
        }
    }

    public static <T> void nonNull(T value, Supplier<String> messageSupplier) {
        assertThat(value, Objects::nonNull, () -> new IllegalArgumentException(messageSupplier.get()));
    }

    public static <T, X extends Throwable> void assertThat(T value, Predicate<T> assertion, Supplier<? extends X> exceptionSupplier) throws X {
        if (!assertion.test(value)) {
            throw exceptionSupplier.get();
        }
    }

}