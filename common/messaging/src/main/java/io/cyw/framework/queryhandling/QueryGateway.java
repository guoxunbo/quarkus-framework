/*
 * Copyright (c) 2010-2020. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cyw.framework.queryhandling;


import io.cyw.framework.messaging.MessageDispatchInterceptorSupport;
import io.cyw.framework.messaging.MessageResultHandlerInterceptorSupport;
import io.cyw.framework.messaging.ResultMessage;
import io.cyw.framework.messaging.responsetypes.ResponseType;
import io.cyw.framework.messaging.responsetypes.ResponseTypes;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.queues.Queues;
import org.reactivestreams.Publisher;

import java.time.Duration;
import java.util.function.Function;

/**
 * Interface towards the Query Handling components of an application. This interface provides a friendlier API toward
 * the query bus.
 *
 * @author Marc Gathier
 * @author Allard Buijze
 * @author Steven van Beelen
 * @author Milan Savic
 * @since 3.1
 */
public interface QueryGateway extends MessageDispatchInterceptorSupport<QueryMessage<?, ?>>, MessageResultHandlerInterceptorSupport<QueryMessage<?, ?>, ResultMessage<?>> {

    /**
     * Sends the given {@code query} over the {@link QueryBus}, expecting a response with the given {@code responseType}
     * from a single source. The query name will be derived from the provided {@code query}. Execution may be
     * asynchronous, depending on the {@code QueryBus} implementation.
     * <p><b>Do note that the {@code query} will not be dispatched until there is a subscription to the resulting {@link
     * Uni}</b></p>
     *
     * @param query        The {@code query} to be sent
     * @param responseType A {@link Class} describing the desired response type
     * @param <R>          The response class contained in the given {@code responseType}
     * @param <Q>          The query class
     * @return A {@link Uni} containing the query result as dictated by the given {@code responseType}
     */
    default <R, Q> Uni<R> query(Q query, Class<R> responseType) {
        return query(QueryMessage.queryName(query), query, responseType);
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus}, expecting a response with the given {@code responseType}
     * from a single source. Execution may be asynchronous, depending on the {@code QueryBus} implementation.
     * <p><b>Do note that the {@code query} will not be dispatched until there is a subscription to the resulting {@link
     * Uni}</b></p>
     *
     * @param queryName    A {@link String} describing the query to be executed
     * @param query        The {@code query} to be sent
     * @param responseType The {@link ResponseType} used for this query
     * @param <R>          The response class contained in the given {@code responseType}
     * @param <Q>          The query class
     * @return A {@link Uni} containing the query result as dictated by the given {@code responseType}
     */
    default <R, Q> Uni<R> query(String queryName, Q query, Class<R> responseType) {
        return query(queryName, query, ResponseTypes.instanceOf(responseType));
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus}, expecting a response in the form of {@code responseType}
     * from a single source. The query name will be derived from the provided {@code query}. Execution may be
     * asynchronous, depending on the {@code QueryBus} implementation.
     * <p><b>Do note that the {@code query} will not be dispatched until there is a subscription to the resulting {@link
     * Uni}</b></p>
     *
     * @param query        The {@code query} to be sent
     * @param responseType The {@link ResponseType} used for this query
     * @param <R>          The response class contained in the given {@code responseType}
     * @param <Q>          The query class
     * @return A {@link Uni} containing the query result as dictated by the given {@code responseType}
     */
    default <R, Q> Uni<R> query(Q query, ResponseType<R> responseType) {
        return query(QueryMessage.queryName(query), query, responseType);
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus}, expecting a response in the form of {@code responseType}
     * from a single source. Execution may be asynchronous, depending on the {@code QueryBus} implementation.
     * <p><b>Do note that the {@code query} will not be dispatched until there is a subscription to the resulting {@link
     * Uni}</b></p>
     *
     * @param queryName    A {@link String} describing the query to be executed
     * @param query        The {@code query} to be sent
     * @param responseType The {@link ResponseType} used for this query
     * @param <R>          The response class contained in the given {@code responseType}
     * @param <Q>          The query class
     * @return A {@link Uni} containing the query result as dictated by the given {@code responseType}
     */
    <R, Q> Uni<R> query(String queryName, Q query, ResponseType<R> responseType);

