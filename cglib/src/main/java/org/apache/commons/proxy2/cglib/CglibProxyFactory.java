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

package org.apache.commons.proxy2.cglib;

import java.io.Serializable;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.impl.AbstractSubclassingProxyFactory;

/**
 * Cglib-based {@link org.apache.commons.proxy2.ProxyFactory ProxyFactory} implementation.
 */
public class CglibProxyFactory extends AbstractSubclassingProxyFactory
{
    //******************************************************************************************************************
    // Fields
    //******************************************************************************************************************

    private static final CallbackFilter CALLBACKFILTER = new CglibProxyFactoryCallbackFilter();

    //******************************************************************************************************************
    // ProxyFactory Implementation
    //******************************************************************************************************************

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createDelegatorProxy(ClassLoader classLoader, ObjectProvider<?> targetProvider,
            Class<?>... proxyClasses)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(CALLBACKFILTER);
        enhancer.setCallbacks(new Callback[] { new ObjectProviderDispatcher(targetProvider), new EqualsHandler(),
                new HashCodeHandler() });
        @SuppressWarnings("unchecked") // type inference
        final T result = (T) enhancer.create();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
            Class<?>... proxyClasses)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(CALLBACKFILTER);
        enhancer.setCallbacks(new Callback[] { new InterceptorBridge(target, interceptor), new EqualsHandler(),
                new HashCodeHandler() });
        @SuppressWarnings("unchecked") // type inference
        final T result = (T) enhancer.create();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createInvokerProxy(ClassLoader classLoader, Invoker invoker, Class<?>... proxyClasses)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(CALLBACKFILTER);
        enhancer.setCallbacks(
                new Callback[] { new InvokerBridge(invoker), new EqualsHandler(), new HashCodeHandler() });
        @SuppressWarnings("unchecked") // type inference
        final T result = (T) enhancer.create();
        return result;
    }

    //******************************************************************************************************************
    // Inner Classes
    //******************************************************************************************************************

    private static class CglibProxyFactoryCallbackFilter implements CallbackFilter
    {
        @Override
        public int accept(Method method)
        {
            if (ProxyUtils.isEqualsMethod(method))
            {
                return 1;
            }
            else if (ProxyUtils.isHashCode(method))
            {
                return 2;
            }
            else
            {
                return 0;
            }
        }
    }

    private static class EqualsHandler implements MethodInterceptor, Serializable
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable
        {
            return Boolean.valueOf(o == objects[0]);
        }
    }

    private static class HashCodeHandler implements MethodInterceptor, Serializable
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable
        {
            return Integer.valueOf(System.identityHashCode(o));
        }
    }

    private static class InterceptorBridge implements MethodInterceptor, Serializable
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private final Object target;
        private final Interceptor inner;

        public InterceptorBridge(Object target, Interceptor inner)
        {
            this.inner = inner;
            this.target = target;
        }

        @Override
        public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable
        {
            return inner.intercept(new MethodProxyInvocation(object, target, method, args, methodProxy));
        }
    }

    private static class InvokerBridge implements net.sf.cglib.proxy.InvocationHandler, Serializable
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private final Invoker original;

        public InvokerBridge(Invoker original)
        {
            this.original = original;
        }

        @Override
        public Object invoke(Object object, Method method, Object[] objects) throws Throwable
        {
            return original.invoke(object, method, objects);
        }
    }

    private static class MethodProxyInvocation implements Invocation
    {
        private final Object proxy;
        private final Object target;
        private final Method method;
        private final Object[] args;
        private final MethodProxy methodProxy;

        public MethodProxyInvocation(Object proxy, Object target, Method method, Object[] args, MethodProxy methodProxy)
        {
            this.proxy = proxy;
            this.target = target;
            this.method = method;
            this.methodProxy = methodProxy;
            this.args = ObjectUtils.defaultIfNull(ArrayUtils.clone(args), ProxyUtils.EMPTY_ARGUMENTS);
        }

        @Override
        public Method getMethod()
        {
            return method;
        }

        @Override
        public Object[] getArguments()
        {
            return args;
        }

        @Override
        public Object proceed() throws Throwable
        {
            return methodProxy.invoke(target, args);
        }

        @Override
        public Object getProxy()
        {
            return proxy;
        }
    }

    private static class ObjectProviderDispatcher implements Dispatcher, Serializable
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private final ObjectProvider<?> delegateProvider;

        public ObjectProviderDispatcher(ObjectProvider<?> delegateProvider)
        {
            this.delegateProvider = delegateProvider;
        }

        @Override
        public Object loadObject()
        {
            return delegateProvider.getObject();
        }
    }
}
