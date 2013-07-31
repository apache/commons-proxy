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

import java.util.Arrays;

import static org.junit.Assert.*;

public class StubInterceptorBuilderTest
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private ProxyFactory proxyFactory;
    private StubInterface target;
    private StubInterceptorBuilder builder;

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Before
    public void setUp()
    {
        this.proxyFactory = new CglibProxyFactory();
        this.target = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE, StubInterface.class);
        this.builder = new StubInterceptorBuilder(proxyFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowExceptionWithException()
    {
        StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                stub.voidMethod("Hello");
                thenThrow(new IllegalArgumentException("Nope!"));
            }
        });
        proxy.voidMethod("Hello");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowExceptionWithProvidedException()
    {
        StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                stub.voidMethod("Hello");
                thenThrow(ObjectProviderUtils.constant(new IllegalArgumentException("Nope!")));
            }
        });
        proxy.voidMethod("Hello");
    }

    @Test
    public void testWithArrayParameter()
    {
        StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.arrayParameter("One", "Two", "Three")).thenReturn("Four");
            }
        });

        assertEquals("Four", proxy.arrayParameter("One", "Two", "Three"));
    }

    @Test
    public void testAnyMatcher()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.one(any(String.class))).thenReturn("World");
            }
        });
        assertEquals("World", proxy.one("Hello"));
        assertEquals("World", proxy.one(null));
    }

    @Test(expected = IllegalStateException.class)
    public void testMixingArgumentMatchingStrategies()
    {
        builder.trainFor(StubInterface.class, new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.three(isInstance(String.class), "World")).thenAnswer(ObjectProviderUtils.constant("World"));
            }
        });
    }

    @Test(expected = RuntimeException.class)
    public void testThrowingExceptionObject()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.one("Hello")).thenThrow(new RuntimeException("No way, Jose!"));
            }
        });
        proxy.one("Hello");
    }

    @Test(expected = RuntimeException.class)
    public void testThrowingProvidedException()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.one("Hello")).thenThrow(ObjectProviderUtils.constant(new RuntimeException("No way, Jose!")));
            }
        });
        proxy.one("Hello");
    }

    @Test
    public void testWithArgumentMatchers()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.one(isInstance(String.class))).thenAnswer(ObjectProviderUtils.constant("World"));
            }
        });
        assertEquals("World", proxy.one("Hello"));
        assertEquals("World", proxy.one("Whatever"));
    }

    private StubInterface createProxy(Trainer<StubInterface> trainer)
    {
        Interceptor interceptor = builder.trainFor(StubInterface.class, trainer).build();

        return proxyFactory.createInterceptorProxy(target, interceptor, StubInterface.class);
    }

    @Test
    public void testWithStringArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.stringArray()).thenReturn("One", "Two");
            }
        });
        assertArrayEquals(new String[]{"One", "Two"}, proxy.stringArray());
    }

    @Test
    public void testWithBooleanArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.booleanArray()).thenReturn(false, true, false);
            }
        });
        assertTrue(Arrays.equals(new boolean[]{false, true, false}, proxy.booleanArray()));
    }

    @Test
    public void testWithByteArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.byteArray()).thenReturn((byte) 1, (byte) 2);
            }
        });
        assertArrayEquals(new byte[]{1, 2}, proxy.byteArray());
    }

    @Test
    public void testWithShortArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.shortArray()).thenReturn((short) 1, (short) 2);
            }
        });
        assertArrayEquals(new short[]{1, 2}, proxy.shortArray());
    }

    @Test
    public void testWithIntArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.intArray()).thenReturn(1, 2);
            }
        });
        assertArrayEquals(new int[]{1, 2}, proxy.intArray());
    }

    @Test
    public void testWithLongArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.longArray()).thenReturn(1, 2);
            }
        });
        assertArrayEquals(new long[]{1, 2}, proxy.longArray());
    }

    @Test
    public void testWithFloatArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.floatArray()).thenReturn(1f, 2f);
            }
        });
        assertArrayEquals(new float[]{1f, 2f}, proxy.floatArray(), 0.0f);
    }

    @Test
    public void testWithDoubleArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.doubleArray()).thenReturn(1.0, 2.0);
            }
        });
        assertArrayEquals(new double[]{1.0, 2.0}, proxy.doubleArray(), 0.0);
    }

    @Test
    public void testWithCharArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.charArray()).thenReturn('a', 'b', 'c');
            }
        });
        assertArrayEquals(new char[]{'a', 'b', 'c'}, proxy.charArray());
    }

    @Test
    public void testWithMismatchedArgument()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.one(eq("Hello"))).thenReturn("World");
            }
        });
        assertEquals("World", proxy.one("Hello"));
        assertEquals(null, proxy.one("Whatever"));
    }

    @Test
    public void testWithMultipleMethodsTrained()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.one("Hello")).thenReturn("World");
                when(stub.two("Foo")).thenReturn("Bar");
            }
        });
        assertEquals("World", proxy.one("Hello"));
        assertEquals("Bar", proxy.two("Foo"));
    }

    @Test
    public void testWithSingleMethodTrained()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface stub)
            {
                when(stub.one("Hello")).thenReturn("World");
            }
        });
        assertEquals("World", proxy.one("Hello"));
        assertEquals(null, proxy.two("Whatever"));
        assertEquals(null, proxy.one("Mismatch!"));
    }
}