    /**
     * Use the given {@link Publisher} of {@link QueryMessage}s to send the incoming queries away. Queries will be sent sequentially. Once the
     * result of the Nth query arrives, the (N + 1)th query is dispatched.
     *
     * @param queries a {@link Publisher} stream of queries to be dispatched
     * @return a {@link Multi} of query results. The ordering of query results corresponds to the ordering of queries being
     * dispatched
     * @see #query(String, Object, ResponseType)
     * @see Multi#concatMap(Function)
     */
    default Multi<Object> query(Publisher<QueryMessage<?, ?>> queries) {
        return Multi.createFrom().publisher(queries)
                .concatMap(q -> query(q.getQueryName(), q.getPayload(), q.getResponseType()).toMulti());
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus}, expecting a response in the form of {@code responseType}
     * from several sources. The returned {@link Multi} is completed when a {@code timeout} occurs or when all possible
     * results are received. The query name will be derived from the provided {@code query}. Execution may be
     * asynchronous, depending on the {@code QueryBus} implementation.
     * <p><b>{@code query} will not be dispatched until there is a subscription to the resulting {@link Uni}</b></p>
     * <b>Note</b>: Any {@code null} results will be filtered out by the {@link QueryGateway}. If you require
     * the {@code null} to be returned, we suggest using {@code QueryBus} instead.
     *
     * @param query        The {@code query} to be sent
     * @param responseType The {@link ResponseType} used for this query
     * @param timeout      A timeout of {@code long} for the query
     * @param <R>          The response class contained in the given {@code responseType}
     * @param <Q>          The query class
     * @return A {@link Multi} containing the query results as dictated by the given {@code responseType}
     */
    default <R, Q> Multi<R> scatterGather(Q query, ResponseType<R> responseType, Duration timeout) {
        return scatterGather(QueryMessage.queryName(query), query, responseType, timeout);
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus}, expecting a response in the form of {@code responseType}
     * from several sources. The returned {@link Multi} is completed when a {@code timeout} occurs or when all results
     * are received. Execution may be asynchronous, depending on the {@code QueryBus} implementation.
     * <p><b>{@code query} will not be dispatched until there is a subscription to the resulting {@link Uni}</b></p>
     * <b>Note</b>: Any {@code null} results will be filtered out by the {@link QueryGateway}. If you require
     * the {@code null} to be returned, we suggest using {@code QueryBus} instead.
     *
     * @param queryName    A {@link String} describing the query to be executed
     * @param query        The {@code query} to be sent
     * @param responseType The {@link ResponseType} used for this query
     * @param timeout      A timeout of {@code long} for the query
     * @param <R>          The response class contained in the given {@code responseType}
     * @param <Q>          The query class
     * @return A {@link Multi} containing the query results as dictated by the given {@code responseType}
     */
    <R, Q> Multi<R> scatterGather(String queryName, Q query, ResponseType<R> responseType, Duration timeout);

