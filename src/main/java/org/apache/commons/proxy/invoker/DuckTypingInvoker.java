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

package org.apache.commons.proxy.invoker;

import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.ObjectProvider;

import java.lang.reflect.Method;

/**
 * An invoker which supports "duck typing", meaning that it finds a matching method
 * on the object returned from the target provider and invokes it.  This class is useful for
 * adapting an existing class to an interface it does not implement.
 */
public class DuckTypingInvoker implements Invoker
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final ObjectProvider targetProvider;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public DuckTypingInvoker( final ObjectProvider targetProvider )
    {
        this.targetProvider = targetProvider;
    }

//----------------------------------------------------------------------------------------------------------------------
// Interface Invoker
//----------------------------------------------------------------------------------------------------------------------

    public Object invoke( final Object proxy, final Method method, final Object[] arguments ) throws Throwable
    {
        final Object target = targetProvider.getObject();
        final Class targetClass = target.getClass();
        try
        {
            final Method targetMethod = targetClass.getMethod( method.getName(), method.getParameterTypes() );
            return targetMethod.invoke( target, arguments );
        }
        catch ( NoSuchMethodException e )
        {
            throw new UnsupportedOperationException("Target type " + targetClass.getName() + " does not have a method matching " + method + "." );
        }
    }
}
