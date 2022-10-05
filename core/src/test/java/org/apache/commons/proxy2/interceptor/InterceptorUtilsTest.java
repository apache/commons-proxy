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

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.provider.ObjectProviderUtils;
import org.apache.commons.proxy2.util.AbstractTestCase;
import org.apache.commons.proxy2.util.Echo;
import org.junit.Test;
import org.junit.jupiter.api.function.Executable;

public class InterceptorUtilsTest extends AbstractTestCase
{
    @Test
    public void testConstant() throws Throwable
    {
        Interceptor interceptor = InterceptorUtils.constant("Hello!");
        Invocation invocation = mockInvocation(Echo.class, "echoBack", String.class).withArguments("World!").build();
        assertEquals("Hello!", interceptor.intercept(invocation));
    }

    @Test
    public void testProvider() throws Throwable
    {
        Interceptor interceptor = InterceptorUtils.provider(ObjectProviderUtils.constant("Foo!"));
        Invocation invocation = mockInvocation(Echo.class, "echoBack", String.class).withArguments("World!").build();
        assertEquals("Foo!", interceptor.intercept(invocation));
    }

    @Test
    public void testThrowingExceptionObject()
    {
        final Interceptor interceptor = InterceptorUtils.throwing(new RuntimeException("Oops!"));
        final Invocation invocation = mockInvocation(Echo.class, "echoBack", String.class).withArguments("World!").build();
        // FIXME Simplification once upgraded to Java 1.8
        final Executable testMethod = new Executable() {
            @Override
            public void execute() throws Throwable {
                interceptor.intercept(invocation);
            }
        };
        assertThrows(RuntimeException.class, testMethod);
    }

    @Test
    public void testThrowingProvidedException()
    {
        final Interceptor interceptor = InterceptorUtils
                .throwing(ObjectProviderUtils.constant(new RuntimeException("Oops!")));
        final Invocation invocation = mockInvocation(Echo.class, "echoBack", String.class).withArguments("World!").build();
        // FIXME Simplification once upgraded to Java 1.8
        final Executable testMethod = new Executable() {
            @Override
            public void execute() throws Throwable {
                interceptor.intercept(invocation);
            }
        };
        assertThrows(RuntimeException.class, testMethod);
    }

}
