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

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.interceptor.matcher.InvocationMatcher;

/**
 * A {@link SwitchInterceptor} maintains a list of
 * {@link org.apache.commons.proxy2.interceptor.matcher.InvocationMatcher}/{@link Interceptor} pairs. Each invocation
 * will be checked against the registered InvocationMatchers. If one matches the current invocation, then the
 * corresponding Interceptor will be called. If no InvocationMatchers match, the
 * {@link org.apache.commons.proxy2.Invocation#proceed()} method is called with no interception.
 */
public class SwitchInterceptor implements Interceptor, Serializable
{
    //******************************************************************************************************************
    // Fields
    //******************************************************************************************************************

    private static final long serialVersionUID = 1L;

    private final List<Pair<InvocationMatcher, Interceptor>> cases
        = new CopyOnWriteArrayList<Pair<InvocationMatcher, Interceptor>>();

    //******************************************************************************************************************
    // Constructors
    //******************************************************************************************************************

    public SwitchInterceptor()
    {
    }

    //******************************************************************************************************************
    // Interceptor Implementation
    //******************************************************************************************************************

    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        for (Pair<InvocationMatcher, Interceptor> currentCase : cases)
        {
            if (currentCase.getLeft().matches(invocation))
            {
                return currentCase.getRight().intercept(invocation);
            }
        }
        return invocation.proceed();
    }

    //******************************************************************************************************************
    // Other Methods
    //******************************************************************************************************************

    public CaseBuilder when(InvocationMatcher matcher)
    {
        return new CaseBuilder(matcher);
    }

    //******************************************************************************************************************
    // Inner Classes
    //******************************************************************************************************************

    public class CaseBuilder
    {
        private final InvocationMatcher matcher;

        private CaseBuilder(InvocationMatcher matcher)
        {
            this.matcher = matcher;
        }

        public SwitchInterceptor then(Interceptor interceptor)
        {
            cases.add(new ImmutablePair<InvocationMatcher, Interceptor>(matcher, interceptor));
            return SwitchInterceptor.this;
        }
    }
}
