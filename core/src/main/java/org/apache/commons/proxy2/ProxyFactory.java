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

/**
 * ProxyFactory interface.
 * @since 2.0
 */
public interface ProxyFactory
{
//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    /**
     * Learn whether this {@link ProxyFactory} is capable of creating a proxy for the specified set of classes.
     *
     * @param proxyClasses the proxy2 classes
     * @return boolean
     */
    boolean canProxy( Class<?>... proxyClasses );

    /**
     * Creates a proxy which delegates to the object provided by <code>delegateProvider</code>.  The proxy will be
     * generated using the current thread's "context class loader."
     *
     * @param delegateProvider the delegate provider
     * @param proxyClasses     the interfaces that the proxy should implement
     * @return a proxy which delegates to the object provided by the target object provider
     */
    <T> T createDelegatorProxy( ObjectProvider<?> delegateProvider, Class<?>... proxyClasses );

    /**
     * Creates a proxy which delegates to the object provided by <code>delegateProvider</code>.
     *
     * @param classLoader      the class loader to use when generating the proxy
     * @param delegateProvider the delegate provider
     * @param proxyClasses     the interfaces that the proxy should implement
     * @return a proxy which delegates to the object provided by the target <code>delegateProvider>
     */
    <T> T createDelegatorProxy( ClassLoader classLoader, ObjectProvider<?> delegateProvider,
                                        Class<?>... proxyClasses );

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
    <T> T createInterceptorProxy( Object target, Interceptor interceptor,
                                          Class<?>... proxyClasses );

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
    <T> T createInterceptorProxy( ClassLoader classLoader, Object target, Interceptor interceptor,
                                          Class<?>... proxyClasses );

    /**
     * Creates a proxy which uses the provided {@link Invoker} to handle all method invocations.  The proxy will be
     * generated using the current thread's "context class loader."
     *
     * @param invoker      the invoker
     * @param proxyClasses the interfaces that the proxy should implement
     * @return a proxy which uses the provided {@link Invoker} to handle all method invocations
     */
    <T> T createInvokerProxy( Invoker invoker, Class<?>... proxyClasses );

    /**
     * Creates a proxy which uses the provided {@link Invoker} to handle all method invocations.
     *
     * @param classLoader  the class loader to use when generating the proxy
     * @param invoker      the invoker
     * @param proxyClasses the interfaces that the proxy should implement
     * @return a proxy which uses the provided {@link Invoker} to handle all method invocations
     */
    <T> T createInvokerProxy( ClassLoader classLoader, Invoker invoker,
                                      Class<?>... proxyClasses );
}
