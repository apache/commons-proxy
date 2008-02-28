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

package org.apache.commons.proxy;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A <code>ProxyFactory</code> can be used to create three different &quot;flavors&quot; of proxy objects.
 * <p/>
 * <ul>
 * <li>Delegator - the proxy will delegate to an object provided by an {@link ObjectProvider}</li>
 * <li>Interceptor - the proxy will pass each method invocation through an {@link Interceptor}</li>
 * <li>Invoker - the proxy will allow an {@link Invoker} to handle all method invocations</li>
 * </ul>
 * <p/>
 * <p>
 * Originally, the ProxyFactory class was an interface.  However, to allow for future changes to the
 * class without breaking binary or semantic compatibility, it has been changed to a concrete class.
 * <p/>
 * </p>
 * <p>
 * <b>Note</b>: This class uses Java reflection.  For more efficient proxies, try using either
 * {@link org.apache.commons.proxy.factory.cglib.CglibProxyFactory CglibProxyFactory} or
 * {@link org.apache.commons.proxy.factory.javassist.JavassistProxyFactory JavassistProxyFactory} instead.
 * </p>
 *
 * @author James Carman
 * @since 1.0
 */
public class ProxyFactory
{
//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    /**
     * Returns true if all <code>proxyClasses</code> are interfaces.
     *
     * @param proxyClasses the proxy classes
     * @return true if all <code>proxyClasses</code> are interfaces
     */
    public boolean canProxy(Class[] proxyClasses)
    {
        for (int i = 0; i < proxyClasses.length; i++)
        {
            Class proxyClass = proxyClasses[i];
            if (!proxyClass.isInterface())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a proxy which delegates to the object provided by <code>delegateProvider</code>.  The proxy will be
     * generated using the current thread's "context class loader."
     *
     * @param delegateProvider the delegate provider
     * @param proxyClasses     the interfaces that the proxy should implement
     * @return a proxy which delegates to the object provided by the target object provider
     */
    public Object createDelegatorProxy(ObjectProvider delegateProvider, Class[] proxyClasses)
    {
        return createDelegatorProxy(Thread.currentThread().getContextClassLoader(), delegateProvider, proxyClasses);
    }

    /**
     * Creates a proxy which delegates to the object provided by <code>delegateProvider</code>.
     *
     * @param classLoader      the class loader to use when generating the proxy
     * @param delegateProvider the delegate provider
     * @param proxyClasses     the interfaces that the proxy should implement
     * @return a proxy which delegates to the object provided by the target <code>delegateProvider>
     */
    public Object createDelegatorProxy(ClassLoader classLoader, ObjectProvider delegateProvider,
                                       Class[] proxyClasses)
    {
        return Proxy.newProxyInstance(classLoader, proxyClasses,
                new DelegatorInvocationHandler(delegateProvider));
    }

    /**
     * Creates a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     * <code>target</code> object.  The proxy will be generated using the current thread's "context class loader."
     *
     * @param target       the target object
     * @param interceptor  the method interceptor
     * @param proxyClasses the interfaces that the proxy should implement
     * @return a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     *         <code>target</code> object.
     */
    public Object createInterceptorProxy(Object target, Interceptor interceptor,
                                         Class[] proxyClasses)
    {
        return createInterceptorProxy(Thread.currentThread().getContextClassLoader(), target, interceptor,
                proxyClasses);
    }

    /**
     * Creates a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     * <code>target</code> object.
     *
     * @param classLoader  the class loader to use when generating the proxy
     * @param target       the target object
     * @param interceptor  the method interceptor
     * @param proxyClasses the interfaces that the proxy should implement.
     * @return a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     *         <code>target</code> object.
     */
    public Object createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
                                         Class[] proxyClasses)
    {
        return Proxy
                .newProxyInstance(classLoader, proxyClasses, new InterceptorInvocationHandler(target, interceptor));
    }

    /**
     * Creates a proxy which uses the provided {@link Invoker} to handle all method invocations.  The proxy will be
     * generated using the current thread's "context class loader."
     *
     * @param invoker      the invoker
     * @param proxyClasses the interfaces that the proxy should implement
     * @return a proxy which uses the provided {@link Invoker} to handle all method invocations
     */
    public Object createInvokerProxy(Invoker invoker, Class[] proxyClasses)
    {
        return createInvokerProxy(Thread.currentThread().getContextClassLoader(), invoker,
                proxyClasses);
    }

    /**
     * Creates a proxy which uses the provided {@link Invoker} to handle all method invocations.
     *
     * @param classLoader  the class loader to use when generating the proxy
     * @param invoker      the invoker
     * @param proxyClasses the interfaces that the proxy should implement
     * @return a proxy which uses the provided {@link Invoker} to handle all method invocations
     */
    public Object createInvokerProxy(ClassLoader classLoader, Invoker invoker,
                                     Class[] proxyClasses)
    {
        return Proxy.newProxyInstance(classLoader, proxyClasses, new InvokerInvocationHandler(invoker));
    }

//**********************************************************************************************************************
// Inner Classes
//**********************************************************************************************************************

    private static class DelegatorInvocationHandler extends AbstractInvocationHandler
    {
        private final ObjectProvider delegateProvider;

        protected DelegatorInvocationHandler(ObjectProvider delegateProvider)
        {
            this.delegateProvider = delegateProvider;
        }

        public Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable
        {
            try
            {
                return method.invoke(delegateProvider.getObject(), args);
            }
            catch (InvocationTargetException e)
            {
                throw e.getTargetException();
            }
        }
    }

    private static class InterceptorInvocationHandler extends AbstractInvocationHandler
    {
        private final Object target;
        private final Interceptor methodInterceptor;

        public InterceptorInvocationHandler(Object target, Interceptor methodInterceptor)
        {
            this.target = target;
            this.methodInterceptor = methodInterceptor;
        }

        public Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable
        {
            final ReflectionInvocation invocation = new ReflectionInvocation(target, method, args);
            return methodInterceptor.intercept(invocation);
        }
    }

    private abstract static class AbstractInvocationHandler implements InvocationHandler, Serializable
    {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (isHashCode(method))
            {
                return new Integer(System.identityHashCode(proxy));
            }
            else if (isEqualsMethod(method))
            {
                return Boolean.valueOf(proxy == args[0]);
            }
            else
            {
                return invokeImpl(proxy, method, args);
            }
        }

        protected abstract Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable;
    }

