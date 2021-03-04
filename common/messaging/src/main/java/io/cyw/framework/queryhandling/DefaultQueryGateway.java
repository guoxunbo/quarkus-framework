package io.cyw.framework.queryhandling;

import io.cyw.framework.common.Registration;
import io.cyw.framework.messaging.MessageDispatchInterceptor;
import io.cyw.framework.messaging.MessageResultHandlerInterceptor;
import io.cyw.framework.messaging.ResultMessage;
import io.cyw.framework.messaging.responsetypes.ResponseType;
import io.cyw.framework.utils.Assert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.cyw.framework.messaging.GenericMessage.asMessage;
import static java.util.Arrays.asList;

public class DefaultQueryGateway implements QueryGateway {

    private final List<MessageDispatchInterceptor<QueryMessage<?, ?>>> dispatchInterceptors;

    private final List<MessageResultHandlerInterceptor<QueryMessage<?, ?>, ResultMessage<?>>> resultInterceptors;

    private final QueryBus queryBus;

    public DefaultQueryGateway(Builder builder) {
        builder.validate();
        this.queryBus = builder.queryBus;
        this.dispatchInterceptors = new CopyOnWriteArrayList<>(builder.dispatchInterceptors);
        this.resultInterceptors = new CopyOnWriteArrayList<>(builder.resultInterceptors);
    }

    @Override
    public Registration registerResultHandlerInterceptor(MessageResultHandlerInterceptor<QueryMessage<?, ?>, ResultMessage<?>> interceptor) {
        resultInterceptors.add(interceptor);
        return () -> resultInterceptors.remove(interceptor);
    }

    @Override
    public <R, Q> Uni<R> query(String queryName, Q query, ResponseType<R> responseType) {
        return Uni.createFrom().<QueryMessage<?, ?>>item(
                () -> new GenericQueryMessage<>(asMessage(query), queryName, responseType)).plug(
                this::processDispatchInterceptors)
                .flatMap(this::dispatchQuery)
                .onItem()
                .transformToMulti(this::processResultsInterceptors).<R>plug(this::getPayload).toUni();
    }

    @Override
    public <R, Q> Multi<R> scatterGather(String queryName, Q query, ResponseType<R> responseType, Duration timeout) {
        return Uni.createFrom().<QueryMessage<?, ?>>item(
                () -> new GenericQueryMessage<>(asMessage(query), queryName, responseType)).plug(
                this::processDispatchInterceptors)
                .flatMap(q -> dispatchScatterGatherQuery(q, timeout.toMillis(), TimeUnit.MILLISECONDS))
                .onItem()
                .transformToMulti(this::processResultsInterceptors).<R>plug(this::getPayload);
    }

    @Override
    public <Q, I, U> Uni<SubscriptionQueryResult<I, U>> subscriptionQuery(String queryName, Q query, ResponseType<I> initialResponseType, ResponseType<U> updateResponseType, int updateBufferSize) {
        return Uni.createFrom().<QueryMessage<?, ?>>item(
                () -> new GenericSubscriptionQueryMessage<>(asMessage(query), initialResponseType,
                                                            updateResponseType)).plug(this::processDispatchInterceptors)
                .map(interceptedQuery -> (SubscriptionQueryMessage<Q, U, I>) interceptedQuery)
                .flatMap(isq -> dispatchSubscriptionQuery(isq, updateBufferSize))
                .plug(this::processSubscriptionQueryResult);
    }

