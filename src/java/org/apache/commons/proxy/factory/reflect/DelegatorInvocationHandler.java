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

import org.apache.commons.proxy.ObjectProvider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An invocation handler which delegates to an object provided by an {@link ObjectProvider}.
 *
 * @author James Carman
 * @version 1.0
 */
class DelegatorInvocationHandler implements InvocationHandler
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final ObjectProvider delegateProvider;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    protected DelegatorInvocationHandler( ObjectProvider delegateProvider )
    {
        this.delegateProvider = delegateProvider;
    }

//----------------------------------------------------------------------------------------------------------------------
// InvocationHandler Implementation
//----------------------------------------------------------------------------------------------------------------------

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        try
        {
            return method.invoke( delegateProvider.getObject(), args );
        }
        catch( InvocationTargetException e )
        {
            throw e.getTargetException();
        }
    }
}

