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

package org.apache.commons.proxy2.stub;

import org.apache.commons.proxy2.*;

import java.lang.reflect.Method;

/**
 * {@link ProxyFactory} that stubs proxies' behavior using {@link StubConfigurer}s.
 * The {@link ObjectProvider}|{@link Interceptor}|{@link Invoker} specified per the
 * {@link ProxyFactory} contract provides the default behavior for non-stubbed method calls.
 *
 * @author Matt Benson
 */
public class StubProxyFactory implements ProxyFactory {
    private ProxyFactory parent;
    private StubConfigurer<?>[] stubConfigurers;

    /**
     * Create a new StubProxyFactory instance.
     * @param stubConfigurers for each proxy created by <code>parent</code>, any {@link StubConfigurer}s
     *  whose <code>stubType</code> is assignable from the proxy will be invoked to stub behavior.
     */
    public StubProxyFactory(StubConfigurer<?>... stubConfigurers) {
        this(ProxyUtils.proxyFactory(), stubConfigurers);
    }

    /**
     * Create a new StubProxyFactory instance.
     * @param parent
     * @param stubConfigurers for each proxy created by <code>parent</code>, any {@link StubConfigurer}s
     *  whose <code>stubType</code> is assignable from the proxy will be invoked to stub behavior.
     */
    public StubProxyFactory(ProxyFactory parent,
            StubConfigurer<?>... stubConfigurers) {
        super();
        if (parent == null) {
            throw new IllegalArgumentException(
                    "no parent ProxyFactory specified");
        }
        this.parent = parent;
        if (stubConfigurers == null || stubConfigurers.length == 0) {
            throw new IllegalArgumentException("no StubConfigurers specified");
        }
        this.stubConfigurers = stubConfigurers;
    }

    /**
     * {@inheritDoc}
     */
    public boolean canProxy(Class<?>... proxyClasses) {
        return parent.canProxy(proxyClasses);
    }

    /**
     * {@inheritDoc}
     */
    public <T> T createDelegatorProxy(ObjectProvider<?> delegateProvider,
            Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        final T result = (T) create(null, new DelegatorInterceptor(
                delegateProvider), proxyClasses);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public <T> T createDelegatorProxy(ClassLoader classLoader,
            final ObjectProvider<?> delegateProvider, Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        final T result = (T) create(classLoader, null,
                new DelegatorInterceptor(delegateProvider), proxyClasses);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public <T> T createInterceptorProxy(Object target, Interceptor interceptor,
            Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        final T result = (T) create(target, new InterceptorInterceptor(
                interceptor), proxyClasses);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public <T> T createInterceptorProxy(ClassLoader classLoader, Object target,
            final Interceptor interceptor, Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        final T result = (T) create(classLoader, target,
                new InterceptorInterceptor(interceptor), proxyClasses);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public <T> T createInvokerProxy(Invoker invoker, Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        final T result = (T) create(null, new InvokerInterceptor(invoker),
                proxyClasses);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public <T> T createInvokerProxy(ClassLoader classLoader,
            final Invoker invoker, Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        final T result = (T) create(classLoader, null, new InvokerInterceptor(
                invoker), proxyClasses);
        return result;
    }

    private <T> T create(Object target, StubInterceptor stubInterceptor,
            Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        final T result = (T) parent.createInterceptorProxy(target,
                stubInterceptor, proxyClasses);
        configure(result, stubInterceptor);
        return result;
    }

    private <T> T create(ClassLoader classLoader, Object target,
            StubInterceptor stubInterceptor, Class<?>... proxyClasses) {
        @SuppressWarnings("unchecked")
        final T result = (T) parent.createInterceptorProxy(classLoader, target,
                stubInterceptor, proxyClasses);
        configure(result, stubInterceptor);
        return result;
    }

    private <T> void configure(T stub, StubInterceptor stubInterceptor) {
        for (StubConfigurer<?> stubConfigurer : stubConfigurers) {
            if (stubConfigurer.getStubType().isInstance(stub)) {
                @SuppressWarnings("unchecked")
                final StubConfigurer<T> typedStubConfigurer = (StubConfigurer<T>) stubConfigurer;
                typedStubConfigurer.configure(stubInterceptor, stub);
            }
        }
        stubInterceptor.complete();

    }

    /**
     * Centralize validation of method/return value.  Noop in this implementation.
     * @param m
     * @param o
     * @return <code>true</code>
     */
    protected boolean acceptsValue(Method m, Object o) {
        return true;
    }    

    private abstract class CentralizedAnswerValidatingStubInterceptor extends StubInterceptor {

        /** Serialization version */
        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean acceptsValue(Method m, Object o) {
            return super.acceptsValue(m, o) && StubProxyFactory.this.acceptsValue(m, o);
        }    
    }

    /**
     * {@link StubInterceptor} that wraps an {@link ObjectProvider}.
     */
    private class DelegatorInterceptor extends CentralizedAnswerValidatingStubInterceptor {

        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private ObjectProvider<?> delegateProvider;

        /**
         * Create a new DelegatorInterceptor instance.
         *
         * @param delegateProvider
         */
        protected DelegatorInterceptor(ObjectProvider<?> delegateProvider) {
            super();
            this.delegateProvider = delegateProvider;
        }

        @Override
        protected Object interceptFallback(Invocation invocation)
                throws Throwable {
            return invocation.getMethod().invoke(delegateProvider.getObject(),
                    invocation.getArguments());
        }
    }

    /**
     * {@link StubInterceptor} that wraps an {@link Interceptor}.
     */
    private class InterceptorInterceptor extends CentralizedAnswerValidatingStubInterceptor {

        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private Interceptor interceptor;

        /**
         * Create a new InterceptorInterceptor instance.
         *
         * @param interceptor
         */
        protected InterceptorInterceptor(Interceptor interceptor) {
            super();
            this.interceptor = interceptor;
        }

        @Override
        protected Object interceptFallback(Invocation invocation)
                throws Throwable {
            return interceptor.intercept(invocation);
        }
    }

    /**
     * {@link StubInterceptor} that wraps an {@link Invoker}.
     */
    private class InvokerInterceptor extends CentralizedAnswerValidatingStubInterceptor {

        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private Invoker invoker;

        /**
         * Create a new InvokerInterceptor instance.
         *
         * @param invoker
         */
        protected InvokerInterceptor(Invoker invoker) {
            super();
            this.invoker = invoker;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Object interceptFallback(Invocation invocation)
                throws Throwable {
            return invoker.invoke(invocation.getProxy(),
                    invocation.getMethod(), invocation.getArguments());
        }
    }
}
