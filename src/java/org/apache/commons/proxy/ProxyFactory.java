/* $Id$
 *
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.InvocationHandler;

/**
 * A <code>ProxyFactory</code> essentially encapsulates a "proxying strategy."  All <code>commons-proxy</code> proxies
 * are created using a <code>ProxyFactory</code>.  So, to change the proxying strategy, simply provide a different
 * <code>ProxyFactory</code> implementation.
 *
 * @author James Carman
 * @version 1.0
 */
public interface ProxyFactory
{
//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a proxy which delegates to the object provided by <code>delegateProvider</code>.  The proxy will be
     * generated using the current thread's "context class loader."
     *
     * @param delegateProvider the delegate provider
     * @param proxyInterfaces  the interfaces that the proxy should implement
     * @return a proxy which delegates to the object provided by the target object provider
     */
    public Object createDelegatorProxy( ObjectProvider delegateProvider, Class... proxyInterfaces );

    /**
     * Creates a proxy which delegates to the object provided by <code>delegateProvider</code>.
     *
     * @param classLoader      the class loader to use when generating the proxy
     * @param delegateProvider the delegate provider
     * @param proxyInterfaces  the interfaces that the proxy should implement
     * @return a proxy which delegates to the object provided by the target <code>delegateProvider>
     */
    public Object createDelegatorProxy( ClassLoader classLoader, ObjectProvider delegateProvider,
                                        Class... proxyInterfaces );

    /**
     * Creates a proxy which passes through a {@link MethodInterceptor method interceptor} before eventually reaching
     * the <code>target</code> object.  The proxy will be generated using the current thread's "context class loader."
     *
     * @param target          the target object
     * @param interceptor     the method interceptor
     * @param proxyInterfaces the interfaces that the proxy should implement
     * @return a proxy which passes through a {@link MethodInterceptor method interceptor} before eventually reaching
     *         the <code>target</code> object.
     */
    public Object createInterceptorProxy( Object target, MethodInterceptor interceptor, Class... proxyInterfaces );
    /**
     * Creates a proxy which passes through a {@link org.aopalliance.intercept.MethodInterceptor method interceptor}
     * before eventually reaching the <code>target</code> object.
     *
     * @param classLoader     the class loader to use when generating the proxy
     * @param target          the target object
     * @param interceptor     the method interceptor
     * @param proxyInterfaces the interfaces that the proxy should implement.
     * @return a proxy which passes through a {@link org.aopalliance.intercept.MethodInterceptor method interceptor}
     *         before eventually reaching the <code>target</code> object.
     */
    public Object createInterceptorProxy( ClassLoader classLoader, Object target, MethodInterceptor interceptor,
                                          Class... proxyInterfaces );

    /**
     * Creates a proxy which uses the provided {@link InvocationHandler} to handle all method invocations.  The proxy
     * will be generated using the current thread's "context class loader."
     *
     * @param invocationHandler the invocation handler
     * @param proxyInterfaces   the interfaces that the proxy should implement
     * @return a proxy which uses the provided {@link InvocationHandler} to handle all method invocations
     */
    public Object createInvocationHandlerProxy( InvocationHandler invocationHandler, Class... proxyInterfaces );

    /**
     * Creates a proxy which uses the provided {@link InvocationHandler} to handle all method invocations.
     *
     * @param classLoader       the class loader to use when generating the proxy
     * @param invocationHandler the invocation handler
     * @param proxyInterfaces   the interfaces that the proxy should implement
     * @return a proxy which uses the provided {@link InvocationHandler} to handle all method invocations
     */
    public Object createInvocationHandlerProxy( ClassLoader classLoader, InvocationHandler invocationHandler,
                                                Class... proxyInterfaces );
}

