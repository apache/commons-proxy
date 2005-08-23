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

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * A reflection-based implementation of the <code>MethodInvocation</code> interface.
 * 
 * @author James Carman
 * @version 1.0
 */
class ReflectionMethodInvocation implements MethodInvocation
{
    private final Method method;
    private final Object[] arguments;
    private final Object target;

    public ReflectionMethodInvocation( Object target, Method method, Object[] arguments )
    {
        this.method = method;
        this.arguments = arguments;
        this.target = target;
    }

    public Object[] getArguments()
    {
        return arguments;
    }

    public Method getMethod()
    {
        return method;
    }

    public Object proceed() throws Throwable
    {
        return method.invoke( target, arguments );
    }

    public Object getThis()
    {
        return target;
    }

    public AccessibleObject getStaticPart()
    {
        return method;
    }
}