    private <I, U, Q> Uni<SubscriptionQueryResult<I, U>> processSubscriptionQueryResult(Uni<Tuple2<QueryMessage<Q, U>, SubscriptionQueryResult<QueryResponseMessage<U>, SubscriptionQueryUpdateMessage<I>>>> tuple2Uni) {
        return tuple2Uni.map(tuple -> {
            // TODO
            //            return messageWithResult -> messageWithResult.getT2().map(subscriptionResult -> {
            //                Mono<I> interceptedInitialResult = Mono.<QueryMessage<?, ?>>just(messageWithResult.getT1())
            //                        .zipWith(Mono.just(Flux.<ResultMessage<?>>from(subscriptionResult.initialResult())))
            //                        .flatMapMany(this::processResultsInterceptors)
            //                        .<I>transform(this::getPayload)
            //                        .next();
            //                Flux<U> interceptedUpdates = Mono.<QueryMessage<?, ?>>just(messageWithResult.getT1())
            //                        .zipWith(Mono.just(subscriptionResult.updates().<ResultMessage<?>>map(it -> it)))
            //                        .flatMapMany(this::processResultsInterceptors)
            //                        .transform(this::getPayload);
            //                return new DefaultSubscriptionQueryResult<>(interceptedInitialResult,
            //                                                            interceptedUpdates,
            //                                                            subscriptionResult);
            //            });
            return new DefaultSubscriptionQueryResult<>(null, null, null);
        });
    }

    private <Q, I, U> Uni<Tuple2<QueryMessage<Q, I>, SubscriptionQueryResult<QueryResponseMessage<I>, SubscriptionQueryUpdateMessage<U>>>> dispatchSubscriptionQuery(SubscriptionQueryMessage<Q, I, U> queryMessage, int updateBufferSize) {
        Uni<SubscriptionQueryResult<QueryResponseMessage<I>, SubscriptionQueryUpdateMessage<U>>> result = Uni.createFrom()
                .item(() -> queryBus.subscriptionQuery(queryMessage, updateBufferSize));

        return Uni.combine()
                .all()
                .unis(Uni.createFrom().<QueryMessage<Q, I>>item(queryMessage), result)
                .asTuple();
    }

    private Uni<Tuple2<QueryMessage<?, ?>, ResultMessage<?>>> dispatchScatterGatherQuery(QueryMessage<?, ?> queryMessage, long timeout, TimeUnit timeUnit) {
        Multi<ResultMessage<?>> results = Multi.createFrom()
                .deferred(() -> Multi.createFrom()
                        .items(queryBus.scatterGather(queryMessage, timeout, timeUnit)));

        return Uni.combine()
                .all()
                .unis(Uni.createFrom().<QueryMessage<?, ?>>item(queryMessage), Uni.createFrom()
                        .multi(results))
                .asTuple();
    }

    private <R> Multi<R> getPayload(Multi<ResultMessage<?>> resultMessageFlux) {
        return resultMessageFlux.filter(r -> Objects.nonNull(r.getPayload()))
                .map(it -> (R) it.getPayload());
    }

    private Multi<ResultMessage<?>> processResultsInterceptors(Tuple2<QueryMessage<?, ?>, ResultMessage<?>> queryWithResponses) {
        QueryMessage<?, ?> queryMessage = queryWithResponses.getItem1();
        Multi<ResultMessage<?>> queryResultMessage = Multi.createFrom()
                .item(queryWithResponses.getItem2())
                .flatMap(this::mapExceptionalResult);

        return Multi.createFrom()
                .iterable(resultInterceptors)
                .onItem()
                .scan(() -> queryResultMessage, (result, interceptor) -> interceptor.intercept(queryMessage, result))
                .onItem()
                .transformToMulti(Function.identity())
                .concatenate();
    }

    private Multi<? extends ResultMessage<?>> mapExceptionalResult(ResultMessage<?> result) {
        return result.isExceptional() ? Multi.createFrom()
                .failure(result.exceptionResult()) : Multi.createFrom()
                .item(result);
    }

    private Uni<Tuple2<QueryMessage<?, ?>, ResultMessage<?>>> dispatchQuery(QueryMessage<?, ?> queryMessage) {
        Uni<ResultMessage<?>> results = Uni.createFrom()
                .deferred(() -> Uni.createFrom()
                        .completionStage(queryBus.query(queryMessage)));

        return Uni.combine()
                .all()
                .unis(Uni.createFrom().<QueryMessage<?, ?>>item(queryMessage), results)
                .asTuple();
    }

