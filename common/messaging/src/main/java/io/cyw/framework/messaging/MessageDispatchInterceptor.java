/*
 * Copyright (c) 2010-2018. Axon Framework
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

package io.cyw.framework.messaging;

import io.smallrye.mutiny.Uni;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public interface MessageDispatchInterceptor<M extends Message<?>> {

    /**
     * Invoked each time a message is about to be dispatched. The given {@code message} represents the message
     * being dispatched.
     *
     * @param message The message intended to be dispatched
     * @return the message to dispatch
     */
    default M handle(M message) {
        return handle(Collections.singletonList(message)).apply(0, message);
    }

    /**
     * Apply this interceptor to the given list of {@code messages}. This method returns a function that can be
     * invoked to obtain a modified version of messages at each position in the list.
     *
     * @param messages The Messages to pre-process
     * @return a function that processes messages based on their position in the list
     */
    default BiFunction<Integer, M, M> handle(List<? extends M> messages) {
        return (position, message) -> intercept(Uni.createFrom()
                                                        .item(message)).await()
                .indefinitely();
    }

    Uni<M> intercept(Uni<M> message);

}