    /**
     * Uses the given {@link Publisher} of {@link QueryMessage}s to send incoming queries in scatter gather manner. Queries will be sent
     * sequentially. Once the result of Nth query arrives, the (N + 1)th query is dispatched. All queries will be dispatched
     * using given {@code timeout} and {@code timeUnit}.
     *
     * @param queries a {@link Publisher} stream of queries to be dispatched
     * @param timeout A timeout of {@code long} for the query
     * @return a {@link Multi} of query results. The ordering of query results corresponds to the ordering of queries
     * being dispatched
     * @see Multi#concatMap(Function)
     */
    default Multi<Object> scatterGather(Publisher<QueryMessage<?, ?>> queries, Duration timeout) {
        return Multi.createFrom().publisher(queries)
                .concatMap(q -> scatterGather(q.getQueryName(), q.getPayload(), q.getResponseType(), timeout));
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus}, returning the initial result and a stream of incremental
     * updates, received at the moment the query is sent, until it is cancelled by the caller or closed by the emitting
     * side. Should be used when the response type of the initial result and incremental update match.
     * <p><b>{@code query} will not be dispatched until there is a subscription to the resulting {@link Multi}</b></p>
     * <p>
     * <b>Note</b>: Any {@code null} results, on the initial result or the updates, will be filtered out by the
     * {@link QueryGateway}. If you require the {@code null} to be returned for the initial and update results,
     * we suggest using the {@code QueryBus} instead.
     *
     * @param query      The {@code query} to be sent
     * @param resultType The response type used for this query
     * @param <Q>        The type of the query
     * @param <R>        The type of the result (initial and updates)
     * @return Multi which can be used to cancel receiving updates
     * @see QueryBus#subscriptionQuery(SubscriptionQueryMessage)
     */
    default <Q, R> Multi<R> subscriptionQuery(Q query, ResponseType<R> resultType) {
        return subscriptionQuery(query, resultType, resultType).toMulti().flatMap(
                result -> result.initialResult().onItem().transformToMulti(res -> result.updates()).onTermination()
                        .invoke((throwable, flag) -> result.close()));
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus}, returns initial result and keeps streaming incremental
     * updates, received at the moment the query is sent, until it is cancelled by the caller or closed by the emitting
     * side. Should be used when response type of initial result and incremental update match.
     * <p><b>{@code query} will not be dispatched until there is a subscription to the resulting {@link Multi}</b></p>
     * <p>
     * <b>Note</b>: Any {@code null} results, on the initial result or the updates, will be filtered out by the
     * {@link QueryGateway}. If you require the {@code null} to be returned for the initial and update results,
     * we suggest using the {@code QueryBus} instead.
     *
     * @param query      The {@code query} to be sent
     * @param resultType The response type used for this query
     * @param <Q>        The type of the query
     * @param <R>        The type of the result (initial and updates)
     * @return Multi which can be used to cancel receiving updates
     * @see QueryBus#subscriptionQuery(SubscriptionQueryMessage)
     */
    default <Q, R> Multi<R> subscriptionQuery(Q query, Class<R> resultType) {
        return subscriptionQuery(query, ResponseTypes.instanceOf(resultType));
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus}, returns initial result and keeps streaming incremental
     * updates, received at the moment the query is sent, until it is cancelled by the caller or closed by the emitting
     * side. Should be used when initial result contains multiple instances of response type and needs to be flattened.
     * Response type of initial response and incremental updates needs to match.
     * <p><b>{@code query} will not be dispatched until there is a subscription to the resulting {@link Multi}</b></p>
     * <p>
     * <b>Note</b>: Any {@code null} results, on the initial result or the updates, will be filtered out by the
     * {@link QueryGateway}. If you require the {@code null} to be returned for the initial and update results,
     * we suggest using the {@code QueryBus} instead.
     *
     * @param query      The {@code query} to be sent
     * @param resultType The response type used for this query
     * @param <Q>        The type of the query
     * @param <R>        The type of the result (initial and updates)
     * @return Multi which can be used to cancel receiving updates
     * @see QueryBus#subscriptionQuery(SubscriptionQueryMessage)
     */
    default <Q, R> Multi<R> subscriptionQueryMany(Q query, Class<R> resultType) {
        return subscriptionQuery(query, ResponseTypes.multipleInstancesOf(resultType),
                                 ResponseTypes.instanceOf(resultType)).onItem().transformToMulti(
                result -> result.initialResult().onItem().transformToMulti(Multi.createFrom()::iterable).onItem()
                        .transformToMultiAndConcatenate(s -> result.updates()).onTermination()
                        .invoke((signal, flag) -> result.close()));
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus}, streaming incremental updates, received at the moment
     * the query is sent, until it is cancelled by the caller or closed by the emitting side.
     * Should be used when the subscriber is interested only in updates.
     * <p><b>{@code query} will not be dispatched until there is a subscription to the resulting {@link Multi}</b></p>
     * <p>
     * <b>Note</b>: Any {@code null} results, will be filtered out by the
     * {@link QueryGateway}. If you require the {@code null} to be returned for the initial and update results,
     * we suggest using the {@code QueryBus} instead.
     *
     * @param query      The {@code query} to be sent
     * @param resultType The response type used for this query
     * @param <Q>        The type of the query
     * @param <R>        The type of the result (updates)
     * @return Multi which can be used to cancel receiving updates
     * @see QueryBus#subscriptionQuery(SubscriptionQueryMessage)
     */
    default <Q, R> Multi<R> queryUpdates(Q query, ResponseType<R> resultType) {
        return subscriptionQuery(query, ResponseTypes.instanceOf(Void.class), resultType).onItem()
                .transformToMulti(result -> result.updates().onTermination().invoke((signal, flag) -> result.close()));
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus}, streaming incremental updates, received at the moment
     * the query is sent, until it is cancelled by the caller or closed by the emitting side.
     * Should be used when the subscriber is interested only in updates.
     * <p><b>{@code query} will not be dispatched until there is a subscription to the resulting {@link Multi}</b></p>
     * <p>
     * <b>Note</b>: Any {@code null} results, will be filtered out by the
     * {@link QueryGateway}. If you require the {@code null} to be returned for the initial and update results,
     * we suggest using the {@code QueryBus} instead.
     *
     * @param query      The {@code query} to be sent
     * @param resultType The response type used for this query
     * @param <Q>        The type of the query
     * @param <R>        The type of the result (updates)
     * @return Multi which can be used to cancel receiving updates
     * @see QueryBus#subscriptionQuery(SubscriptionQueryMessage)
     */
    default <Q, R> Multi<R> queryUpdates(Q query, Class<R> resultType) {
        return queryUpdates(query, ResponseTypes.instanceOf(resultType));
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus} and returns result containing initial response and
     * incremental updates, received at the moment the query is sent, until it is cancelled by the caller or closed by
     * the emitting side.
     * <p><b>{@code query} will not be dispatched until there is a subscription to the resulting {@link Uni}</b></p>
     * <p>
     * <b>Note</b>: Any {@code null} results, on the initial result or the updates, will be filtered out by the
     * {@link QueryGateway}. If you require the {@code null} to be returned for the initial and update results,
     * we suggest using the {@code QueryBus} instead.
     *
     * @param query               The {@code query} to be sent
     * @param initialResponseType The initial response type used for this query
     * @param updateResponseType  The update response type used for this query
     * @param <Q>                 The type of the query
     * @param <I>                 The type of the initial response
     * @param <U>                 The type of the incremental update
     * @return registration which can be used to cancel receiving updates
     * @see QueryBus#subscriptionQuery(SubscriptionQueryMessage)
     */
    default <Q, I, U> Uni<SubscriptionQueryResult<I, U>> subscriptionQuery(Q query, Class<I> initialResponseType, Class<U> updateResponseType) {
        return subscriptionQuery(QueryMessage.queryName(query), query, initialResponseType, updateResponseType);
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus} and returns result containing initial response and
     * incremental updates, received at the moment the query is sent, until it is cancelled by the caller or closed by
     * the emitting side.
     * <p><b>{@code query} will not be dispatched until there is a subscription to the resulting {@link Uni}</b></p>
     * <p>
     * <b>Note</b>: Any {@code null} results, on the initial result or the updates, will be filtered out by the
     * {@link QueryGateway}. If you require the {@code null} to be returned for the initial and update results,
     * we suggest using the {@code QueryBus} instead.
     *
     * @param queryName           A {@link String} describing query to be executed
     * @param query               The {@code query} to be sent
     * @param initialResponseType The initial response type used for this query
     * @param updateResponseType  The update response type used for this query
     * @param <Q>                 The type of the query
     * @param <I>                 The type of the initial response
     * @param <U>                 The type of the incremental update
     * @return registration which can be used to cancel receiving updates
     * @see QueryBus#subscriptionQuery(SubscriptionQueryMessage)
     */
    default <Q, I, U> Uni<SubscriptionQueryResult<I, U>> subscriptionQuery(String queryName, Q query, Class<I> initialResponseType, Class<U> updateResponseType) {
        return subscriptionQuery(queryName, query, ResponseTypes.instanceOf(initialResponseType),
                                 ResponseTypes.instanceOf(updateResponseType));
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus} and returns result containing initial response and
     * incremental updates, received at the moment the query is sent, until it is cancelled by the caller or closed by
     * the emitting side.
     * <p><b>{@code query} will not be dispatched until there is a subscription to the resulting {@link Uni}</b></p>
     * <p>
     * <b>Note</b>: Any {@code null} results, on the initial result or the updates, will be filtered out by the
     * {@link QueryGateway}. If you require the {@code null} to be returned for the initial and update results,
     * we suggest using the {@code QueryBus} instead.
     *
     * @param query               The {@code query} to be sent
     * @param initialResponseType The initial response type used for this query
     * @param updateResponseType  The update response type used for this query
     * @param <Q>                 The type of the query
     * @param <I>                 The type of the initial response
     * @param <U>                 The type of the incremental update
     * @return registration which can be used to cancel receiving updates
     * @see QueryBus#subscriptionQuery(SubscriptionQueryMessage)
     */
    default <Q, I, U> Uni<SubscriptionQueryResult<I, U>> subscriptionQuery(Q query, ResponseType<I> initialResponseType, ResponseType<U> updateResponseType) {
        return subscriptionQuery(QueryMessage.queryName(query), query, initialResponseType, updateResponseType);
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus} and returns result containing initial response and
     * incremental updates, received at the moment the query is sent, until it is cancelled by the caller or closed by
     * the emitting side.
     * <p><b>{@code query} will not be dispatched until there is a subscription to the resulting {@link Uni}</b></p>
     * <p>
     * <b>Note</b>: Any {@code null} results, on the initial result or the updates, will be filtered out by the
     * {@link QueryGateway}. If you require the {@code null} to be returned for the initial and update results,
     * we suggest using the {@code QueryBus} instead.
     *
     * @param queryName           A {@link String} describing query to be executed
     * @param query               The {@code query} to be sent
     * @param initialResponseType The initial response type used for this query
     * @param updateResponseType  The update response type used for this query
     * @param <Q>                 The type of the query
     * @param <I>                 The type of the initial response
     * @param <U>                 The type of the incremental update
     * @return registration which can be used to cancel receiving updates
     * @see QueryBus#subscriptionQuery(SubscriptionQueryMessage)
     */
    default <Q, I, U> Uni<SubscriptionQueryResult<I, U>> subscriptionQuery(String queryName, Q query, ResponseType<I> initialResponseType, ResponseType<U> updateResponseType) {
        return subscriptionQuery(queryName, query, initialResponseType, updateResponseType, Queues.BUFFER_XS);
    }

