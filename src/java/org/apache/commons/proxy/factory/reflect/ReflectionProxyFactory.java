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
package org.apache.commons.proxy.factory.reflect;

import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.factory.util.AbstractProxyFactory;

import java.lang.reflect.Proxy;

/**
 * A JDK {@link java.lang.reflect.Proxy Proxy}-based {@link org.apache.commons.proxy.ProxyFactory} implementation.
 *
 * @author James Carman
 * @version 1.0
 */
public class ReflectionProxyFactory extends AbstractProxyFactory
{
//----------------------------------------------------------------------------------------------------------------------
// ProxyFactory Implementation
//----------------------------------------------------------------------------------------------------------------------

    public Object createDelegatorProxy( ClassLoader classLoader, ObjectProvider targetProvider,
                                        Class[] proxyClasses )
    {
        return Proxy.newProxyInstance( classLoader, proxyClasses,
                                       new DelegatorInvocationHandler( targetProvider ) );
    }

    public Object createInterceptorProxy( ClassLoader classLoader, Object target, Interceptor interceptor,
                                          Class[] proxyClasses )
    {
        return Proxy
                .newProxyInstance( classLoader, proxyClasses, new InterceptorInvocationHandler( target, interceptor ) );
    }

    public Object createInvokerProxy( ClassLoader classLoader, Invoker invoker,
                                      Class[] proxyClasses )
    {
        return Proxy.newProxyInstance( classLoader, proxyClasses, new InvokerInvocationHandler( invoker ) );
    }
}

