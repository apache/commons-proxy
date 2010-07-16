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
     * Creates a proxy which delegates to the object provided by <code>delegateProvider</code>.
     *
     * @param classLoader      the class loader to use when generating the proxy
     * @param delegateProvider the delegate provider
     * @param proxyClass     the class/interface that the proxy should implement
     * @return a proxy which delegates to the object provided by the target <code>delegateProvider>
     */
    public <T> T createDelegatorProxy( ClassLoader classLoader, ObjectProvider<T> delegateProvider,
                                       Class<T> proxyClass );

    /**
     * Creates a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     * <code>target</code> object.  The proxy will be generated using the current thread's "context class loader."
     *
     * @param target       the target object
     * @param interceptor  the method interceptor
     * @param proxyClass the class/interface that the proxy should implement
     * @return a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     *         <code>target</code> object.
     */
    public <T> T createInterceptorProxy( Object target, Interceptor interceptor,
                                         Class<T> proxyClass );

    /**
     * Creates a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     * <code>target</code> object.
     *
     * @param classLoader  the class loader to use when generating the proxy
     * @param target       the target object
     * @param interceptor  the method interceptor
     * @param proxyClass the class/interface that the proxy should implement.
     * @return a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     *         <code>target</code> object.
     */
    public <T> T createInterceptorProxy( ClassLoader classLoader,
                                         Object target,
                                         Interceptor interceptor,
                                         Class<T> proxyClass );

    /**
     * Creates a proxy which uses the provided {@link Invoker} to handle all method invocations.  The proxy will be
     * generated using the current thread's "context class loader."
     *
     * @param invoker      the invoker
     * @param proxyClass the class/interface that the proxy should implement
     * @return a proxy which uses the provided {@link Invoker} to handle all method invocations
     */
    public <T> T createInvokerProxy( Invoker invoker, Class<T> proxyClass );

    /**
     * Creates a proxy which uses the provided {@link Invoker} to handle all method invocations.
     *
     * @param classLoader  the class loader to use when generating the proxy
     * @param invoker      the invoker
     * @param proxyClass the class/interface that the proxy should implement
     * @return a proxy which uses the provided {@link Invoker} to handle all method invocations
     */
    public <T> T createInvokerProxy( ClassLoader classLoader, Invoker invoker,
                                     Class<T> proxyClass );

    /**
     * Creates a proxy which delegates to the object provided by <code>delegateProvider</code>.  The proxy will be
     * generated using the current thread's "context class loader."
     *
     * @param delegateProvider the delegate provider
     * @param proxyClass     the class/interface that the proxy should implement
     * @return a proxy which delegates to the object provided by the target object provider
     */
    public <T> T createDelegatorProxy( ObjectProvider<T> delegateProvider, Class<T> proxyClass );
    /**
     * Returns true if all <code>proxyClasses</code> are interfaces.
     *
     * @param proxyClasses the proxy classes
     * @return true if all <code>proxyClasses</code> are interfaces
     */
    public boolean canProxy( Class<?>... proxyClasses );

    /**
     * Creates a proxy which delegates to the object provided by <code>delegateProvider</code>.  The proxy will be
     * generated using the current thread's "context class loader."
     *
     * @param delegateProvider the delegate provider
     * @param proxyClasses     the interfaces that the proxy should implement
     * @return a proxy which delegates to the object provided by the target object provider
     */
    public Object createDelegatorProxy( ObjectProvider<?> delegateProvider, Class<?>... proxyClasses );

    /**
     * Creates a proxy which delegates to the object provided by <code>delegateProvider</code>.
     *
     * @param classLoader      the class loader to use when generating the proxy
     * @param delegateProvider the delegate provider
     * @param proxyClasses     the interfaces that the proxy should implement
     * @return a proxy which delegates to the object provided by the target <code>delegateProvider>
     */
    public Object createDelegatorProxy( ClassLoader classLoader, ObjectProvider<?> delegateProvider,
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
    public Object createInterceptorProxy( Object target, Interceptor interceptor,
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
    public Object createInterceptorProxy( ClassLoader classLoader, Object target, Interceptor interceptor,
                                          Class<?>... proxyClasses );

    /**
     * Creates a proxy which uses the provided {@link Invoker} to handle all method invocations.  The proxy will be
     * generated using the current thread's "context class loader."
     *
     * @param invoker      the invoker
     * @param proxyClasses the interfaces that the proxy should implement
     * @return a proxy which uses the provided {@link Invoker} to handle all method invocations
     */
    public Object createInvokerProxy( Invoker invoker, Class<?>... proxyClasses );

    /**
     * Creates a proxy which uses the provided {@link Invoker} to handle all method invocations.
     *
     * @param classLoader  the class loader to use when generating the proxy
     * @param invoker      the invoker
     * @param proxyClasses the interfaces that the proxy should implement
     * @return a proxy which uses the provided {@link Invoker} to handle all method invocations
     */
    public Object createInvokerProxy( ClassLoader classLoader, Invoker invoker,
                                      Class<?>... proxyClasses );
}
