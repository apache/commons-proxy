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

import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.exception.ProxyFactoryException;
import org.apache.commons.proxy.invoker.NullInvoker;
import org.apache.commons.proxy.provider.ConstantProvider;
import org.apache.commons.proxy.util.AbstractEcho;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;

/**
 * @author James Carman
 * @since 1.0
 */
public abstract class AbstractSubclassingProxyFactoryTestCase extends AbstractProxyFactoryTestCase
{
    protected AbstractSubclassingProxyFactoryTestCase(ProxyFactory factory)
    {
        super(factory);
    }

    public void testWithAbstractSuperclass()
    {
        final Echo echo = (Echo) factory.createDelegatorProxy(new ConstantProvider(new EchoImpl()), new Class[]{AbstractEcho.class});
        assertEquals("hello", echo.echoBack("hello"));
        assertEquals("helloworld", echo.echoBack("hello", "world"));
    }

    public void testCanProxy()
    {
        assertTrue(factory.canProxy(new Class[]{Echo.class}));
        assertTrue(factory.canProxy(new Class[]{EchoImpl.class}));
        assertFalse(factory.canProxy(new Class[]{FinalEcho.class}));
        assertTrue(factory.canProxy(new Class[]{FinalMethodEcho.class, Echo.class}));
        assertFalse(factory.canProxy(new Class[]{NoDefaultConstructorEcho.class}));
        assertTrue(factory.canProxy(new Class[]{ProtectedConstructorEcho.class}));
        assertFalse(factory.canProxy(new Class[]{InvisibleEcho.class}));
        assertFalse(factory.canProxy(new Class[]{Echo.class, EchoImpl.class, String.class}));
    }

    public void testDelegatorClassReuse()
    {
        final Echo echo1 = (Echo) factory
                .createDelegatorProxy(new ConstantProvider(new EchoImpl()), new Class[]{Echo.class, EchoImpl.class});
        final Echo echo2 = (Echo) factory
                .createDelegatorProxy(new ConstantProvider(new EchoImpl()), new Class[]{Echo.class, EchoImpl.class});
        assertSame("Delegator proxy classes should be reused.", echo1.getClass(), echo2.getClass());
    }

    public void testInterceptorClassReuse()
    {
        final Echo echo1 = (Echo) factory
                .createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), new Class[]{Echo.class, EchoImpl.class});
        final Echo echo2 = (Echo) factory
                .createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), new Class[]{Echo.class, EchoImpl.class});
        assertSame("Interceptor proxy classes should be reused.", echo1.getClass(), echo2.getClass());
    }

    public void testInvokerClassReuse()
    {
        final Echo echo1 = (Echo) factory
                .createInvokerProxy(new NullInvoker(), new Class[]{Echo.class, EchoImpl.class});
        final Echo echo2 = (Echo) factory
                .createInvokerProxy(new NullInvoker(), new Class[]{Echo.class, EchoImpl.class});
        assertSame("Invoker proxy classes should be reused.", echo1.getClass(), echo2.getClass());
    }

    public void testInvokerWithSuperclass()
    {
        final Echo echo = (Echo) factory
                .createInvokerProxy(new NullInvoker(), new Class[]{Echo.class, EchoImpl.class});
        assertTrue(echo instanceof EchoImpl);
    }

    public void testDelegatorWithSuperclass()
    {
        final Echo echo = (Echo) factory
                .createDelegatorProxy(new ConstantProvider(new EchoImpl()), new Class[]{Echo.class, EchoImpl.class});
        assertTrue(echo instanceof EchoImpl);
    }

    public void testInterceptorWithSuperclass()
    {
        final Echo echo = (Echo) factory
                .createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), new Class[]{Echo.class, EchoImpl.class});
        assertTrue(echo instanceof EchoImpl);
    }

    public void testProxiesWithClashingFinalMethodInSuperclass()
    {
        final Class[] proxyClasses = new Class[]{Echo.class, FinalMethodEcho.class};
        Echo proxy = (Echo) factory.createDelegatorProxy(new ConstantProvider(new EchoImpl()), proxyClasses);
        assertEquals("final", proxy.echoBack("echo"));

        proxy = (Echo) factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(), proxyClasses);
        assertEquals("final", proxy.echoBack("echo"));

        proxy = (Echo) factory.createInvokerProxy(new NullInvoker(), proxyClasses);
        assertEquals("final", proxy.echoBack("echo"));
    }

    public void testDelegatorWithMultipleSuperclasses()
    {
        try
        {
            factory.createDelegatorProxy(new ConstantProvider(new EchoImpl()),
                    new Class[]{EchoImpl.class, String.class});
            fail();
        }
        catch (ProxyFactoryException e)
        {
        }
    }

    public void testInterceptorWithMultipleSuperclasses()
    {
        try
        {
            factory.createInterceptorProxy(new EchoImpl(), new NoOpMethodInterceptor(),
                    new Class[]{EchoImpl.class, String.class});
            fail();
        }
        catch (ProxyFactoryException e)
        {
        }
    }

    public void testInvocationHandlerWithMultipleSuperclasses()
    {
        try
        {
            factory.createInvokerProxy(new NullInvoker(),
                    new Class[]{EchoImpl.class, String.class});
            fail();
        }
        catch (ProxyFactoryException e)
        {
        }
    }

    public static final class FinalEcho extends EchoImpl
    {
    }

    public static class FinalMethodEcho extends EchoImpl
    {
        public final String echoBack(String message)
        {
            return "final";
        }
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

    private static class InvisibleEcho extends EchoImpl
    {
    }
}
