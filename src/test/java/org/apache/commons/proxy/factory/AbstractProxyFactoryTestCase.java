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

import org.apache.commons.proxy.Invocation;
import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.provider.ProviderUtils;
import org.apache.commons.proxy.util.AbstractTestCase;
import org.apache.commons.proxy.util.DuplicateEcho;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;
import org.apache.commons.proxy.util.SuffixInterceptor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
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
    protected final ProxyFactory factory;
    private static final Class[] ECHO_ONLY = new Class[] { Echo.class };

    protected AbstractProxyFactoryTestCase( ProxyFactory factory )
    {
        this.factory = factory;
    }

    public void testCanProxy()
    {
        assertTrue( factory.canProxy( ECHO_ONLY ) );
        assertFalse( factory.canProxy( new Class[] { EchoImpl.class } ) );
    }

    public void testInterfaceHierarchies()
    {
        final SortedSet set = ( SortedSet ) factory.createDelegatorProxy( ProviderUtils.constantProvider( new TreeSet() ), new Class[] { SortedSet.class } );
        set.add( "Hello" );
    }

    public void testInvokerProxy() throws Exception
    {
        final InvokerTester tester = new InvokerTester();
        final Echo echo = ( Echo )factory.createInvokerProxy( tester, ECHO_ONLY );
        echo.echoBack( "hello" );
        assertEquals( Echo.class.getMethod( "echoBack", new Class[] { String.class } ), tester.method );
        assertSame( echo, tester.proxy );
        assertNotNull( tester.args );
        assertEquals( 1, tester.args.length );
        assertEquals( "hello", tester.args[0] );
    }

    public void testDelegatingProxyInterfaceOrder()
    {
        final Echo echo = ( Echo ) factory.createDelegatorProxy( createSingletonEcho(), new Class[] { Echo.class, DuplicateEcho.class } );
        final List expected = new LinkedList( Arrays.asList( new Class[] { Echo.class, DuplicateEcho.class } ) );
        final List actual = new LinkedList( Arrays.asList( echo.getClass().getInterfaces() ) );
        actual.retainAll( expected );  // Doesn't alter order!
        assertEquals( expected, actual );
    }

    public void testCreateDelegatingProxy()
    {
        final Echo echo = ( Echo ) factory.createDelegatorProxy( createSingletonEcho(), ECHO_ONLY );
        echo.echo();
        assertEquals( "message", echo.echoBack( "message" ) );
        assertEquals( "ab", echo.echoBack( "a", "b" ) );
    }

    public void testBooleanInterceptorParameter()
    {
        final Echo echo = ( Echo ) factory.createInterceptorProxy( new EchoImpl(), new  InterceptorTester(), ECHO_ONLY );
        assertFalse( echo.echoBack( false ) );
        assertTrue( echo.echoBack( true ) );

    }
    public void testPrimitiveParameter()
    {
        final Echo echo = ( Echo ) factory.createDelegatorProxy( createSingletonEcho(), ECHO_ONLY );
        assertEquals( 1, echo.echoBack( 1 ) );
    }

    public void testCreateInterceptorProxy()
    {
        final Echo target = ( Echo ) factory.createDelegatorProxy( createSingletonEcho(), ECHO_ONLY );
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( target, new SuffixInterceptor( " suffix" ), ECHO_ONLY );
        proxy.echo();
        assertEquals( "message suffix", proxy.echoBack( "message" ) );
    }

    private ObjectProvider createSingletonEcho()
    {
        return ProviderUtils.singletonProvider( ProviderUtils.beanProvider( EchoImpl.class ) );
    }

    public void testMethodInvocationImplementation() throws Exception
    {
        final InterceptorTester tester = new InterceptorTester();
        final EchoImpl target = new EchoImpl();
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( target, tester, ECHO_ONLY );
        proxy.echo();
        assertNotNull( tester.arguments );
        assertEquals( 0, tester.arguments.length );
        assertEquals( Echo.class.getMethod( "echo", new Class[] {} ), tester.method );
        assertEquals( target, tester.proxy );
        proxy.echoBack( "Hello" );
        assertNotNull( tester.arguments );
        assertEquals( 1, tester.arguments.length );
        assertEquals( "Hello", tester.arguments[0] );
        assertEquals( Echo.class.getMethod( "echoBack", new Class[] { String.class } ), tester.method );
        proxy.echoBack( "Hello", "World" );
        assertNotNull( tester.arguments );
        assertEquals( 2, tester.arguments.length );
        assertEquals( "Hello", tester.arguments[0] );
        assertEquals( "World", tester.arguments[1] );
        assertEquals( Echo.class.getMethod( "echoBack", new Class[] { String.class, String.class } ), tester.method );
    }

    public void testMethodInvocationDuplicateMethods() throws Exception
    {
        final InterceptorTester tester = new InterceptorTester();
        final EchoImpl target = new EchoImpl();
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( target, tester, new Class[] { Echo.class, DuplicateEcho.class } );
        proxy.echoBack( "hello" );
        assertEquals( Echo.class.getMethod( "echoBack", new Class[] { String.class } ), tester.method );
    }


    public void testMethodInvocationClassCaching() throws Exception
    {
        final InterceptorTester tester = new InterceptorTester();
        final EchoImpl target = new EchoImpl();
        final Echo proxy1 = ( Echo ) factory.createInterceptorProxy( target, tester, ECHO_ONLY );
        final Echo proxy2 = ( Echo ) factory.createInterceptorProxy( target, tester, new Class[] { Echo.class, DuplicateEcho.class } );
        proxy1.echoBack( "hello1" );
        final Class invocationClass1 = tester.invocationClass;
        proxy2.echoBack( "hello2" );
        assertEquals( invocationClass1, tester.invocationClass );
    }

    public void testDelegatingProxyClassCaching() throws Exception
    {
        final Echo proxy1 = ( Echo ) factory.createDelegatorProxy( ProviderUtils.constantProvider( new EchoImpl() ), ECHO_ONLY );
        final Echo proxy2 = ( Echo ) factory.createDelegatorProxy( ProviderUtils.constantProvider( new EchoImpl() ), ECHO_ONLY );
        assertEquals( proxy1.getClass(), proxy2.getClass() );
    }

    public void testInterceptingProxyClassCaching() throws Exception
    {
        final Echo proxy1 = ( Echo ) factory.createInterceptorProxy( new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY );
        final Echo proxy2 = ( Echo ) factory.createInterceptorProxy( new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY );
        assertEquals( proxy1.getClass(), proxy2.getClass() );
    }

    public void testProxyWithCheckedException() throws Exception
    {
        final Echo proxy = ( Echo ) factory.createDelegatorProxy( ProviderUtils.constantProvider( new EchoImpl() ), ECHO_ONLY );
        try
        {
            proxy.ioException();
            fail();
        }
        catch( IOException e )
        {
        }
    }

    public void testProxyWithUncheckedException() throws Exception
    {
        final Echo proxy = ( Echo ) factory.createDelegatorProxy( ProviderUtils.constantProvider( new EchoImpl() ), ECHO_ONLY );
        try
        {
            proxy.illegalArgument();
            fail();
        }
        catch( IllegalArgumentException e )
        {
        }
    }

    public void testInterceptorProxyWithUncheckedException() throws Exception
    {
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( new EchoImpl(), new NoOpMethodInterceptor(),  ECHO_ONLY );
        try
        {
            proxy.illegalArgument();
            fail();
        }
        catch( IllegalArgumentException e )
        {
        }
    }

    public void testInterceptorProxyWithCheckedException() throws Exception
    {
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( new EchoImpl(), new NoOpMethodInterceptor(), ECHO_ONLY );
        try
        {
            proxy.ioException();
            fail();
        }
        catch( IOException e )
        {
        }
    }

    public void testWithNonAccessibleTargetType()
    {
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( new PrivateEcho(), new NoOpMethodInterceptor(), ECHO_ONLY );
        proxy.echo();

    }

    public void testChangingArguments()
    {
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( new EchoImpl(), new ChangeArgumentInterceptor(), ECHO_ONLY );
        assertEquals( "something different", proxy.echoBack( "whatever" ) );
    }

    private static class PrivateEcho extends EchoImpl
    {
    }

    private static class ChangeArgumentInterceptor implements Interceptor
    {
        public Object intercept( Invocation methodInvocation ) throws Throwable
        {
            methodInvocation.getArguments()[0] = "something different";
            return methodInvocation.proceed();
        }
    }

    protected static class NoOpMethodInterceptor implements Interceptor
    {
        public Object intercept( Invocation methodInvocation ) throws Throwable
        {
            return methodInvocation.proceed();
        }
    }

    private static class InvokerTester implements Invoker
    {
        private Object method;
        private Object[] args;
        private Object proxy;

        public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
        {
            this.proxy = proxy;
            this.method = method;
            this.args = args;
            return null;
        }
    }

    private static class InterceptorTester implements Interceptor
    {
        private Object[] arguments;
        private Method method;
        private Object proxy;
        private Class invocationClass;

        public Object intercept( Invocation methodInvocation ) throws Throwable
        {
            arguments = methodInvocation.getArguments();
            method = methodInvocation.getMethod();
            proxy = methodInvocation.getProxy();
            invocationClass = methodInvocation.getClass();
            return methodInvocation.proceed();
        }
    }
}
