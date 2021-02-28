package io.cyw.framework.utils;

import io.cyw.framework.utils.lang.Nullable;

import java.util.function.Supplier;

public abstract class Assert {

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

    public static void notNull(@Nullable Object object, Supplier<String> supplier) {
        isTrue(object != null, supplier);
    }

}