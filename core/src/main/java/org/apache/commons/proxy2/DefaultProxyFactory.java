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

import java.util.Arrays;
import java.util.ServiceLoader;

/**
 * {@link ProxyFactory} implementation that delegates to the first discovered
 * {@link ProxyFactory} service provider that {@link #canProxy(Class...)}.
 *
 * @author Matt Benson
 */
class DefaultProxyFactory implements ProxyFactory {
    /** Shared instance */
    static final DefaultProxyFactory INSTANCE = new DefaultProxyFactory();

    private static final ServiceLoader<ProxyFactory> SERVICES = ServiceLoader
            .load(ProxyFactory.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canProxy(Class<?>... proxyClasses) {
        for (ProxyFactory proxyFactory : SERVICES) {
            if (proxyFactory.canProxy(proxyClasses)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createDelegatorProxy(ObjectProvider<?> delegateProvider,
            Class<?>... proxyClasses) {
        return getCapableProxyFactory(proxyClasses).createDelegatorProxy(
                delegateProvider, proxyClasses);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createDelegatorProxy(ClassLoader classLoader,
            ObjectProvider<?> delegateProvider, Class<?>... proxyClasses) {
        return getCapableProxyFactory(proxyClasses).createDelegatorProxy(
                classLoader, delegateProvider, proxyClasses);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createInterceptorProxy(Object target, Interceptor interceptor,
            Class<?>... proxyClasses) {
        return getCapableProxyFactory(proxyClasses).createInterceptorProxy(
                target, interceptor, proxyClasses);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createInterceptorProxy(ClassLoader classLoader, Object target,
            Interceptor interceptor, Class<?>... proxyClasses) {
        return getCapableProxyFactory(proxyClasses).createInterceptorProxy(
                classLoader, target, interceptor, proxyClasses);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createInvokerProxy(Invoker invoker, Class<?>... proxyClasses) {
        return getCapableProxyFactory(proxyClasses).createInvokerProxy(invoker,
                proxyClasses);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createInvokerProxy(ClassLoader classLoader, Invoker invoker,
            Class<?>... proxyClasses) {
        return getCapableProxyFactory(proxyClasses).createInvokerProxy(
                classLoader, invoker, proxyClasses);
    }

    private ProxyFactory getCapableProxyFactory(Class<?>... proxyClasses) {
        for (ProxyFactory proxyFactory : SERVICES) {
            if (proxyFactory.canProxy(proxyClasses)) {
                return proxyFactory;
            }
        }
        throw new IllegalArgumentException("Could not proxy "
                + Arrays.toString(proxyClasses));
    }
}
