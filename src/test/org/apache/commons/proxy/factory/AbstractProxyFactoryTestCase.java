/*
 *  Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.commons.proxy.factory;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.proxy.ProxyFactory;
import static org.apache.commons.proxy.provider.ProviderUtils.*;
import org.apache.commons.proxy.util.AbstractTestCase;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;
import org.apache.commons.proxy.util.SuffixMethodInterceptor;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * @author James Carman
 * @version 1.0
 */
public abstract class AbstractProxyFactoryTestCase extends AbstractTestCase
{
    private final ProxyFactory factory;

    protected AbstractProxyFactoryTestCase( ProxyFactory factory )
    {
        this.factory = factory;
    }

    public void testCreateProxy()
    {
        final Echo echo = ( Echo ) factory.createProxy( singletonProvider( beanProvider( EchoImpl.class ) ), Echo.class );
        echo.echo();
        assertEquals( "message", echo.echoBack( "message" ) );
        assertEquals( "ab", echo.echoBack( "a", "b" ) );
    }

    public void testCreateInterceptorProxy()
    {
        final Echo target = ( Echo ) factory.createProxy( singletonProvider( beanProvider( EchoImpl.class ) ), Echo.class );
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( target, new SuffixMethodInterceptor( " suffix" ), Echo.class );
        proxy.echo();
        assertEquals( "message suffix", proxy.echoBack( "message" ) );
    }

    public void testMethodInvocationImplementation() throws Exception
    {
        final MethodInvocationTester tester = new MethodInvocationTester();
        final EchoImpl target = new EchoImpl();
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( target, tester, Echo.class );
        proxy.echo();
        assertNull( tester.arguments );
        assertEquals( Echo.class.getMethod( "echo" ), tester.method );
        assertEquals( target, tester.target );
        assertEquals( Echo.class.getMethod( "echo" ), tester.staticPart );
        proxy.echoBack( "Hello" );
        assertNotNull( tester.arguments );
        assertEquals( 1, tester.arguments.length );
        assertEquals( "Hello", tester.arguments[0] );
        assertEquals( Echo.class.getMethod( "echoBack", String.class ), tester.method );
        proxy.echoBack( "Hello", "World" );
        assertNotNull( tester.arguments );
        assertEquals( 2, tester.arguments.length );
        assertEquals( "Hello", tester.arguments[0] );
        assertEquals( "World", tester.arguments[1] );
        assertEquals( Echo.class.getMethod( "echoBack", String.class, String.class ), tester.method );
    }

    public void testProxyWithCheckedException() throws Exception
    {
        final Echo proxy = ( Echo ) factory.createProxy( constantProvider( new EchoImpl() ), Echo.class );
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
        final Echo proxy = ( Echo ) factory.createProxy( constantProvider( new EchoImpl() ), Echo.class );
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
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( new EchoImpl(), new NoOpMethodInterceptor(),  Echo.class );
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
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( new EchoImpl(), new NoOpMethodInterceptor(),  Echo.class );
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
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( new PrivateEcho(), new NoOpMethodInterceptor(), Echo.class );
        proxy.echo();

    }

    public void testChangingArguments()
    {
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( new EchoImpl(), new ChangeArgumentInterceptor(), Echo.class );
        assertEquals( "something different", proxy.echoBack( "whatever" ) );
    }

    private static class PrivateEcho extends EchoImpl
    {
    }

    private static class ChangeArgumentInterceptor implements MethodInterceptor
    {
        public Object invoke( MethodInvocation methodInvocation ) throws Throwable
        {
            methodInvocation.getArguments()[0] = "something different";
            return methodInvocation.proceed();
        }
    }

    private static class NoOpMethodInterceptor implements MethodInterceptor
    {
        public Object invoke( MethodInvocation methodInvocation ) throws Throwable
        {
            return methodInvocation.proceed();
        }
    }

    private static class MethodInvocationTester implements MethodInterceptor
    {
        private Object[] arguments;
        private Method method;
        private Object target;
        private AccessibleObject staticPart;

        public Object invoke( MethodInvocation methodInvocation ) throws Throwable
        {
            this.arguments = methodInvocation.getArguments();
            this.method = methodInvocation.getMethod();
            this.target = methodInvocation.getThis();
            this.staticPart = methodInvocation.getStaticPart();
            return methodInvocation.proceed();
        }
    }
}
