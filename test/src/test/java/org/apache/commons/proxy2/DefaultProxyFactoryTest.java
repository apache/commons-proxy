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

package org.apache.commons.proxy2;

import static org.junit.Assert.*;

import java.lang.reflect.Proxy;

import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.invoker.NullInvoker;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the default ProxyFactory provided by {@link ProxyUtils}.
 */
public class DefaultProxyFactoryTest {
    private ProxyFactory proxyFactory;

    @Before
    public void setUp() {
        proxyFactory = ProxyUtils.proxyFactory();
    }

    @Test
    public void testBasic() {
        Foo foo = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE,
                Foo.class);
        assertNotNull(foo);
        assertTrue(foo instanceof Proxy);
    }

    @Test
    public void testSubclassing() {
        Bar bar = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE,
                Bar.class);
        assertNotNull(bar);
    }

    @Test
    public void testCombined() {
        Bar bar = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE,
                Bar.class, Foo.class);
        assertNotNull(bar);
        assertTrue(bar instanceof Foo);
    }

    public interface Foo {
    }

    public static class Bar {
    }
}
