package io.cyw.framework.messaging;


import io.smallrye.mutiny.Multi;

/**
 * Interceptor that allows results to be intercepted and modified before they are handled. Implementations are required
 * to operate on a {@link Multi} of results or return a new {@link Flux} which will be passed down the interceptor chain.
 * Also, implementations may make decisions based on the message that was dispatched.
 *
 * @param <M> The type of the message for which the result is going to be intercepted
 * @param <R> The type of the result to be intercepted
 */
@FunctionalInterface
public interface MessageResultHandlerInterceptor<M extends Message<?>, R extends ResultMessage<?>> {

    Multi<R> intercept(M message, Multi<R> results);

}
