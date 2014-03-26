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
package org.apache.commons.proxy2.util;

import java.lang.reflect.Method;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.ProxyUtils;

public class MockInvocation implements Invocation
{
    //----------------------------------------------------------------------------------------------------------------------
    // Fields
    //----------------------------------------------------------------------------------------------------------------------

    private final Method method;
    private final Object[] arguments;
    private final Object returnValue;

    //----------------------------------------------------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------------------------------------------------

    public MockInvocation(Method method, Object returnValue, Object... arguments)
    {
        this.returnValue = returnValue;
        this.arguments = ObjectUtils.defaultIfNull(ArrayUtils.clone(arguments), ProxyUtils.EMPTY_ARGUMENTS);
        this.method = method;
    }

    //----------------------------------------------------------------------------------------------------------------------
    // Invocation Implementation
    //----------------------------------------------------------------------------------------------------------------------

    @Override
    public Object[] getArguments()
    {
        return arguments;
    }

    @Override
    public Method getMethod()
    {
        return method;
    }

    @Override
    public Object getProxy()
    {
        return null;
    }

    @Override
    public Object proceed() throws Throwable
    {
        return returnValue;
    }
}