    /**
     * Sends the given {@code query} over the {@link QueryBus} and returns result containing initial response and
     * incremental updates, received at the moment the query is sent, until it is cancelled by the caller or closed by
     * the emitting side.
     * <p><b>{@code query} will not be dispatched until there is a subscription to the resulting {@link Uni}</b></p>
     * <p>
     * <b>Note</b>: Any {@code null} results, on the initial result or the updates, will be filtered out by the
     * {@link QueryGateway}. If you require the {@code null} to be returned for the initial and update results,
     * we suggest using the {@code QueryBus} instead.
     *
     * @param queryName           A {@link String} describing query to be executed
     * @param query               The {@code query} to be sent
     * @param initialResponseType The initial response type used for this query
     * @param updateResponseType  The update response type used for this query
     * @param updateBufferSize    The size of buffer which accumulates updates before subscription to the {@code flux}
     *                            is made
     * @param <Q>                 The type of the query
     * @param <I>                 The type of the initial response
     * @param <U>                 The type of the incremental update
     * @return registration which can be used to cancel receiving updates
     * @see QueryBus#subscriptionQuery(SubscriptionQueryMessage)
     */
    <Q, I, U> Uni<SubscriptionQueryResult<I, U>> subscriptionQuery(String queryName, Q query, ResponseType<I> initialResponseType, ResponseType<U> updateResponseType, int updateBufferSize);

