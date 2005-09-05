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

import org.apache.commons.proxy.handler.NullInvocationHandler;

/**
 * @author James Carman
 * @version 1.0
 */
public class ProxyUtils
{
    /**
     * Creates a "null object" which implements the <code>proxyInterfaces</code>.
     * @param proxyFactory the proxy factory to be used to create the proxy object
     * @param proxyInterfaces the proxy interfaces
     * @return a "null object" which implements the <code>proxyInterfaces</code>.
     */
    public static Object createNullObject( ProxyFactory proxyFactory, Class... proxyInterfaces )
    {
        return proxyFactory.createInvocationHandlerProxy( new NullInvocationHandler(), proxyInterfaces );
    }

    /**
     * Creates a "null object" which implements the <code>proxyInterfaces</code>.
     * @param proxyFactory the proxy factory to be used to create the proxy object
     * @param classLoader the class loader to be used by the proxy factory to create the proxy object
     * @param proxyInterfaces the proxy interfaces
     * @return a "null object" which implements the <code>proxyInterfaces</code>.
     */
    public static Object createNullObject( ProxyFactory proxyFactory, ClassLoader classLoader, Class... proxyInterfaces )
    {
        return proxyFactory.createInvocationHandlerProxy( classLoader, new NullInvocationHandler(), proxyInterfaces );
    }
}
