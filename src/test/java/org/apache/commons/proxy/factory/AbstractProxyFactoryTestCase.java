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

package org.apache.commons.proxy.factory;

import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invocation;
import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.provider.BeanProvider;
import org.apache.commons.proxy.provider.ConstantProvider;
import org.apache.commons.proxy.provider.SingletonProvider;
import org.apache.commons.proxy.util.AbstractTestCase;
import org.apache.commons.proxy.util.DuplicateEcho;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;
import org.apache.commons.proxy.util.SuffixInterceptor;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author James Carman
 * @since 1.0
 */
public abstract class AbstractProxyFactoryTestCase extends AbstractTestCase
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private static final Class[] ECHO_ONLY = new Class[]{Echo.class};
    protected final ProxyFactory factory;
    private static final Class[] COMPARABLE_ONLY = new Class[] { Comparable.class };

//**********************************************************************************************************************
// Constructors
//**********************************************************************************************************************

    protected AbstractProxyFactoryTestCase(ProxyFactory factory)
    {
        this.factory = factory;
    }

//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    private ObjectProvider createSingletonEcho()
    {
        return new SingletonProvider(new BeanProvider(EchoImpl.class));
    }

    public void testInterceptorHashCode()
    {
        final Echo proxy = (Echo) factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        assertEquals(proxy.hashCode(), System.identityHashCode(proxy));
    }

    public void testInvokerHashCode() throws Exception
    {
        final Echo proxy = (Echo) factory.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        assertEquals(proxy.hashCode(), System.identityHashCode(proxy));
    }

    public void testDelegatorHashCode() throws Exception
    {
        final Echo proxy = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        assertEquals(proxy.hashCode(), System.identityHashCode(proxy));
    }


    public void testInterceptorEquals()
    {
        final Date date = new Date();
        final Comparable proxy1 = (Comparable) factory.createInterceptorProxy(date,
                new NoOpMethodInterceptor(), COMPARABLE_ONLY);
        final Comparable proxy2 = (Comparable) factory.createInterceptorProxy(date,
                new NoOpMethodInterceptor(), COMPARABLE_ONLY);
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    public void testInvokerEquals() throws Exception
    {
        final Comparable proxy1 = (Comparable) factory.createInvokerProxy(new InvokerTester(), COMPARABLE_ONLY);
        final Comparable proxy2 = (Comparable) factory.createInvokerProxy(new InvokerTester(), COMPARABLE_ONLY);
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    public void testDelegatorEquals() throws Exception
    {
        final Date date = new Date();
        final Comparable proxy1 = (Comparable) factory.createDelegatorProxy(new ConstantProvider<Date>(date),
                COMPARABLE_ONLY);
        final Comparable proxy2 = (Comparable) factory.createDelegatorProxy(new ConstantProvider<Date>(date),
                COMPARABLE_ONLY);
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    public void testBooleanInterceptorParameter()
    {
        final Echo echo = (Echo) factory.createInterceptorProxy(new EchoImpl(), new InterceptorTester(), ECHO_ONLY);
        assertFalse(echo.echoBack(false));
        assertTrue(echo.echoBack(true));
    }

    public void testCanProxy()
    {
        assertTrue(factory.canProxy(Echo.class));
        assertFalse(factory.canProxy(EchoImpl.class));
    }

    public void testChangingArguments()
    {
        final Echo proxy = (Echo) factory.createInterceptorProxy(new EchoImpl(), new ChangeArgumentInterceptor(), ECHO_ONLY);
        assertEquals("something different", proxy.echoBack("whatever"));
    }

    public void testCreateDelegatingProxy()
    {
        final Echo echo = (Echo) factory.createDelegatorProxy(createSingletonEcho(), ECHO_ONLY);
        echo.echo();
        assertEquals("message", echo.echoBack("message"));
        assertEquals("ab", echo.echoBack("a", "b"));
    }

    public void testCreateInterceptorProxy()
    {
        final Echo target = (Echo) factory.createDelegatorProxy(createSingletonEcho(), ECHO_ONLY);
        final Echo proxy = (Echo) factory.createInterceptorProxy(target, new SuffixInterceptor(" suffix"), ECHO_ONLY);
        proxy.echo();
        assertEquals("message suffix", proxy.echoBack("message"));
    }

    public void testDelegatingProxyClassCaching() throws Exception
    {
        final Echo proxy1 = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        final Echo proxy2 = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        assertNotSame(proxy1, proxy2);
        assertSame(proxy1.getClass(), proxy2.getClass());
    }

    public void testDelegatingProxyInterfaceOrder()
    {
        final Echo echo = (Echo) factory.createDelegatorProxy(createSingletonEcho(), Echo.class, DuplicateEcho.class);
        final List<Class> expected = new LinkedList<Class>(Arrays.asList(Echo.class, DuplicateEcho.class));
        final List<Class> actual = new LinkedList<Class>(Arrays.asList(echo.getClass().getInterfaces()));
        actual.retainAll(expected);  // Doesn't alter order!
        assertEquals(expected, actual);
    }

    public void testDelegatingProxySerializable() throws Exception
    {
        final Echo proxy = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        assertSerializable(proxy);
    }

    public void testInterceptingProxyClassCaching() throws Exception
    {
        final Echo proxy1 = (Echo) factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        final Echo proxy2 = (Echo) factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        assertNotSame(proxy1, proxy2);
        assertSame(proxy1.getClass(), proxy2.getClass());
    }

    public void testInterceptingProxySerializable() throws Exception
    {
        final Echo proxy = (Echo) factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        assertSerializable(proxy);
    }

    public void testInterceptorProxyWithCheckedException() throws Exception
    {
        final Echo proxy = (Echo) factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        try
        {
            proxy.ioException();
            fail();
        }
        catch (IOException e)
        {
        }
    }

    public void testInterceptorProxyWithUncheckedException() throws Exception
    {
        final Echo proxy = (Echo) factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY);
        try
        {
            proxy.illegalArgument();
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    @SuppressWarnings("unchecked")
    public void testInterfaceHierarchies()
    {
        final SortedSet set = factory.createDelegatorProxy(new ConstantProvider<SortedSet>(new TreeSet()), SortedSet.class);
        set.add("Hello");
    }

    public void testInvokerProxy() throws Exception
    {
        final InvokerTester tester = new InvokerTester();
        final Echo echo = (Echo) factory.createInvokerProxy(tester, ECHO_ONLY);
        echo.echoBack("hello");
        assertEquals(Echo.class.getMethod("echoBack", String.class), tester.method);
        assertSame(echo, tester.proxy);
        assertNotNull(tester.args);
        assertEquals(1, tester.args.length);
        assertEquals("hello", tester.args[0]);
    }

    public void testInvokerProxyClassCaching() throws Exception
    {
        final Echo proxy1 = (Echo) factory.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        final Echo proxy2 = (Echo) factory.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        assertNotSame(proxy1, proxy2);
        assertSame(proxy1.getClass(), proxy2.getClass());
    }

    public void testInvokerProxySerializable() throws Exception
    {
        final Echo proxy = (Echo) factory.createInvokerProxy(new InvokerTester(), ECHO_ONLY);
        assertSerializable(proxy);
    }

    public void testMethodInvocationClassCaching() throws Exception
    {
        final InterceptorTester tester = new InterceptorTester();
        final EchoImpl target = new EchoImpl();
        final Echo proxy1 = (Echo) factory.createInterceptorProxy(target, tester, ECHO_ONLY);
        final Echo proxy2 = (Echo) factory.createInterceptorProxy(target, tester, Echo.class, DuplicateEcho.class);
        proxy1.echoBack("hello1");
        final Class invocationClass1 = tester.invocationClass;
        proxy2.echoBack("hello2");
        assertSame(invocationClass1, tester.invocationClass);
    }

    public void testMethodInvocationDuplicateMethods() throws Exception
    {
        final InterceptorTester tester = new InterceptorTester();
        final EchoImpl target = new EchoImpl();
        final Echo proxy = (Echo) factory.createInterceptorProxy(target, tester, Echo.class, DuplicateEcho.class);
        proxy.echoBack("hello");
        assertEquals(Echo.class.getMethod("echoBack", String.class), tester.method);
    }

    public void testMethodInvocationImplementation() throws Exception
    {
        final InterceptorTester tester = new InterceptorTester();
        final EchoImpl target = new EchoImpl();
        final Echo proxy = (Echo) factory.createInterceptorProxy(target, tester, ECHO_ONLY);
        proxy.echo();
        assertNotNull(tester.arguments);
        assertEquals(0, tester.arguments.length);
        assertEquals(Echo.class.getMethod("echo"), tester.method);
        assertEquals(target, tester.proxy);
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

    public void testPrimitiveParameter()
    {
        final Echo echo = (Echo) factory.createDelegatorProxy(createSingletonEcho(), ECHO_ONLY);
        assertEquals(1, echo.echoBack(1));
    }

    public void testProxyWithCheckedException() throws Exception
    {
        final Echo proxy = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        try
        {
            proxy.ioException();
            fail();
        }
        catch (IOException e)
        {
        }
    }

    public void testProxyWithUncheckedException() throws Exception
    {
        final Echo proxy = factory.createDelegatorProxy(new ConstantProvider<Echo>(new EchoImpl()), Echo.class);
        try
        {
            proxy.illegalArgument();
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    public void testWithNonAccessibleTargetType()
    {
        final Echo proxy = (Echo) factory.createInterceptorProxy(new PrivateEcho(), new NoOpMethodInterceptor(), ECHO_ONLY);
        proxy.echo();
    }

//**********************************************************************************************************************
// Inner Classes
//**********************************************************************************************************************

    private static class ChangeArgumentInterceptor implements Interceptor
    {
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
        private Class invocationClass;

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
        public Object intercept(Invocation methodInvocation) throws Throwable
        {
            return methodInvocation.proceed();
        }
    }

    private static class PrivateEcho extends EchoImpl
    {
    }
}