    /**
     * Uses the given {@link Publisher} of {@link SubscriptionQueryMessage}s to send incoming queries away. Queries will
     * be sent sequentially. Once the result of Nth query arrives, the (N + 1)th query is dispatched.
     *
     * @param queries a {@link Publisher} stream of queries to be dispatched
     * @return a {@link Multi} of query results. The ordering of query results corresponds to the ordering of queries
     * being dispatched
     * @see #subscriptionQuery(String, Object, Class, Class)
     * @see Multi#concatMap(Function)
     */
    default Multi<SubscriptionQueryResult<?, ?>> subscriptionQuery( // NOSONAR
                                                                    Publisher<SubscriptionQueryMessage<?, ?, ?>> queries) {
        return subscriptionQuery(queries);
    }

    //    /**
    //     * Uses the given {@link Publisher} of {@link SubscriptionQueryMessage}s to send incoming queries away. Queries will
    //     * be sent sequentially. Once the result of Nth query arrives, the (N + 1)th query is dispatched. All queries will
    //     * be dispatched using the given {@code backpressure}.
    //     *
    //     * @param queries      a {@link Publisher} stream of queries to be dispatched
    //     * @param backpressure the backpressure mechanism to deal with producing of incremental updates
    //     * @return a {@link Multi} of query results. The ordering of query results corresponds to the ordering of queries being
    //     * dispatched
    //     *
    //     * @see #subscriptionQuery(String, Object, Class, Class)
    //     * @see Multi#concatMap(Function)
    //     */
    //    default Multi<SubscriptionQueryResult<?, ?>> subscriptionQuery( // NOSONAR
    //                                                                   Publisher<SubscriptionQueryMessage<?, ?, ?>> queries) {
    //        return subscriptionQuery(queries, Queues.BUFFER_XS);
    //    }

    /**
     * Uses the given {@link Publisher} of {@link SubscriptionQueryMessage}s to send incoming queries away. Queries will
     * be sent sequentially. Once the result of Nth query arrives, the (N + 1)th query is dispatched. All queries will
     * be dispatched using given {@code backpressure} and {@code updateBufferSize}.
     *
     * @param queries          a {@link Publisher} stream of queries to be dispatched
     * @param updateBufferSize the size of buffer which accumulates updates before subscription to the {@code flux}
     *                         is made
     * @return a {@link Multi} of query results. The ordering of query results corresponds to the ordering of queries being
     * dispatched
     * @see #subscriptionQuery(String, Object, Class, Class)
     * @see Multi#concatMap(Function)
     */
    default Multi<SubscriptionQueryResult<?, ?>> subscriptionQuery( // NOSONAR
                                                                    Publisher<SubscriptionQueryMessage<?, ?, ?>> queries, int updateBufferSize) {
        return Multi.createFrom().publisher(queries).concatMap(
                q -> subscriptionQuery(q.getQueryName(), q.getPayload(), q.getResponseType(), q.getUpdateResponseType(),
                                       updateBufferSize).toMulti());
    }

}
