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
import org.apache.commons.proxy.util.DuplicateEcho;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;
import org.apache.commons.proxy.util.SuffixMethodInterceptor;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author James Carman
 * @version 1.0
 */
public abstract class AbstractProxyFactoryTestCase extends AbstractTestCase
{
    protected final ProxyFactory factory;

    protected AbstractProxyFactoryTestCase( ProxyFactory factory )
    {
        this.factory = factory;
    }

    public void testInterfaceHierarchies()
    {
        final SortedSet<String> set = ( SortedSet<String> ) factory.createDelegatorProxy( constantProvider( new TreeSet<String>() ), SortedSet.class );
        set.add( "Hello" );
    }

    public void testInvocationHandlerProxy() throws Exception
    {
        final InvocationHandlerTester tester = new InvocationHandlerTester();
        final Echo echo = ( Echo )factory.createInvocationHandlerProxy( tester, Echo.class );
        echo.echoBack( "hello" );
        assertEquals( Echo.class.getMethod( "echoBack", String.class ), tester.method );
        assertSame( echo, tester.proxy );
        assertNotNull( tester.args );
        assertEquals( 1, tester.args.length );
        assertEquals( "hello", tester.args[0] );
    }

    public void testDelegatingProxyInterfaceOrder()
    {
        final Echo echo = ( Echo ) factory.createDelegatorProxy( singletonProvider( beanProvider( EchoImpl.class ) ), Echo.class, DuplicateEcho.class );
        final List<Class> expected = new LinkedList<Class>( Arrays.asList( Echo.class, DuplicateEcho.class ) );
        final List<Class> actual = new LinkedList<Class>( Arrays.asList( echo.getClass().getInterfaces() ) );
        actual.retainAll( expected );  // Doesn't alter order!
        assertEquals( expected, actual );
    }

    public void testCreateDelegatingProxy()
    {
        final Echo echo = ( Echo ) factory.createDelegatorProxy( singletonProvider( beanProvider( EchoImpl.class ) ), Echo.class );
        echo.echo();
        assertEquals( "message", echo.echoBack( "message" ) );
        assertEquals( "ab", echo.echoBack( "a", "b" ) );
    }

    public void testPrimitiveParameter()
    {
        final Echo echo = ( Echo ) factory.createDelegatorProxy( singletonProvider( beanProvider( EchoImpl.class ) ), Echo.class );
        assertEquals( 1, echo.echoBack( 1 ) );
    }

    public void testCreateInterceptorProxy()
    {
        final Echo target = ( Echo ) factory.createDelegatorProxy( singletonProvider( beanProvider( EchoImpl.class ) ), Echo.class );
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

    public void testMethodInvocationDuplicateMethods() throws Exception
    {
        final MethodInvocationTester tester = new MethodInvocationTester();
        final EchoImpl target = new EchoImpl();
        final Echo proxy = ( Echo ) factory.createInterceptorProxy( target, tester, Echo.class, DuplicateEcho.class );
        proxy.echoBack( "hello" );
        assertEquals( Echo.class.getMethod( "echoBack", String.class ), tester.method );
    }


    public void testMethodInvocationClassCaching() throws Exception
    {
        final MethodInvocationTester tester = new MethodInvocationTester();
        final EchoImpl target = new EchoImpl();
        final Echo proxy1 = ( Echo ) factory.createInterceptorProxy( target, tester, Echo.class );
        final Echo proxy2 = ( Echo ) factory.createInterceptorProxy( target, tester, Echo.class, DuplicateEcho.class );
        proxy1.echoBack( "hello1" );
        final Class invocationClass1 = tester.invocationClass;
        proxy2.echoBack( "hello2" );
        assertEquals( invocationClass1, tester.invocationClass );
    }

    public void testDelegatingProxyClassCaching() throws Exception
    {
        final Echo proxy1 = ( Echo ) factory.createDelegatorProxy( constantProvider( new EchoImpl() ), Echo.class );
        final Echo proxy2 = ( Echo ) factory.createDelegatorProxy( constantProvider( new EchoImpl() ), Echo.class );
        assertEquals( proxy1.getClass(), proxy2.getClass() );
    }

    public void testInterceptingProxyClassCaching() throws Exception
    {
        final Echo proxy1 = ( Echo ) factory.createInterceptorProxy( new EchoImpl(), new NoOpMethodInterceptor(), Echo.class );
        final Echo proxy2 = ( Echo ) factory.createInterceptorProxy( new EchoImpl(), new NoOpMethodInterceptor(), Echo.class );
        assertEquals( proxy1.getClass(), proxy2.getClass() );
    }

    public void testProxyWithCheckedException() throws Exception
    {
        final Echo proxy = ( Echo ) factory.createDelegatorProxy( constantProvider( new EchoImpl() ), Echo.class );
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
        final Echo proxy = ( Echo ) factory.createDelegatorProxy( constantProvider( new EchoImpl() ), Echo.class );
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

    private static class InvocationHandlerTester implements InvocationHandler
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

    private static class MethodInvocationTester implements MethodInterceptor
    {
        private Object[] arguments;
        private Method method;
        private Object target;
        private AccessibleObject staticPart;
        private Class invocationClass;

        public Object invoke( MethodInvocation methodInvocation ) throws Throwable
        {
            this.arguments = methodInvocation.getArguments();
            this.method = methodInvocation.getMethod();
            this.target = methodInvocation.getThis();
            this.staticPart = methodInvocation.getStaticPart();
            this.invocationClass = methodInvocation.getClass();
            return methodInvocation.proceed();
        }
    }
}
