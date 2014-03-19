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

import java.lang.reflect.Method;

import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.util.AbstractTestCase;
import org.apache.commons.proxy2.util.Echo;
import org.junit.Test;

public class InvokerInterceptorTest extends AbstractTestCase
{
    // ----------------------------------------------------------------------------------------------------------------------
    // Other Methods
    // ----------------------------------------------------------------------------------------------------------------------

    @Test
    public void testIntercept() throws Throwable
    {
        final InvokerInterceptor interceptor = new InvokerInterceptor(new Invoker()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable
            {
                return "Hello!";
            }
        });
        final Invocation invocation = new MockInvocationBuilder(Echo.class.getDeclaredMethod("echoBack", String.class))
                .withArguments("foo").build();

        assertEquals("Hello!", interceptor.intercept(invocation));
    }

    @Test(expected = NullPointerException.class)
    public void testWithNullInvoker()
    {
        new InvokerInterceptor(null);
    }
}
