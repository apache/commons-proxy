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

import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.ProxyUtils;

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

    private static class DelegatorInterceptor extends StubInterceptor {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private ObjectProvider<?> delegateProvider;

        private DelegatorInterceptor(ObjectProvider<?> delegateProvider) {
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

    private static class InterceptorInterceptor extends StubInterceptor {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private Interceptor interceptor;

        private InterceptorInterceptor(Interceptor interceptor) {
            super();
            this.interceptor = interceptor;
        }

        @Override
        protected Object interceptFallback(Invocation invocation)
                throws Throwable {
            return interceptor.intercept(invocation);
        }
    }

    private static class InvokerInterceptor extends StubInterceptor {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private Invoker invoker;

        private InvokerInterceptor(Invoker invoker) {
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