    private static class InvokerInvocationHandler extends AbstractInvocationHandler
    {
        private final Invoker invoker;

        public InvokerInvocationHandler(Invoker invoker)
        {
            this.invoker = invoker;
        }

        public Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable
        {
            return invoker.invoke(proxy, method, args);
        }
    }

    protected static boolean isHashCode(Method method)
    {
        return "hashCode".equals(method.getName()) &&
                Integer.TYPE.equals(method.getReturnType()) &&
                method.getParameterTypes().length == 0;
    }

    protected static boolean isEqualsMethod(Method method)
    {
        return "equals".equals(method.getName()) &&
                Boolean.TYPE.equals(method.getReturnType()) &&
                method.getParameterTypes().length == 1 &&
                Object.class.equals(method.getParameterTypes()[0]);
    }

    private static class ReflectionInvocation implements Invocation, Serializable
    {
        private final Method method;
        private final Object[] arguments;
        private final Object target;

        public ReflectionInvocation(Object target, Method method, Object[] arguments)
        {
            this.method = method;
            this.arguments = (arguments == null ? ProxyUtils.EMPTY_ARGUMENTS : arguments);
            this.target = target;
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
            return target;
        }

        public Object proceed() throws Throwable
        {
            try
            {
                return method.invoke(target, arguments);
            }
            catch (InvocationTargetException e)
            {
                throw e.getTargetException();
            }
        }
    }
}

