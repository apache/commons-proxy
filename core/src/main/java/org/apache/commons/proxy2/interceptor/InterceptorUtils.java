/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.proxy2.interceptor;

import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.provider.ObjectProviderUtils;

public final class InterceptorUtils
{
//----------------------------------------------------------------------------------------------------------------------
// Static Methods
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Creates an {@link Interceptor} which always returns a constant value (for all methods).
     * @param value the constant
     * @return an {@link Interceptor} which always returns a constant value (for all methods)
     */
    public static Interceptor constant(Object value)
    {
        return new ObjectProviderInterceptor(ObjectProviderUtils.constant(value));
    }

    /**
     * Creates an {@link Interceptor} which returns the resulting object from an
     * object provider (for all methods).
     * @param provider the object provider
     * @return an {@link Interceptor} which returns the resulting object from an
     * object provider (for all methods)
     */
    public static Interceptor provider(ObjectProvider<?> provider)
    {
        return new ObjectProviderInterceptor(provider);
    }

    /**
     * Creates an {@link Interceptor} which throws a specific exception (for all methods).
     * @param e the exception
     * @return an {@link Interceptor} which throws a specific exception (for all methods)
     */
    public static Interceptor throwing(Exception e)
    {
        return new ThrowingInterceptor(ObjectProviderUtils.constant(e));
    }

    /**
     * Creates an {@link Interceptor} which throws the exception provided by an object
     * provider (for all methods).
     * @param provider the object provider
     * @return an {@link Interceptor} which throws the exception provided by an object
     * provider (for all methods)
     */
    public static Interceptor throwing(ObjectProvider<? extends Exception> provider)
    {
        return new ThrowingInterceptor(provider);
    }

    /**
     * Creates an {@link Interceptor} that delegates to the specified {@link Invoker}.
     * @param invoker delegate
     * @return invoker {@link Interceptor}
     */
    public static Interceptor invoking(Invoker invoker)
    {
        return new InvokerInterceptor(invoker);
    }

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    private InterceptorUtils()
    {
    }
}
