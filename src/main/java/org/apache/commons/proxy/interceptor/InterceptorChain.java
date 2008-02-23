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

package org.apache.commons.proxy.interceptor;

import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.ProxyUtils;
import org.apache.commons.proxy.Interceptor;

/**
 * An <code>InterceptorChain</code> assists with creating proxies which go through a series of
 * {@link Interceptor interceptors}.
 *
 * <pre>
 *   MyServiceInterface serviceImpl = ...;
 *   ProxyFactory factory = ...;
 *   Interceptor[] interceptors = ...;
 *   InterceptorChain chain = new InterceptorChain(interceptors);
 *   ObjectProvider provider = chain.createProxyProvider(factory, serviceImpl);
 *   MyServiceInterface serviceProxy = ( MyServiceInterface )provider.getObject();
 *   serviceProxy.someServiceMethod(...); // This will go through the interceptors! 
 * </pre>
 *
 * @author James Carman
 * @since 1.0
 */
public class InterceptorChain
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------
    private final Interceptor[] interceptors;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public InterceptorChain( Interceptor[] interceptors )
    {
        this.interceptors = interceptors;
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    private Object createProxy( ProxyFactory proxyFactory, ClassLoader classLoader, Object terminus,
                                Class[] proxyClasses )
    {
        Object currentTarget = terminus;
        for( int i = interceptors.length - 1; i >= 0; --i )
        {
            currentTarget = proxyFactory
                    .createInterceptorProxy( classLoader, currentTarget, interceptors[i], proxyClasses );
        }
        return currentTarget;
    }

    /**
     * Creates an {@link ObjectProvider} which will return a proxy that sends method invocations through this
     * chain of interceptors and ultimately arrive at the supplied terminus object.  The proxy will support all
     * interfaces implemented by the terminus object.  The thread context classloader will be used to generate the
     * proxy class.
     * 
     * @param proxyFactory the {@link ProxyFactory} to use to create the proxy
     * @param terminus the terminus
     * @return an {@link ObjectProvider} which will return a proxy that sends method invocations through this
     * chain of interceptors and ultimately arrive at the supplied terminus object
     */
    public ObjectProvider createProxyProvider( ProxyFactory proxyFactory, Object terminus )
    {
        return createProxyProvider( proxyFactory, terminus, null );
    }

    /**
     * Creates an {@link ObjectProvider} which will return a proxy that sends method invocations through this
     * chain of interceptors and ultimately arrive at the supplied terminus object.  The proxy will support only
     * the specified interfaces/classes.  The thread context classloader will be used to generate the
     * proxy class.
     *
     * @param proxyFactory the {@link ProxyFactory} to use to create the proxy
     * @param terminus the terminus
     * @param proxyClasses the interfaces to support
     * @return an {@link ObjectProvider} which will return a proxy that sends method invocations through this
     * chain of interceptors and ultimately arrive at the supplied terminus object
     */
    public ObjectProvider createProxyProvider( ProxyFactory proxyFactory, Object terminus, Class[] proxyClasses )
    {
        return createProxyProvider( proxyFactory, Thread.currentThread().getContextClassLoader(), terminus,
                                    proxyClasses );
    }

    /**
     * Creates an {@link ObjectProvider} which will return a proxy that sends method invocations through this
     * chain of interceptors and ultimately arrive at the supplied terminus object.  The proxy will support only
     * the specified interfaces/classes.  The specified classloader will be used to generate the
     * proxy class.
     *
     * @param proxyFactory the {@link ProxyFactory} to use to create the proxy
     * @param classLoader the classloader to be used to generate the proxy class
     * @param terminus the terminus
     * @param proxyClasses the interfaces to support
     * @return an {@link ObjectProvider} which will return a proxy that sends method invocations through this
     * chain of interceptors and ultimately arrive at the supplied terminus object
     */
    public ObjectProvider createProxyProvider( ProxyFactory proxyFactory, ClassLoader classLoader, Object terminus,
                                               Class[] proxyClasses )
    {
        if( proxyClasses == null || proxyClasses.length == 0 )
        {
            proxyClasses = ProxyUtils.getAllInterfaces( terminus.getClass() );
        }
        return new ProxyObjectProvider( proxyFactory, classLoader, terminus, proxyClasses );
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private class ProxyObjectProvider implements ObjectProvider
    {
        private final ClassLoader classLoader;
        private final Class[] proxyClasses;
        private final Object terminus;
        private final ProxyFactory proxyFactory;

        public ProxyObjectProvider( ProxyFactory proxyFactory, ClassLoader classLoader, Object terminus,
                                    Class[] proxyClasses )
        {
            this.classLoader = classLoader;
            this.proxyClasses = proxyClasses;
            this.terminus = terminus;
            this.proxyFactory = proxyFactory;
        }

        public Object getObject()
        {
            return createProxy( proxyFactory, classLoader, terminus, proxyClasses );
        }
    }
}

