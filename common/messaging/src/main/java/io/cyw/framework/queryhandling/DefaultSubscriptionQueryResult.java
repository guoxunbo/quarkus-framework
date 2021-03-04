package io.cyw.framework.queryhandling;

import io.cyw.framework.common.Registration;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class DefaultSubscriptionQueryResult<I, U> implements SubscriptionQueryResult<I, U> {

    private final Uni<I> initialResult;
    private final Multi<U> updates;
    private final Registration registrationDelegate;

    /**
     * Initializes the result with mono and flux used for result retrieval.
     *
     * @param initialResult        mono representing initial result
     * @param updates              flux representing incremental updates
     * @param registrationDelegate delegate which cancels the registration of this result
     */
    public DefaultSubscriptionQueryResult(Uni<I> initialResult, Multi<U> updates, Registration registrationDelegate) {
        this.initialResult = initialResult;
        this.updates = updates;
        this.registrationDelegate = registrationDelegate;
    }

    @Override
    public Uni<I> initialResult() {
        return initialResult;
    }

    @Override
    public Multi<U> updates() {
        return updates;
    }

    @Override
    public boolean cancel() {
        return registrationDelegate.cancel();
    }
}
