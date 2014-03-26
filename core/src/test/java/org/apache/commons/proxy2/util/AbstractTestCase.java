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
package org.apache.commons.proxy2.util;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.ProxyUtils;

/**
 * @author James Carman
 * @since 2.0
 */
public abstract class AbstractTestCase
{
    //**********************************************************************************************************************
    // Other Methods
    //**********************************************************************************************************************

    protected void assertSerializable(Object o)
    {
        assertTrue(o instanceof Serializable);
        SerializationUtils.clone((Serializable) o);
    }

    protected MockInvocationBuilder mockInvocation(Class<?> type, String name, Class<?>... argumentTypes)
    {
        try
        {
            return new MockInvocationBuilder(Validate.notNull(type).getMethod(name, argumentTypes));
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException("Method not found.", e);
        }
    }

    protected static final class MockInvocationBuilder implements Builder<Invocation>
    {
        private final Method method;
        private Object[] arguments = ProxyUtils.EMPTY_ARGUMENTS;
        private Object returnValue = null;

        public MockInvocationBuilder(Method method)
        {
            this.method = method;
        }

        public MockInvocationBuilder withArguments(Object... arguments)
        {
            this.arguments = arguments;
            return this;
        }

        public MockInvocationBuilder returning(Object value)
        {
            this.returnValue = value;
            return this;
        }

        @Override
        public Invocation build()
        {
            return new MockInvocation(method, returnValue, arguments);
        }
    }
}
