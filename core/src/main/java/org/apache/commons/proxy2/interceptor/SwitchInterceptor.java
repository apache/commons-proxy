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

package org.apache.commons.proxy2.interceptor;

import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invocation;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A {@link SwitchInterceptor} maintains a list of {@link InvocationMatcher}/{@link Interceptor} pairs.  Each
 * invocation will be checked against the registered InvocationMatchers.  If one matches the current invocation, then
 * the corresponding Interceptor will be called.  If no InvocationMatchers match, then the invocation will merely
 * {@link org.apache.commons.proxy2.Invocation#proceed()} method is called.
 */
public class SwitchInterceptor implements Interceptor, Serializable
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final List<Case> cases = new CopyOnWriteArrayList<Case>();

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public SwitchInterceptor()
    {
    }

    public SwitchInterceptor(InvocationMatcher matcher, Interceptor interceptor)
    {
        cases.add(new Case(matcher, interceptor));
    }

//----------------------------------------------------------------------------------------------------------------------
// Interceptor Implementation
//----------------------------------------------------------------------------------------------------------------------

    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        for (Case currentCase : cases)
        {
            if(currentCase.matcher.matches(invocation))
            {
                return currentCase.interceptor.intercept(invocation);
            }
        }
        return invocation.proceed();
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public SwitchInterceptor onCase(InvocationMatcher matcher, Interceptor interceptor)
    {
        cases.add(new Case(matcher, interceptor));
        return this;
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private static class Case implements Serializable
    {
        private InvocationMatcher matcher;
        private Interceptor interceptor;

        private Case(InvocationMatcher matcher, Interceptor interceptor)
        {
            this.matcher = matcher;
            this.interceptor = interceptor;
        }
    }
}
