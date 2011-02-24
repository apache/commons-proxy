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

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.invoker.NullInvoker;
import org.apache.commons.proxy2.provider.BeanProvider;
import org.apache.commons.proxy2.provider.ConstantProvider;
import org.apache.commons.proxy2.stub.StubConfigurer;
import org.apache.commons.proxy2.stub.StubProxyFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link StubProxyFactory}.
 */
public abstract class AbstractStubProxyFactoryTest {
    private static final StubConfigurer<Foo> FOO_CONFIGURER = new StubConfigurer<Foo>() {

        @Override
        protected void configure(Foo stub) {
            when(stub.fooString()).thenReturn("foo").when(stub.fooInt())
                    .thenReturn(0).when(stub.fooInteger()).thenReturn(100);
        }
    };

    private static final StubConfigurer<Bar> BAR_CONFIGURER = new StubConfigurer<Bar>() {

        @Override
        protected void configure(Bar stub) {
            when(stub.barString()).thenReturn("bar").when(stub.barLong())
                    .thenReturn(0L).when(stub.barLongObject()).thenReturn(100L);
        }
    };

    private static final StubConfigurer<CollideFoo> COLLIDE_FOO_CONFIGURER = new StubConfigurer<CollideFoo>() {

        @Override
        protected void configure(CollideFoo stub) {
            when(stub.fooString()).thenReturn("collideFoo");
        }
    };

    private static final StubConfigurer<ArrayValues> ARRAY_VALUES_CONFIGURER = new StubConfigurer<ArrayValues>() {

        @Override
        protected void configure(ArrayValues stub) {
            when(stub.booleanArray()).thenReturn(true, false)
            .when(stub.byteArray()).thenReturn((byte) 0, (byte) 1, (byte) 2)
            .when(stub.shortArray()).thenReturn(new short[] { 3, 4, 5 })
            .when(stub.intArray()).thenReturn(6, 7, 8)
            .when(stub.charArray()).thenReturn('a', 'b', 'c')
            .when(stub.longArray()).thenReturn(9L, 10L, 11L)
            .when(stub.floatArray()).thenReturn(0.0f, 0.1f, 0.2f)
            .when(stub.doubleArray()).thenReturn(0.0, 0.1, 0.2)
            .when(stub.stringArray()).thenReturn("foo", "bar", "baz")
            .when(stub.objectArray()).thenReturn("foo", 1, null);
        }
    };

    private static final StubConfigurer<CurrentTime> CURRENT_TIME_CONFIGURER = new StubConfigurer<CurrentTime>() {

        @Override
        protected void configure(CurrentTime stub) {
            when(stub.currentTimeMillis()).thenAnswer(
                    new ObjectProvider<Long>() {

                        public Long getObject() {
                            return System.currentTimeMillis();
                        }
                    });
        }

    };

    private static final StubConfigurer<AcceptArguments> ACCEPT_ARGUMENTS_CONFIGURER = new StubConfigurer<AcceptArguments>() {

        @Override
        protected void configure(AcceptArguments stub) {
            when(stub.respondTo("foo")).thenReturn("who")
            .when(stub.respondTo("bar")).thenReturn("far")
            .when(stub.respondTo("baz")).thenReturn("spazz");
        }
    };

    private StubProxyFactory proxyFactory;

    @Before
    public void setUp() {
        proxyFactory = new StubProxyFactory(createParent(), FOO_CONFIGURER,
                BAR_CONFIGURER, COLLIDE_FOO_CONFIGURER,
                ARRAY_VALUES_CONFIGURER, CURRENT_TIME_CONFIGURER, ACCEPT_ARGUMENTS_CONFIGURER);
    }

    protected abstract ProxyFactory createParent();

