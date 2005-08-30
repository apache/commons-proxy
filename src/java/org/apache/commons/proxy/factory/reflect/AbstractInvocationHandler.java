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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * A useful baseclass for implementing invocation handlers.
 *
 * @author James Carman
 * @version 1.0
 */
public abstract class AbstractInvocationHandler implements InvocationHandler
{
    /**
     * Creates a proxy object which implements the specified proxy interfaces.
     *
     * @param proxyInterfaces the proxy interfaces
     * @return a proxy object which implements the specified proxy interfaces
     */
    public Object createProxy( Class... proxyInterfaces )
    {
        return createProxy( Thread.currentThread().getContextClassLoader(), proxyInterfaces );
    }

    /**
     * Creates a proxy object which implements the specified proxy interfaces, using the specified class loader.
     *
     * @param classLoader     the class loader
     * @param proxyInterfaces the proxy interfaces
     * @return a proxy object which implements the specified proxy interfaces, using the specified class loader.
     */
    public Object createProxy( ClassLoader classLoader, Class... proxyInterfaces )
    {
        return Proxy.newProxyInstance( classLoader, proxyInterfaces, this );
    }
}

