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

import org.apache.commons.proxy.Invocation;
import org.apache.commons.proxy.ProxyUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A reflection-based implementation of the {@link Invocation} interface.
 *
 * @author James Carman
 * @version 1.0
 */
class ReflectionInvocation implements Invocation
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final Method method;
    private final Object[] arguments;
    private final Object target;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public ReflectionInvocation( Object target, Method method, Object[] arguments )
    {
        this.method = method;
        this.arguments = ( arguments == null ? ProxyUtils.EMPTY_ARGUMENTS : arguments );
        this.target = target;
    }

//----------------------------------------------------------------------------------------------------------------------
// Invocation Implementation
//----------------------------------------------------------------------------------------------------------------------


    public Object[] getArguments()
    {
        return arguments;
    }

    public Method getMethod()
    {
        return method;
    }

    public Object getProxy()
    {
        return target;
    }

    public Object proceed() throws Throwable
    {
        try
        {
            return method.invoke( target, arguments );
        }
        catch( InvocationTargetException e )
        {
            throw e.getTargetException();
        }
    }
}

