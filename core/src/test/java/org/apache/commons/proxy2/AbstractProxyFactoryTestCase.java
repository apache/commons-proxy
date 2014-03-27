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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.proxy2.provider.BeanProvider;
import org.apache.commons.proxy2.provider.ConstantProvider;
import org.apache.commons.proxy2.provider.SingletonProvider;
import org.apache.commons.proxy2.util.AbstractTestCase;
import org.apache.commons.proxy2.util.DuplicateEcho;
import org.apache.commons.proxy2.util.Echo;
import org.apache.commons.proxy2.util.EchoImpl;
import org.apache.commons.proxy2.util.SuffixInterceptor;
import org.junit.Test;

/**
 * @author James Carman
 * @since 1.0
 */
@SuppressWarnings("serial")
public abstract class AbstractProxyFactoryTestCase extends AbstractTestCase
{
    //**********************************************************************************************************************
    // Fields
    //**********************************************************************************************************************

    private static final Class<?>[] ECHO_ONLY = new Class[] { Echo.class };
    protected final ProxyFactory factory;
    private static final Class<?>[] COMPARABLE_ONLY = new Class[] { Comparable.class };

    //**********************************************************************************************************************
    // Constructors
    //**********************************************************************************************************************

    protected AbstractProxyFactoryTestCase()
    {
        final ServiceLoader<ProxyFactory> serviceLoader = ServiceLoader.load(ProxyFactory.class);
        Iterator<ProxyFactory> iter = serviceLoader.iterator();
        if (iter.hasNext())
        {
            this.factory = iter.next();
        }
        else
        {
            throw new RuntimeException("Unable to find proxy factory implementation.");
        }

    }

    //**********************************************************************************************************************
    // Other Methods
    //**********************************************************************************************************************

    private ObjectProvider<Echo> createSingletonEcho()
    {
        return new SingletonProvider<Echo>(new BeanProvider<Echo>(EchoImpl.class));
    }

    @Test
    public void testInterceptorHashCode()
    {
        final Echo proxy = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        assertEquals(proxy.hashCode(), System.identityHashCode(proxy));
    }

    @Test
    public void testInvokerHashCode() throws Exception
    {
        final Echo proxy = factory.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        assertEquals(proxy.hashCode(), System.identityHashCode(proxy));
    }

    @Test
    public void testDelegatorHashCode() throws Exception
    {
        final Echo proxy = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        assertEquals(proxy.hashCode(), System.identityHashCode(proxy));
    }

