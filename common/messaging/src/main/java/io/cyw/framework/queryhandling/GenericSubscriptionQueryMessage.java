package io.cyw.framework.queryhandling;

import io.cyw.framework.messaging.Message;
import io.cyw.framework.messaging.responsetypes.ResponseType;

import java.util.Map;

public class GenericSubscriptionQueryMessage<Q, I, U> extends GenericQueryMessage<Q, I>
        implements SubscriptionQueryMessage<Q, I, U> {

    private static final long serialVersionUID = 1429822949683471834L;

    private final ResponseType<U> updateResponseType;

    /**
     * Initializes the message with the given {@code payload}, expected {@code responseType} and expected {@code
     * updateResponseType}. The query name is set to the fully qualified class name of the {@code payload}.
     *
     * @param payload            The payload expressing the query
     * @param responseType       The expected response type
     * @param updateResponseType The expected type of incremental updates
     */
    public GenericSubscriptionQueryMessage(Q payload, ResponseType<I> responseType,
                                           ResponseType<U> updateResponseType) {
        this(payload, payload.getClass().getName(), responseType, updateResponseType);
    }

    /**
     * Initializes the message with the given {@code payload}, {@code queryName}, expected {@code responseType} and
     * expected {@code updateResponseType}.
     *
     * @param payload            The payload expressing the query
     * @param queryName          The name identifying the query to execute
     * @param responseType       The expected response type
     * @param updateResponseType The expected type of incremental updates
     */
    public GenericSubscriptionQueryMessage(Q payload, String queryName, ResponseType<I> responseType,
                                           ResponseType<U> updateResponseType) {
        super(payload, queryName, responseType);
        this.updateResponseType = updateResponseType;
    }

    /**
     * Initializes the message, using given {@code delegate} as the carrier of payload and metadata and given {@code
     * queryName}, expected {@code responseType} and expected {@code updateResponseType}.
     *
     * @param delegate           The message containing the payload and meta data for this message
     * @param queryName          The name identifying the query to execute
     * @param responseType       The expected response type
     * @param updateResponseType The expected type of incremental updates
     */
    public GenericSubscriptionQueryMessage(Message<Q> delegate, String queryName, ResponseType<I> responseType,
                                           ResponseType<U> updateResponseType) {
        super(delegate, queryName, responseType);
        this.updateResponseType = updateResponseType;
    }

    @Override
    public ResponseType<U> getUpdateResponseType() {
        return updateResponseType;
    }

    @Override
    public GenericSubscriptionQueryMessage<Q, I, U> withMetaData(Map<String, ?> metaData) {
        return new GenericSubscriptionQueryMessage<>(getDelegate().withMetaData(metaData),
                                                     getQueryName(),
                                                     getResponseType(),
                                                     updateResponseType);
    }

    @Override
    public GenericSubscriptionQueryMessage<Q, I, U> andMetaData(Map<String, ?> metaData) {
        return new GenericSubscriptionQueryMessage<>(getDelegate().andMetaData(metaData),
                                                     getQueryName(),
                                                     getResponseType(),
                                                     updateResponseType);
    }
}
