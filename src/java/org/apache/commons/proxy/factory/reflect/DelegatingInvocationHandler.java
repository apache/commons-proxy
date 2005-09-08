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

import org.apache.commons.proxy.ProxyUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An invocation handler which delegates to another object.
 *
 * @author James Carman
 * @version 1.0
 */
public abstract class DelegatingInvocationHandler extends AbstractInvocationHandler
{
//----------------------------------------------------------------------------------------------------------------------
// Abstract Methods
//----------------------------------------------------------------------------------------------------------------------

    protected abstract Object getDelegate();

//----------------------------------------------------------------------------------------------------------------------
// InvocationHandler Implementation
//----------------------------------------------------------------------------------------------------------------------

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        try
        {
            return method.invoke( getDelegate(), args );
        }
        catch( InvocationTargetException e )
        {
            throw e.getTargetException();
        }
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    /**
     * A simplified proxy creation method which merely creates a proxy which supports all the interfaces implemented by
     * the delegate.
     *
     * @return a proxy which supports all the interfaces implemented by the delegate
     */
    public Object createProxy()
    {
        return createProxy( ProxyUtils.getAllInterfaces( getDelegate().getClass() ) );
    }

    /**
     * A simplified proxy creation method which merely creates a proxy which supports all the interfaces implemented by
     * the delegate, using the specified class loader.
     *
     * @return a proxy which supports all the interfaces implemented by the delegate, using the specified class loader.
     */
    public Object createProxy( ClassLoader classLoader )
    {
        return createProxy( classLoader, ProxyUtils.getAllInterfaces( getDelegate().getClass() ) );
    }
}

