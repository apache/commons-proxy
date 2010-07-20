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
     * Learn whether this {@link ProxyFactory} is capable of creating a proxy2 for the specified set of classes.
     *
     * @param proxyClasses the proxy2 classes
     * @return boolean
     */
    public boolean canProxy( Class<?>... proxyClasses );

    /**
     * Creates a proxy2 which delegates to the object provided by <code>delegateProvider</code>.  The proxy2 will be
     * generated using the current thread's "context class loader."
     *
     * @param delegateProvider the delegate provider
     * @param proxyClasses     the interfaces that the proxy2 should implement
     * @return a proxy2 which delegates to the object provided by the target object provider
     */
    public <T> T createDelegatorProxy( ObjectProvider<?> delegateProvider, Class<?>... proxyClasses );

    /**
     * Creates a proxy2 which delegates to the object provided by <code>delegateProvider</code>.
     *
     * @param classLoader      the class loader to use when generating the proxy2
     * @param delegateProvider the delegate provider
     * @param proxyClasses     the interfaces that the proxy2 should implement
     * @return a proxy2 which delegates to the object provided by the target <code>delegateProvider>
     */
    public <T> T createDelegatorProxy( ClassLoader classLoader, ObjectProvider<?> delegateProvider,
                                        Class<?>... proxyClasses );

    /**
     * Creates a proxy2 which passes through a {@link Interceptor interceptor} before eventually reaching the
     * <code>target</code> object.  The proxy2 will be generated using the current thread's "context class loader."
     *
     * @param target       the target object
     * @param interceptor  the method interceptor
     * @param proxyClasses the interfaces that the proxy2 should implement
     * @return a proxy2 which passes through a {@link Interceptor interceptor} before eventually reaching the
     *         <code>target</code> object.
     */
    public <T> T createInterceptorProxy( Object target, Interceptor interceptor,
                                          Class<?>... proxyClasses );

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
    public <T> T createInterceptorProxy( ClassLoader classLoader, Object target, Interceptor interceptor,
                                          Class<?>... proxyClasses );

    /**
     * Creates a proxy2 which uses the provided {@link Invoker} to handle all method invocations.  The proxy2 will be
     * generated using the current thread's "context class loader."
     *
     * @param invoker      the invoker
     * @param proxyClasses the interfaces that the proxy2 should implement
     * @return a proxy2 which uses the provided {@link Invoker} to handle all method invocations
     */
    public <T> T createInvokerProxy( Invoker invoker, Class<?>... proxyClasses );

    /**
     * Creates a proxy2 which uses the provided {@link Invoker} to handle all method invocations.
     *
     * @param classLoader  the class loader to use when generating the proxy2
     * @param invoker      the invoker
     * @param proxyClasses the interfaces that the proxy2 should implement
     * @return a proxy2 which uses the provided {@link Invoker} to handle all method invocations
     */
    public <T> T createInvokerProxy( ClassLoader classLoader, Invoker invoker,
                                      Class<?>... proxyClasses );
}
