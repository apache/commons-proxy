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
package org.apache.commons.proxy.interceptor;

import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.ProxyUtils;
import org.apache.commons.proxy.Interceptor;

/**
 * A <code>MethodInterceptorChain</code> assists with creating proxies which go through a series of
 * <code>MethodInterceptors</code>.
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

    public ObjectProvider createProxyProvider( ProxyFactory proxyFactory, Object terminus )
    {
        return createProxyProvider( proxyFactory, terminus, null );
    }

    public ObjectProvider createProxyProvider( ProxyFactory proxyFactory, Object terminus, Class[] proxyClasses )
    {
        return createProxyProvider( proxyFactory, Thread.currentThread().getContextClassLoader(), terminus,
                                    proxyClasses );
    }

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

