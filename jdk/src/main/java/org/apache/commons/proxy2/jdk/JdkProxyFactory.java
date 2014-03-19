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

package org.apache.commons.proxy2.jdk;

import org.apache.commons.proxy2.*;
import org.apache.commons.proxy2.impl.AbstractProxyFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * {@link ProxyFactory} implementation that uses {@link java.lang.reflect.Proxy} proxies.
 */
public class JdkProxyFactory extends AbstractProxyFactory
{
//**********************************************************************************************************************
// ProxyFactory Implementation
//**********************************************************************************************************************

    /**
     * Creates a proxy2 which delegates to the object provided by <code>delegateProvider</code>.
     *
     * @param classLoader      the class loader to use when generating the proxy2
     * @param delegateProvider the delegate provider
     * @param proxyClasses     the interfaces that the proxy2 should implement
     * @return a proxy2 which delegates to the object provided by the target <code>delegateProvider>
     */
    @SuppressWarnings("unchecked")
    public <T> T createDelegatorProxy( ClassLoader classLoader, ObjectProvider<?> delegateProvider,
                                        Class<?>... proxyClasses )
    {
        return (T) Proxy.newProxyInstance(classLoader, proxyClasses,
                                      new DelegatorInvocationHandler(delegateProvider));
    }

    /**
     * Creates a proxy2 which passes through a {@link Interceptor interceptor} before eventually reaching the
     * <code>target</code> object.
     *
     * @param classLoader  the class loader to use when generating the proxy2
     * @param target       the target object
     * @param interceptor  the method interceptor
     * @param proxyClasses the interfaces that the proxy2 should implement.
     * @return a proxy2 which passes through a {@link Interceptor interceptor} before eventually reaching the
     *         <code>target</code> object.
     */
    @SuppressWarnings("unchecked")
    public <T> T createInterceptorProxy( ClassLoader classLoader, Object target, Interceptor interceptor,
                                          Class<?>... proxyClasses )
    {
        return (T) Proxy
                .newProxyInstance(classLoader, proxyClasses, new InterceptorInvocationHandler(target, interceptor));
    }

    /**
     * Creates a proxy2 which uses the provided {@link Invoker} to handle all method invocations.
     *
     * @param classLoader  the class loader to use when generating the proxy2
     * @param invoker      the invoker
     * @param proxyClasses the interfaces that the proxy2 should implement
     * @return a proxy2 which uses the provided {@link Invoker} to handle all method invocations
     */
    @SuppressWarnings("unchecked")
    public <T> T createInvokerProxy( ClassLoader classLoader, Invoker invoker,
                                      Class<?>... proxyClasses )
    {
        return (T) Proxy.newProxyInstance(classLoader, proxyClasses, new InvokerInvocationHandler(invoker));
    }

//**********************************************************************************************************************
// Inner Classes
//**********************************************************************************************************************

    private abstract static class AbstractInvocationHandler implements InvocationHandler, Serializable
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
        {
            if( ProxyUtils.isHashCode(method) )
            {
                return System.identityHashCode(proxy);
            }
            else if( ProxyUtils.isEqualsMethod(method) )
            {
                return proxy == args[0];
            }
            else
            {
                return invokeImpl(proxy, method, args);
            }
        }

        protected abstract Object invokeImpl( Object proxy, Method method, Object[] args ) throws Throwable;
    }

    private static class DelegatorInvocationHandler extends AbstractInvocationHandler
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private final ObjectProvider<?> delegateProvider;

        protected DelegatorInvocationHandler( ObjectProvider<?> delegateProvider )
        {
            this.delegateProvider = delegateProvider;
        }

        public Object invokeImpl( Object proxy, Method method, Object[] args ) throws Throwable
        {
            try
            {
                return method.invoke(delegateProvider.getObject(), args);
            }
            catch( InvocationTargetException e )
            {
                throw e.getTargetException();
            }
        }
    }

    private static class InterceptorInvocationHandler extends AbstractInvocationHandler
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private final Object target;
        private final Interceptor methodInterceptor;

        public InterceptorInvocationHandler( Object target, Interceptor methodInterceptor )
        {
            this.target = target;
            this.methodInterceptor = methodInterceptor;
        }

        public Object invokeImpl( Object proxy, Method method, Object[] args ) throws Throwable
        {
            final ReflectionInvocation invocation = new ReflectionInvocation(proxy, target, method, args);
            return methodInterceptor.intercept(invocation);
        }
    }

    private static class InvokerInvocationHandler extends AbstractInvocationHandler
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private final Invoker invoker;

        public InvokerInvocationHandler( Invoker invoker )
        {
            this.invoker = invoker;
        }

        public Object invokeImpl( Object proxy, Method method, Object[] args ) throws Throwable
        {
            return invoker.invoke(proxy, method, args);
        }
    }

    private static class ReflectionInvocation implements Invocation, Serializable
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private final Object proxy;
        private final Object target;
        private final Method method;
        private final Object[] arguments;

        public ReflectionInvocation( Object proxy, Object target, Method method, Object[] arguments )
        {
            this.proxy = proxy;
            this.target = target;
            this.method = method;
            this.arguments = ( arguments == null ? ProxyUtils.EMPTY_ARGUMENTS : arguments );
        }

        public Object[] getArguments()
        {
            return arguments;
        }

        public Method getMethod()
        {
            return method;
        }

        public Object getProxy()
        {
            return proxy;
        }

        public Object proceed() throws Throwable
        {
            try
            {
                return method.invoke(target, arguments);
            }
            catch( InvocationTargetException e )
            {
                throw e.getTargetException();
            }
        }
    }
}
