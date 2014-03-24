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
import org.apache.commons.proxy2.*;
import org.apache.commons.proxy2.interceptor.SwitchInterceptor;
import org.apache.commons.proxy2.interceptor.matcher.ArgumentMatcher;
import org.apache.commons.proxy2.interceptor.matcher.InvocationMatcher;
import org.apache.commons.proxy2.invoker.NullInvoker;
import org.apache.commons.proxy2.invoker.RecordedInvocation;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

class TrainingContext
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private static final ThreadLocal<TrainingContext> TRAINING_CONTEXT = new ThreadLocal<TrainingContext>();

    private final ProxyFactory proxyFactory;

    private Deque<TrainingContextFrame<?>> frameDeque = new LinkedList<TrainingContextFrame<?>>();

    private final TrainingContext resume;

//----------------------------------------------------------------------------------------------------------------------
// Static Methods
//----------------------------------------------------------------------------------------------------------------------

    static TrainingContext current()
    {
        return TRAINING_CONTEXT.get();
    }

    static synchronized TrainingContext join(ProxyFactory proxyFactory)
    {
        final TrainingContext context = new TrainingContext(proxyFactory);
        TRAINING_CONTEXT.set(context);
        return context;
    }

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    private TrainingContext(ProxyFactory proxyFactory)
    {
        this.proxyFactory = proxyFactory;
        this.resume = current();
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    void part()
    {
        synchronized (TRAINING_CONTEXT)
        {
            if (resume == null)
            {
                TRAINING_CONTEXT.remove();
            }
            else
            {
                TRAINING_CONTEXT.set(resume);
            }
        }
    }

    private TrainingContextFrame<?> peek()
    {
        return frameDeque.peek();
    }

    <T> T pop()
    {
        return pop(NullInvoker.INSTANCE);
    }

    <T> T pop(Invoker invoker)
    {
        final TrainingContextFrame<?> frame = frameDeque.pop();
        return proxyFactory.createInterceptorProxy(
                proxyFactory.createInvokerProxy(invoker, frame.type),
                frame.stubInterceptor,
                frame.type);
    }
    
    <T> T push(Class<T> type)
    {
        return push(type, new SwitchInterceptor());
    }

    <T> T push(Class<T> type, SwitchInterceptor switchInterceptor)
    {
        TrainingContextFrame<T> frame = new TrainingContextFrame<T>(type, switchInterceptor);
        Invoker invoker = new TrainingInvoker(frame);
        frameDeque.push(frame);
        return proxyFactory.createInvokerProxy(invoker, type);
    }

    void record(ArgumentMatcher<?> argumentMatcher)
    {
        peek().argumentMatchers.add(argumentMatcher);
    }

    void then(Interceptor interceptor)
    {
        peek().then(interceptor);
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private static final class ExactArgumentsMatcher implements InvocationMatcher
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
                    Arrays.deepEquals(invocation.getArguments(), recordedInvocation.getArguments());
        }
    }

    private static final class MatchingArgumentsMatcher implements InvocationMatcher
    {
        private final RecordedInvocation recordedInvocation;
        private final ArgumentMatcher<?>[] matchers;

        private MatchingArgumentsMatcher(RecordedInvocation recordedInvocation, ArgumentMatcher<?>[] matchers)
        {
            this.recordedInvocation = recordedInvocation;
            this.matchers = ArrayUtils.clone(matchers);
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
                @SuppressWarnings({ "rawtypes", "unchecked" })
                final boolean matches = ((ArgumentMatcher) matchers[i]).matches(argument);
                if (!matches)
                {
                    return false;
                }
            }
            return true;
        }
    }

    private static final class TrainingContextFrame<T>
    {
        private final String id = UUID.randomUUID().toString();

        private final SwitchInterceptor stubInterceptor;

        private final List<ArgumentMatcher<?>> argumentMatchers = new LinkedList<ArgumentMatcher<?>>();

        private InvocationMatcher matcher = null;

        private final Class<T> type;

        private TrainingContextFrame(Class<T> type, SwitchInterceptor stubInterceptor)
        {
            this.type = type;
            this.stubInterceptor = stubInterceptor;
        }

        private String getId()
        {
            return id;
        }

        void then(Interceptor thenInterceptor)
        {
            if (matcher == null)
            {
                throw new IllegalStateException("No when!");
            }
            stubInterceptor.when(matcher).then(thenInterceptor);
            matcher = null;
        }

        void methodInvoked(Method method, Object[] arguments)
        {
            final ArgumentMatcher<?>[] matchersArray = argumentMatchers.toArray(new ArgumentMatcher[argumentMatchers.size()]);
            argumentMatchers.clear();
            final RecordedInvocation invocation = new RecordedInvocation(method, arguments);
            if (ArrayUtils.isEmpty(matchersArray))
            {
                this.matcher = new ExactArgumentsMatcher(invocation);
            }
            else if (matchersArray.length == arguments.length)
            {
                this.matcher = new MatchingArgumentsMatcher(invocation, matchersArray);
            }
            else
            {
                throw new IllegalStateException("Either use exact arguments or argument matchers, but not both.");
            }
        }
    }

    private static final class TrainingInvoker implements Invoker
    {
        private static final long serialVersionUID = 1L;

        private final String id;

        private TrainingInvoker(TrainingContextFrame<?> frame)
        {
            this.id = frame.getId();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable
        {
            final TrainingContextFrame<?> frame = current().peek();
            if (!frame.getId().equals(id))
            {
                throw new IllegalStateException("Wrong stub!");
            }
            else
            {
                frame.methodInvoked(method, arguments);
            }

            final Class<?> type = method.getReturnType();

            if (Object[].class.isAssignableFrom(type))
            {
                return Array.newInstance(type.getComponentType(), 0);
            }
            return ProxyUtils.nullValue(type);
        }
    }
}
