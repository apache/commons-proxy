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

package org.apache.commons.proxy2.stub;

import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.cglib.CglibProxyFactory;
import org.apache.commons.proxy2.invoker.NullInvoker;
import org.apache.commons.proxy2.provider.ObjectProviderUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestStubInterceptorBuilder
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private ProxyFactory proxyFactory;
    private StubInterface target;

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Before
    public void setUp()
    {
        this.proxyFactory = new CglibProxyFactory();
        this.target = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE, StubInterface.class);
    }

    @Test(expected = IllegalStateException.class)
    public void testMixingArgumentMatchingStrategies()
    {
        StubInterceptorBuilder builder = new StubInterceptorBuilder(proxyFactory);
        builder.configure(StubInterface.class, new Behavior<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.three(isInstance(String.class), "World")).thenReturn(ObjectProviderUtils.constant("World"));
            }
        });
    }

    @Test
    public void testWithArgumentMatchers()
    {
        StubInterceptorBuilder builder = new StubInterceptorBuilder(proxyFactory);
        Interceptor interceptor = builder.configure(StubInterface.class, new Behavior<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.one(isInstance(String.class))).thenReturn(ObjectProviderUtils.constant("World"));
            }
        }).build();

        final StubInterface proxy = proxyFactory.createInterceptorProxy(target, interceptor, StubInterface.class);
        assertEquals("World", proxy.one("Hello"));
        assertEquals("World", proxy.one("Whatever"));
    }

    @Test
    public void testWithMismatchedArgument()
    {
        StubInterceptorBuilder builder = new StubInterceptorBuilder(proxyFactory);
        Interceptor interceptor = builder.configure(StubInterface.class, new Behavior<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.one(eq("Hello"))).thenReturn("World");
            }
        }).build();
        final StubInterface proxy = proxyFactory.createInterceptorProxy(target, interceptor, StubInterface.class);
        assertEquals("World", proxy.one("Hello"));
        assertEquals(null, proxy.one("Whatever"));
    }

    @Test
    public void testWithMultipleMethodsTrained()
    {
        StubInterceptorBuilder builder = new StubInterceptorBuilder(proxyFactory);
        Interceptor interceptor = builder.configure(StubInterface.class, new Behavior<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.one("Hello")).thenReturn("World");
                when(stub.two("Foo")).thenReturn("Bar");
            }
        }).build();

        final StubInterface proxy = proxyFactory.createInterceptorProxy(target, interceptor, StubInterface.class);
        assertEquals("World", proxy.one("Hello"));
        assertEquals("Bar", proxy.two("Foo"));
    }

    @Test
    public void testWithSingleMethodTrained()
    {
        StubInterceptorBuilder builder = new StubInterceptorBuilder(proxyFactory);
        Interceptor interceptor = builder.configure(StubInterface.class, new Behavior<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.one("Hello")).thenReturn("World");
            }
        }).build();

        final StubInterface proxy = proxyFactory.createInterceptorProxy(target, interceptor, StubInterface.class);
        assertEquals("World", proxy.one("Hello"));
        assertEquals(null, proxy.two("Whatever"));
        assertEquals(null, proxy.one("Mismatch!"));
    }
}
