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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;

/**
 * An invocation invoker that passes through a {@link Interceptor}.
 *
 * @author James Carman
 * @version 1.0
 */
class InterceptorInvocationHandler implements InvocationHandler
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final Object target;
    private final Interceptor methodInterceptor;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public InterceptorInvocationHandler( Object target, Interceptor methodInterceptor )
    {
        this.target = target;
        this.methodInterceptor = methodInterceptor;
    }

//----------------------------------------------------------------------------------------------------------------------
// InvocationHandler Implementation
//----------------------------------------------------------------------------------------------------------------------

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        final ReflectionInvocation invocation = new ReflectionInvocation( target, method, args );
        return methodInterceptor.intercept( invocation );
    }
}

