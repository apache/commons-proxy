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

import net.sf.cglib.proxy.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.proxy2.*;
import org.apache.commons.proxy2.impl.AbstractSubclassingProxyFactory;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Cglib-based {@link ProxyFactory} implementation.
 */
public class CglibProxyFactory extends AbstractSubclassingProxyFactory
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private static CallbackFilter callbackFilter = new CglibProxyFactoryCallbackFilter();

  //**********************************************************************************************************************
 // ProxyFactory Implementation
 //**********************************************************************************************************************

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> T createDelegatorProxy(ClassLoader classLoader, ObjectProvider<?> targetProvider,
                                       Class<?>... proxyClasses)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(callbackFilter);
        enhancer.setCallbacks(new Callback[]{new ObjectProviderDispatcher(targetProvider), new EqualsHandler(), new HashCodeHandler()});
        return (T) enhancer.create();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> T createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
                                         Class<?>... proxyClasses)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(callbackFilter);
        enhancer.setCallbacks(new Callback[]{new InterceptorBridge(target, interceptor), new EqualsHandler(), new HashCodeHandler()});
        return (T) enhancer.create();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> T createInvokerProxy(ClassLoader classLoader, Invoker invoker,
                                     Class<?>... proxyClasses)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(callbackFilter);
        enhancer.setCallbacks(new Callback[]{new InvokerBridge(invoker), new EqualsHandler(), new HashCodeHandler()});
        return (T) enhancer.create();
    }

//**********************************************************************************************************************
// Inner Classes
//**********************************************************************************************************************

    private static class CglibProxyFactoryCallbackFilter implements CallbackFilter
    {
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

        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable
        {
            return o == objects[0];
        }
    }

    private static class HashCodeHandler implements MethodInterceptor, Serializable
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable
        {
            return System.identityHashCode(o);
        }
    }

    private static class InterceptorBridge implements net.sf.cglib.proxy.MethodInterceptor, Serializable
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

        public Object invoke(Object object, Method method, Object[] objects) throws Throwable
        {
            return original.invoke(object, method, objects);
        }
    }

    private static class MethodProxyInvocation implements Invocation, Serializable
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

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
            this.args = ArrayUtils.clone(args);
        }

        public Method getMethod()
        {
            return method;
        }

        public Object[] getArguments()
        {
            return args;
        }

        public Object proceed() throws Throwable
        {
            return methodProxy.invoke(target, args);
        }

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

        public Object loadObject()
        {
            return delegateProvider.getObject();
        }
    }
}