    @Test
    public void testInterceptorEquals()
    {
        final Date date = new Date();
        final Comparable<?> proxy1 = factory.createInterceptorProxy(date, new NoOpMethodInterceptor(), COMPARABLE_ONLY);
        final Comparable<?> proxy2 = factory.createInterceptorProxy(date, new NoOpMethodInterceptor(), COMPARABLE_ONLY);
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    @Test
    public void testInvokerEquals() throws Exception
    {
        final Comparable<?> proxy1 = factory.createInvokerProxy(new InvokerTester(), COMPARABLE_ONLY);
        final Comparable<?> proxy2 = factory.createInvokerProxy(new InvokerTester(), COMPARABLE_ONLY);
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    @Test
    public void testDelegatorEquals() throws Exception
    {
        final Date date = new Date();
        final Comparable<?> proxy1 = factory.createDelegatorProxy(new ConstantProvider<Date>(date), COMPARABLE_ONLY);
        final Comparable<?> proxy2 = factory.createDelegatorProxy(new ConstantProvider<Date>(date), COMPARABLE_ONLY);
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    @Test
    public void testBooleanInterceptorParameter()
    {
        final Echo echo = factory.createInterceptorProxy(new EchoImpl(), new InterceptorTester(), ECHO_ONLY);
        assertFalse(echo.echoBack(false));
        assertTrue(echo.echoBack(true));
    }

    @Test
    public void testCanProxy()
    {
        assertTrue(factory.canProxy(Echo.class));
        assertFalse(factory.canProxy(EchoImpl.class));
    }

    @Test
    public void testChangingArguments()
    {
        final Echo proxy = factory.createInterceptorProxy(new EchoImpl(), new ChangeArgumentInterceptor(), ECHO_ONLY);
        assertEquals("something different", proxy.echoBack("whatever"));
    }

    @Test
    public void testCreateDelegatingProxy()
    {
        final Echo echo = factory.createDelegatorProxy(createSingletonEcho(), ECHO_ONLY);
        echo.echo();
        assertEquals("message", echo.echoBack("message"));
        assertEquals("ab", echo.echoBack("a", "b"));
    }

    @Test
    public void testCreateInterceptorProxy()
    {
        final Echo target = factory.createDelegatorProxy(createSingletonEcho(), ECHO_ONLY);
        final Echo proxy = factory.createInterceptorProxy(target, new SuffixInterceptor(" suffix"), ECHO_ONLY);
        proxy.echo();
        assertEquals("message suffix", proxy.echoBack("message"));
    }

    @Test
    public void testDelegatingProxyClassCaching() throws Exception
    {
        final Echo proxy1 = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        final Echo proxy2 = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        assertNotSame(proxy1, proxy2);
        assertSame(proxy1.getClass(), proxy2.getClass());
    }

    @Test
    public void testDelegatingProxyInterfaceOrder()
    {
        final Echo echo = factory.createDelegatorProxy(createSingletonEcho(), Echo.class, DuplicateEcho.class);
        final List<Class<?>> expected = new LinkedList<Class<?>>(Arrays.<Class<?>> asList(Echo.class,
                DuplicateEcho.class));
        final List<Class<?>> actual = new LinkedList<Class<?>>(Arrays.asList(echo.getClass().getInterfaces()));
        actual.retainAll(expected); // Doesn't alter order!
        assertEquals(expected, actual);
    }

    @Test
    public void testDelegatingProxySerializable() throws Exception
    {
        final Echo proxy = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        assertSerializable(proxy);
    }

    @Test
    public void testInterceptingProxyClassCaching() throws Exception
    {
        final Echo proxy1 = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        final Echo proxy2 = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        assertNotSame(proxy1, proxy2);
        assertSame(proxy1.getClass(), proxy2.getClass());
    }

    @Test
    public void testInterceptingProxySerializable() throws Exception
    {
        final Echo proxy = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        assertSerializable(proxy);
    }

    @Test(expected = IOException.class)
    public void testInterceptorProxyWithCheckedException() throws Exception
    {
        final Echo proxy = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        proxy.ioException();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInterceptorProxyWithUncheckedException() throws Exception
    {
        final Echo proxy = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        proxy.illegalArgument();
    }

    @Test
    public void testInterfaceHierarchies()
    {
        final SortedSet<String> set = factory.createDelegatorProxy(new ConstantProvider<SortedSet<String>>(
                new TreeSet<String>()), SortedSet.class);
        set.add("Hello");
    }

    @Test
    public void testInvokerProxy() throws Exception
    {
        final InvokerTester tester = new InvokerTester();
        final Echo echo = factory.createInvokerProxy(tester, ECHO_ONLY);
        echo.echoBack("hello");
        assertEquals(Echo.class.getMethod("echoBack", String.class), tester.method);
        assertSame(echo, tester.proxy);
        assertNotNull(tester.args);
        assertEquals(1, tester.args.length);
        assertEquals("hello", tester.args[0]);
    }

    @Test
    public void testInvokerProxyClassCaching() throws Exception
    {
        final Echo proxy1 = factory.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        final Echo proxy2 = factory.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        assertNotSame(proxy1, proxy2);
        assertSame(proxy1.getClass(), proxy2.getClass());
    }

    @Test
    public void testInvokerProxySerializable() throws Exception
    {
        final Echo proxy = factory.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        assertSerializable(proxy);
    }

    @Test
    public void testMethodInvocationClassCaching() throws Exception
    {
        final InterceptorTester tester = new InterceptorTester();
        final EchoImpl target = new EchoImpl();
        final Echo proxy1 = factory.createInterceptorProxy(target, tester, ECHO_ONLY);
        final Echo proxy2 = factory.createInterceptorProxy(target, tester, Echo.class, DuplicateEcho.class);
        proxy1.echoBack("hello1");
        final Class<?> invocationClass1 = tester.invocationClass;
        proxy2.echoBack("hello2");
        assertSame(invocationClass1, tester.invocationClass);
    }

    @Test
    public void testMethodInvocationDuplicateMethods() throws Exception
    {
        final InterceptorTester tester = new InterceptorTester();
        final EchoImpl target = new EchoImpl();
        final Echo proxy = factory.createInterceptorProxy(target, tester, Echo.class, DuplicateEcho.class);
        proxy.echoBack("hello");
        assertEquals(Echo.class.getMethod("echoBack", String.class), tester.method);
    }

    @Test
    public void testMethodInvocationImplementation() throws Exception
    {
        final InterceptorTester tester = new InterceptorTester();
        final EchoImpl target = new EchoImpl();
        final Echo proxy = factory.createInterceptorProxy(target, tester, ECHO_ONLY);
        proxy.echo();
        assertNotNull(tester.arguments);
        assertEquals(0, tester.arguments.length);
        assertEquals(Echo.class.getMethod("echo"), tester.method);
        assertSame(proxy, tester.proxy);
        proxy.echoBack("Hello");
        assertNotNull(tester.arguments);
        assertEquals(1, tester.arguments.length);
        assertEquals("Hello", tester.arguments[0]);
        assertEquals(Echo.class.getMethod("echoBack", String.class), tester.method);
        proxy.echoBack("Hello", "World");
        assertNotNull(tester.arguments);
        assertEquals(2, tester.arguments.length);
        assertEquals("Hello", tester.arguments[0]);
        assertEquals("World", tester.arguments[1]);
        assertEquals(Echo.class.getMethod("echoBack", String.class, String.class), tester.method);
    }

    @Test
    public void testPrimitiveParameter()
    {
        final Echo echo = factory.createDelegatorProxy(createSingletonEcho(), ECHO_ONLY);
        assertEquals(1, echo.echoBack(1));
    }

    @Test(expected = IOException.class)
    public void testProxyWithCheckedException() throws Exception
    {
        final Echo proxy = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        proxy.ioException();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProxyWithUncheckedException() throws Exception
    {
        final Echo proxy = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        proxy.illegalArgument();
    }

    @Test
    public void testWithNonAccessibleTargetType()
    {
        final Echo proxy = factory.createInterceptorProxy(new PrivateEcho(), new NoOpMethodInterceptor(), ECHO_ONLY);
        proxy.echo();
    }

    //**********************************************************************************************************************
    // Inner Classes
    //**********************************************************************************************************************

    private static class ChangeArgumentInterceptor implements Interceptor
    {
        @Override
        public Object intercept(Invocation methodInvocation) throws Throwable
        {
            methodInvocation.getArguments()[0] = "something different";
            return methodInvocation.proceed();
        }
    }

    protected static class InterceptorTester implements Interceptor
    {
        private Object[] arguments;
        private Method method;
        private Object proxy;
        private Class<?> invocationClass;

        @Override
        public Object intercept(Invocation methodInvocation) throws Throwable
        {
            arguments = methodInvocation.getArguments();
            method = methodInvocation.getMethod();
            proxy = methodInvocation.getProxy();
            invocationClass = methodInvocation.getClass();
            return methodInvocation.proceed();
        }
    }

    protected static class InvokerTester implements Invoker
    {
        private Object method;
        private Object[] args;
        private Object proxy;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            this.proxy = proxy;
            this.method = method;
            this.args = args;
            return null;
        }
    }

    protected static class NoOpMethodInterceptor implements Interceptor, Serializable
    {
        @Override
        public Object intercept(Invocation methodInvocation) throws Throwable
        {
            return methodInvocation.proceed();
        }
    }

    private static class PrivateEcho extends EchoImpl
    {
    }
}
