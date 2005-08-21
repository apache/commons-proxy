/*
 *  Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.commons.proxy.factory.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author James Carman
 * @version 1.0
 */
public abstract class AbstractInvocationHandler implements InvocationHandler
{
    public Object createProxy( Class... proxyInterfaces )
    {
        return createProxy( Thread.currentThread().getContextClassLoader(), proxyInterfaces );
    }

    public Object createProxy( ClassLoader classLoader, Class... proxyInterfaces )
    {
        return Proxy.newProxyInstance( classLoader, proxyInterfaces, this );
    }
}

