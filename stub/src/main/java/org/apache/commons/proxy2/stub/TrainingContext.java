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

package org.apache.commons.proxy2.stub;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.interceptor.SwitchInterceptor;
import org.apache.commons.proxy2.interceptor.matcher.ArgumentMatcher;
import org.apache.commons.proxy2.interceptor.matcher.InvocationMatcher;
import org.apache.commons.proxy2.invoker.RecordedInvocation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TrainingContext
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private List<ArgumentMatcher> argumentMatchers = new LinkedList<ArgumentMatcher>();
    private InvocationMatcher matcher;
    private Interceptor interceptor;
    private final SwitchInterceptor switchInterceptor;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public TrainingContext(SwitchInterceptor switchInterceptor)
    {
        this.switchInterceptor = switchInterceptor;
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public void addArgumentMatcher(ArgumentMatcher argumentMatcher)
    {
        argumentMatchers.add(argumentMatcher);
    }

    public void setInterceptor(Interceptor interceptor)
    {
        this.interceptor = interceptor;
        addCase();
    }

    private void addCase()
    {
        if (matcher != null && interceptor != null)
        {
            switchInterceptor.when(matcher).then(interceptor);
            matcher = null;
            interceptor = null;
            argumentMatchers.clear();
        }
    }

    public void stubMethodInvoked(Method method, Object[] arguments)
    {
        final ArgumentMatcher[] matchersArray = argumentMatchers.toArray(new ArgumentMatcher[argumentMatchers.size()]);
        argumentMatchers.clear();
        final RecordedInvocation invocation = new RecordedInvocation(method, arguments);
        if (ArrayUtils.isEmpty(matchersArray))
        {
            this.matcher = new ExactArgumentsMatcher(invocation);
        }
        else if (matchersArray.length == arguments.length)
        {
            this.matcher = new ArgumentMatchersMatcher(invocation, matchersArray);
        }
        else
        {
            throw new IllegalStateException("Either use exact arguments or argument matchers, but not both.");
        }
        addCase();
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private static class ArgumentMatchersMatcher implements InvocationMatcher
    {
        private final RecordedInvocation recordedInvocation;
        private final ArgumentMatcher[] matchers;

        private ArgumentMatchersMatcher(RecordedInvocation recordedInvocation, ArgumentMatcher[] matchers)
        {
            this.recordedInvocation = recordedInvocation;
            this.matchers = matchers;
        }

        @Override
        public boolean matches(Invocation invocation)
        {
            return invocation.getMethod().equals(recordedInvocation.getInvokedMethod()) &&
                    allArgumentsMatch(invocation.getArguments());
        }

        private boolean allArgumentsMatch(Object[] arguments)
        {
            for (int i = 0; i < arguments.length; i++)
            {
                Object argument = arguments[i];
                if (!matchers[i].matches(argument))
                {
                    return false;
                }
            }
            return true;
        }
    }

    private static class ExactArgumentsMatcher implements InvocationMatcher
    {
        private final RecordedInvocation recordedInvocation;

        private ExactArgumentsMatcher(RecordedInvocation recordedInvocation)
        {
            this.recordedInvocation = recordedInvocation;
        }

        @Override
        public boolean matches(Invocation invocation)
        {
            return invocation.getMethod().equals(recordedInvocation.getInvokedMethod()) &&
                    Arrays.equals(invocation.getArguments(), recordedInvocation.getArguments());
        }
    }
}
