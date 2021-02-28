package io.cyw.framework.messaging;


import io.cyw.framework.common.Registration;

/**
 * Interface marking components capable of registering a {@link MessageResultHandlerInterceptor}. Generally, these are
 * messaging components injected into the receiving end of the communication.
 *
 * @param <M> The type of the message for which the result is going to be intercepted
 * @param <R> The type of the result to be intercepted
 * @author Milan Savic
 * @since 4.4.2
 */
public interface MessageResultHandlerInterceptorSupport<M extends Message<?>, R extends ResultMessage<?>> {

    /**
     * Register the given {@link MessageResultHandlerInterceptor}. After registration, the interceptor will be invoked
     * for each result message received on the messaging component that it was registered to.
     *
     * @param interceptor The reactive interceptor to register
     * @return a Registration, which may be used to unregister the interceptor
     */
    Registration registerResultHandlerInterceptor(MessageResultHandlerInterceptor<M, R> interceptor);

}
