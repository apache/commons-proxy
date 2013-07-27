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

package org.apache.commons.proxy2.interceptor.matcher;

import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.interceptor.InvocationMatcher;

/**
 * A {@link MethodNameMatcher} simply checks to see that the method name of the invocation matches the target method
 * name given in the constructor.
 */
public class MethodNameMatcher implements InvocationMatcher
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final String methodName;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public MethodNameMatcher(String methodName)
    {
        this.methodName = methodName;
    }

//----------------------------------------------------------------------------------------------------------------------
// InvocationMatcher Implementation
//----------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean matches(Invocation invocation)
    {
        return methodName.equals(invocation.getMethod().getName());
    }
}
