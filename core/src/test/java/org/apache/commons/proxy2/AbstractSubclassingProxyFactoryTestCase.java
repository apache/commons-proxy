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
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.commons.proxy2.exception.ProxyFactoryException;
import org.apache.commons.proxy2.invoker.NullInvoker;
import org.apache.commons.proxy2.provider.ConstantProvider;
import org.apache.commons.proxy2.util.AbstractEcho;
import org.apache.commons.proxy2.util.Echo;
import org.apache.commons.proxy2.util.EchoImpl;
import org.junit.Test;

@SuppressWarnings("serial")
public abstract class AbstractSubclassingProxyFactoryTestCase extends AbstractProxyFactoryTestCase
{
    //----------------------------------------------------------------------------------------------------------------------
    // Fields
    //----------------------------------------------------------------------------------------------------------------------

    private static final Class<?>[] DATE_ONLY = new Class[] { Date.class };

    //----------------------------------------------------------------------------------------------------------------------
    // Other Methods
    //----------------------------------------------------------------------------------------------------------------------

    @Override
    @Test
    public void testCanProxy()
    {
        assertTrue(factory.canProxy(new Class[] { Echo.class }));
        assertTrue(factory.canProxy(new Class[] { EchoImpl.class }));
        assertFalse(factory.canProxy(new Class[] { FinalEcho.class }));
        assertTrue(factory.canProxy(new Class[] { FinalMethodEcho.class, Echo.class }));
        assertFalse(factory.canProxy(new Class[] { NoDefaultConstructorEcho.class }));
        assertTrue(factory.canProxy(new Class[] { ProtectedConstructorEcho.class }));
        assertFalse(factory.canProxy(new Class[] { InvisibleEcho.class }));
        assertFalse(factory.canProxy(new Class[] { Echo.class, EchoImpl.class, String.class }));
    }

    @Override
    @Test
    public void testDelegatorEquals() throws Exception
    {
        final EqualsEcho echo = new EqualsEcho("text");
        final Echo proxy1 = factory.createDelegatorProxy(new ConstantProvider<Echo>(echo),
                new Class[] { EqualsEcho.class });
        final Echo proxy2 = factory.createDelegatorProxy(new ConstantProvider<Echo>(echo),
                new Class[] { EqualsEcho.class });
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    @Test(expected = ProxyFactoryException.class)
    public void testDelegatorWithMultipleSuperclasses()
    {
        factory.createDelegatorProxy(new ConstantProvider<EchoImpl>(new EchoImpl()), new Class[] { EchoImpl.class,
                String.class });
    }

    @Test
    public void testDelegatorWithSuperclass()
    {
        final Echo echo = factory.createDelegatorProxy(new ConstantProvider<EchoImpl>(new EchoImpl()), new Class[] {
                Echo.class, EchoImpl.class });
        assertTrue(echo instanceof EchoImpl);
    }

    @Override
    @Test
    public void testInterceptorEquals()
    {
        final EqualsEcho echo = new EqualsEcho("text");
        final Echo proxy1 = factory.createInterceptorProxy(echo, new NoOpMethodInterceptor(),
                new Class[] { EqualsEcho.class });
        final Echo proxy2 = factory.createInterceptorProxy(echo, new NoOpMethodInterceptor(),
                new Class[] { EqualsEcho.class });
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    @Test(expected = ProxyFactoryException.class)
    public void testInterceptorWithMultipleSuperclasses()
    {
        factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), new Class[] { EchoImpl.class,
                String.class });
    }

    @Test
    public void testInterceptorWithSuperclass()
    {
        final Echo echo = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), new Class[] {
                Echo.class, EchoImpl.class });
        assertTrue(echo instanceof EchoImpl);
    }

    @Test(expected = ProxyFactoryException.class)
    public void testInvocationHandlerWithMultipleSuperclasses()
    {
        factory.createInvokerProxy(new NullInvoker(), new Class[] { EchoImpl.class, String.class });
    }

    @Override
    @Test
    public void testInvokerEquals() throws Exception
    {
        final Date proxy1 = factory.createInvokerProxy(new InvokerTester(), DATE_ONLY);
        final Date proxy2 = factory.createInvokerProxy(new InvokerTester(), DATE_ONLY);
        assertEquals(proxy1, proxy1);
        assertFalse(proxy1.equals(proxy2));
        assertFalse(proxy2.equals(proxy1));
    }

    @Test
    public void testInvokerWithSuperclass()
    {
        final Echo echo = factory.createInvokerProxy(new NullInvoker(), new Class[] { Echo.class, EchoImpl.class });
        assertTrue(echo instanceof EchoImpl);
    }

    @Test
    public void testProxiesWithClashingFinalMethodInSuperclass()
    {
        final Class<?>[] proxyClasses = new Class[] { Echo.class, FinalMethodEcho.class };
        Echo proxy = factory.createDelegatorProxy(new ConstantProvider<EchoImpl>(new EchoImpl()), proxyClasses);
        assertEquals("final", proxy.echoBack("echo"));

        proxy = factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), proxyClasses);
        assertEquals("final", proxy.echoBack("echo"));

        proxy = factory.createInvokerProxy(new NullInvoker(), proxyClasses);
        assertEquals("final", proxy.echoBack("echo"));
    }

    @Test
    public void testWithAbstractSuperclass()
    {
        final Echo echo = factory.createDelegatorProxy(new ConstantProvider<EchoImpl>(new EchoImpl()),
                new Class[] { AbstractEcho.class });
        assertEquals("hello", echo.echoBack("hello"));
        assertEquals("helloworld", echo.echoBack("hello", "world"));
    }

    //----------------------------------------------------------------------------------------------------------------------
    // Inner Classes
    //----------------------------------------------------------------------------------------------------------------------

    public static class EqualsEcho extends EchoImpl
    {
        @SuppressWarnings("unused")
        private final String text;

        protected EqualsEcho()
        {
            this("testing");
        }

        public EqualsEcho(String text)
        {
            this.text = text;
        }
    }

    public static final class FinalEcho extends EchoImpl
    {
    }

    public static class FinalMethodEcho extends EchoImpl
    {
        @Override
        public final String echoBack(String message)
        {
            return "final";
        }
    }

    private static class InvisibleEcho extends EchoImpl
    {
    }

    public static class NoDefaultConstructorEcho extends EchoImpl
    {
        public NoDefaultConstructorEcho(String param)
        {
        }
    }

    public static class ProtectedConstructorEcho extends EchoImpl
    {
        protected ProtectedConstructorEcho()
        {
        }
    }
}