    @Test
    public void testBasic() {
        Foo foo = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE, Foo.class);
        assertEquals("foo", foo.fooString());
        assertEquals(0, foo.fooInt());
        assertEquals(100, foo.fooInteger().intValue());
        Bar bar = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE, Bar.class);
        assertEquals("bar", bar.barString());
        assertEquals(0L, bar.barLong());
        assertEquals(100L, bar.barLongObject().longValue());
    }

    @Test
    public void testCombined() {
        Foo foobar = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE, Foo.class, Bar.class);
        assertEquals("foo", foobar.fooString());
        assertEquals(0, foobar.fooInt());
        assertEquals(100, foobar.fooInteger().intValue());
        assertEquals("bar", ((Bar) foobar).barString());
        assertEquals(0L, ((Bar) foobar).barLong());
        assertEquals(100L, ((Bar) foobar).barLongObject().longValue());
    }

    @Test
    public void testCollision() {
        Foo foo = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE, Foo.class, CollideFoo.class);
        assertEquals(0, foo.fooInt());
        assertEquals(100, foo.fooInteger().intValue());
        assertEquals("collideFoo", foo.fooString());
    }

    @Test
    public void testArrays() {
        ArrayValues arrayValues = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE, ArrayValues.class);
        assertBooleanArrayEquals(new boolean[] { true, false }, arrayValues.booleanArray());
        assertArrayEquals(new byte[] { 0, 1, 2 }, arrayValues.byteArray());
        assertArrayEquals(new short[] { 3, 4, 5 }, arrayValues.shortArray());
        assertArrayEquals(new int[] { 6, 7, 8 }, arrayValues.intArray());
        assertArrayEquals(new char[] { 'a', 'b', 'c' }, arrayValues.charArray());
        assertArrayEquals(new long[] { 9L, 10L, 11L }, arrayValues.longArray());
        assertArrayEquals(new float[] { 0.0f, 0.1f, 0.2f }, arrayValues.floatArray(), 0.0f);
        assertArrayEquals(new double[] { 0.0, 0.1, 0.2 }, arrayValues.doubleArray(), 0.0);
        assertArrayEquals(new String[] { "foo", "bar", "baz" }, arrayValues.stringArray());
        assertArrayEquals(new Object[] { "foo", 1, null }, arrayValues.objectArray());
    }

    private void assertBooleanArrayEquals(boolean[] expected, boolean[] actual) {
        if (actual == expected) {
            return;
        }
        if (expected != null && actual != null) {
            if (actual.length == expected.length) {
                for (int i = 0; i < actual.length; i++) {
                    assertTrue(actual[i] == expected[i]);
                }
            }
            return;
        }
        fail();
    }

    @Test
    public void testDeferredResult() {
        CurrentTime currentTime = proxyFactory.createInvokerProxy(
                NullInvoker.INSTANCE, CurrentTime.class);
        assertTrue(System.currentTimeMillis() <= currentTime
                .currentTimeMillis());
    }

    @SuppressWarnings("serial")
    @Test(expected=UnsupportedOperationException.class)
    public void testUnhandled() {
        Baz baz = proxyFactory.createInvokerProxy(new Invoker() {

            public Object invoke(Object proxy, Method method, Object[] arguments)
                    throws Throwable {
                throw new UnsupportedOperationException();
            }
        }, Baz.class);
        baz.dontTouchMe();
    }

    @Test
    public void testAcceptArguments() {
        AcceptArguments acceptArguments = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE, AcceptArguments.class);
        assertEquals("who", acceptArguments.respondTo("foo"));
        assertEquals("far", acceptArguments.respondTo("bar"));
        assertEquals("spazz", acceptArguments.respondTo("baz"));
    }

    @Test
    public void testDelegator() {
        ProxyFactory partialConfiguration = new StubProxyFactory(createParent(), new StubConfigurer<Foo>() {
           /**
             * {@inheritDoc}
             */
            @Override
            protected void configure(Foo stub) {
                when(stub.fooInt()).thenReturn(0).when(stub.fooInteger()).thenReturn(100);
            }
        });
        Foo foo = partialConfiguration.createDelegatorProxy(new BeanProvider<Foo>(FooImpl.class), Foo.class);
        assertEquals(0, foo.fooInt());
        assertEquals(100, foo.fooInteger().intValue());
        assertEquals("FooImpl", foo.fooString());
    }

    @Test
    public void testInterceptor() {
        ProxyFactory partialConfiguration = new StubProxyFactory(createParent(), new StubConfigurer<Foo>() {
            /**
              * {@inheritDoc}
              */
             @Override
             protected void configure(Foo stub) {
                 when(stub.fooInt()).thenReturn(0);
             }
         });

        @SuppressWarnings("serial")
        final Interceptor interceptor = new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                if (invocation.getMethod().getName().equals("fooInteger")) {
                    return 100;
                }
                return invocation.proceed();
            }
        };

        Foo foo = partialConfiguration.createInterceptorProxy(new FooImpl(),
                interceptor, Foo.class);

        assertEquals(0, foo.fooInt());
        assertEquals(100, foo.fooInteger().intValue());
        assertEquals("FooImpl", foo.fooString());
    }

    @Test
    public void testSubclassing() {
        assumeTrue(proxyFactory.canProxy(FooImpl.class));
        FooImpl foo = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE, FooImpl.class);
        assertEquals("foo", foo.fooString());
        assertEquals(0, foo.fooInt());
        assertEquals(100, foo.fooInteger().intValue());
    }

    @Test
    public void testGenericStub() {
        ProxyFactory iterableStubFactory = new StubProxyFactory(createParent(), new StubConfigurer<Iterable<String>>() {

            @Override
            protected void configure(Iterable<String> stub) {
                when(stub.iterator()).thenReturn(Arrays.asList("foo", "bar", "baz").iterator());
            }

        });
        Iterable<String> strings = iterableStubFactory.createInvokerProxy(NullInvoker.INSTANCE, Iterable.class);
        assertIterator(strings.iterator(), "foo", "bar", "baz");
    }

    @Test
    public void testDeferredGenericResult() {
        final ObjectProvider<Iterator<String>> provider =
            new ConstantProvider<Iterator<String>>(Arrays.asList("foo", "bar", "baz").iterator());
        ProxyFactory iterableStubFactory = new StubProxyFactory(createParent(), new StubConfigurer<Iterable<String>>() {

            @Override
            protected void configure(Iterable<String> stub) {
                when(stub.iterator()).thenAnswer(provider);
            }

        });
        Iterable<String> strings = iterableStubFactory.createInvokerProxy(NullInvoker.INSTANCE, Iterable.class);
        assertIterator(strings.iterator(), "foo", "bar", "baz");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadReturnValue() {
        StubProxyFactory factory = new StubProxyFactory(createParent(), new StubConfigurer<AcceptArguments>() {

            @Override
            protected void configure(AcceptArguments stub) {
                when((Object) stub.respondTo("x")).thenReturn(100);
            }

        });

        factory.createInvokerProxy(NullInvoker.INSTANCE, AcceptArguments.class);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadPrimitiveReturnValue() {
        StubProxyFactory factory = new StubProxyFactory(createParent(), new StubConfigurer<Foo>() {
            
            @Override
            protected void configure(Foo stub) {
                when(stub.fooInt()).thenReturn(null);
            }

        });

        factory.createInvokerProxy(NullInvoker.INSTANCE, Foo.class);
    }

    private <T> void assertIterator(Iterator<T> iter, T... expected) {
        for (T t : expected) {
            assertTrue(iter.hasNext());
            assertEquals(t, iter.next());
        }
        assertFalse(iter.hasNext());
    }

    public interface Foo {
        String fooString();

        int fooInt();

        Integer fooInteger();
    }

    public static class FooImpl implements Foo {

        /**
         * {@inheritDoc}
         */
        @Override
        public int fooInt() {
            return 666;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer fooInteger() {
            return 667;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String fooString() {
            return "FooImpl";
        }

    }

    public interface Bar {
        String barString();

        long barLong();

        Long barLongObject();
    }

    public interface Baz {
        Object dontTouchMe();
    }

    public interface CollideFoo {
        String fooString();
    }

    public interface ArrayValues {
        boolean[] booleanArray();

        byte[] byteArray();

        short[] shortArray();

        int[] intArray();

        char[] charArray();

        long[] longArray();

        float[] floatArray();

        double[] doubleArray();

        String[] stringArray();

        Object[] objectArray();
    }

    public interface CurrentTime {
        long currentTimeMillis();
    }

    public interface AcceptArguments {
        String respondTo(String s);
    }
}