    private Uni<QueryMessage<?, ?>> processDispatchInterceptors(Uni<QueryMessage<?, ?>> queryMessageMono) {
        return Multi.createFrom()
                .iterable(dispatchInterceptors)
                .onItem()
                .scan(() -> queryMessageMono, (queryMessage, interceptor) -> interceptor.intercept(queryMessage))
                .onItem()
                .transformToUni(queryMessageUni -> queryMessageUni)
                .concatenate()
                .toUni();
    }

    @Override
    public Registration registerDispatchInterceptor(MessageDispatchInterceptor<QueryMessage<?, ?>> dispatchInterceptor) {
        dispatchInterceptors.add(dispatchInterceptor);
        return () -> dispatchInterceptors.remove(dispatchInterceptor);
    }

    public static class Builder {

        private QueryBus queryBus;

        private List<MessageDispatchInterceptor<QueryMessage<?, ?>>> dispatchInterceptors = new CopyOnWriteArrayList<>();

        private List<MessageResultHandlerInterceptor<QueryMessage<?, ?>, ResultMessage<?>>> resultInterceptors = new CopyOnWriteArrayList<>();

        /**
         * Sets the {@link QueryBus} used to dispatch queries.
         *
         * @param queryBus a {@link QueryBus} used to dispatch queries
         * @return the current Builder instance, for fluent interfacing
         */
        public Builder queryBus(QueryBus queryBus) {
            Assert.nonNull(queryBus, () -> "QueryBus may not be null");
            this.queryBus = queryBus;
            return this;
        }

        /**
         * Sets {@link MessageDispatchInterceptor}s for {@link QueryMessage}s. Are invoked
         * when a query is being dispatched.
         *
         * @param dispatchInterceptors which are invoked when a query is being dispatched
         * @return the current Builder instance, for fluent interfacing
         */
        @SafeVarargs
        public final Builder dispatchInterceptors(MessageDispatchInterceptor<QueryMessage<?, ?>>... dispatchInterceptors) {
            return dispatchInterceptors(asList(dispatchInterceptors));
        }

        /**
         * Sets the {@link List} of {@link MessageDispatchInterceptor}s for {@link QueryMessage}s. Are invoked
         * when a query is being dispatched.
         *
         * @param dispatchInterceptors which are invoked when a query is being dispatched
         * @return the current Builder instance, for fluent interfacing
         */
        public Builder dispatchInterceptors(List<MessageDispatchInterceptor<QueryMessage<?, ?>>> dispatchInterceptors) {
            this.dispatchInterceptors = dispatchInterceptors != null && dispatchInterceptors.isEmpty() ? new CopyOnWriteArrayList<>(
                    dispatchInterceptors) : new CopyOnWriteArrayList<>();
            return this;
        }

        /**
         * Sets {@link MessageResultHandlerInterceptor}s for {@link ResultMessage}s.
         * Are invoked when a result has been received.
         *
         * @param resultInterceptors which are invoked when a result has been received
         * @return the current Builder instance, for fluent interfacing
         */
        @SafeVarargs
        public final Builder resultInterceptors(MessageResultHandlerInterceptor<QueryMessage<?, ?>, ResultMessage<?>>... resultInterceptors) {
            return resultInterceptors(asList(resultInterceptors));
        }

        /**
         * Sets the {@link List} of {@link MessageResultHandlerInterceptor}s for {@link ResultMessage}s.
         * Are invoked when a result has been received.
         *
         * @param resultInterceptors which are invoked when a result has been received
         * @return the current Builder instance, for fluent interfacing
         */
        public Builder resultInterceptors(List<MessageResultHandlerInterceptor<QueryMessage<?, ?>, ResultMessage<?>>> resultInterceptors) {
            this.resultInterceptors = resultInterceptors != null && !resultInterceptors.isEmpty() ? new CopyOnWriteArrayList<>(
                    resultInterceptors) : new CopyOnWriteArrayList<>();
            return this;
        }


        /**
         * Validate whether the fields contained in this Builder as set accordingly.
         */
        protected void validate() {
            Assert.nonNull(queryBus, () -> "The QueryBus is a hard requirement and should be provided");
        }

        public DefaultQueryGateway build() {
            return new DefaultQueryGateway(this);
        }

    }

}
