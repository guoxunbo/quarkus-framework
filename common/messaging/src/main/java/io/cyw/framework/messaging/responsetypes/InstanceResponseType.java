/*
 * Copyright (c) 2010-2019. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cyw.framework.messaging.responsetypes;


import java.beans.ConstructorProperties;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.Future;

import static io.cyw.framework.utils.ReflectionUtils.unwrapIfType;

/**
 * A {@link ResponseType} implementation that will match with query
 * handlers which return a single instance of the expected response type. If matching succeeds, the
 * {@link ResponseType#convert(Object)} function will be called, which will cast the query handler it's response to
 * {@code R}.
 *
 * @param <R> The response type which will be matched against and converted to
 * @author Steven van Beelen
 * @since 3.2
 */
public class InstanceResponseType<R> extends AbstractResponseType<R> {

    /**
     * Instantiate a {@link InstanceResponseType} with the given
     * {@code expectedResponseType} as the type to be matched against and to which the query response should be
     * converted to.
     *
     * @param expectedResponseType the response type which is expected to be matched against and returned
     */
    @ConstructorProperties({"expectedResponseType"})
    public InstanceResponseType(Class<R> expectedResponseType) {
        super(expectedResponseType);
    }

    /**
     * Match the query handler its response {@link Type} with this implementation its responseType
     * {@code R}.
     * Will return true if the expected type is assignable to the response type, taking generic types into account.
     *
     * @param responseType the response {@link Type} of the query handler which is matched against
     * @return true if the response type is assignable to the expected type, taking generic types into account
     */
    @Override
    public boolean matches(Type responseType) {
        Type unwrapped = unwrapIfType(responseType, Future.class, Optional.class);
        return isGenericAssignableFrom(unwrapped) || isAssignableFrom(unwrapped);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<R> responseMessagePayloadType() {
        return (Class<R>) expectedResponseType;
    }

    @Override
    public String toString() {
        return "InstanceResponseType{" + expectedResponseType + "}";
    }

}
