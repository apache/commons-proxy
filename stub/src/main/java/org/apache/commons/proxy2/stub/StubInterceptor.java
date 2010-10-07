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

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.invoker.RecordedInvocation;

/**
 * StubInterceptor collects, then replays on demand, the stubbing information.
 *
 * @author Matt Benson
 */
/*
 * Handling with an interceptor means we get the parent ProxyFactory's implementation of an Invocation
 */
abstract class StubInterceptor implements Interceptor {
    /** Serialization version */
    private static final long serialVersionUID = 1L;

    private interface InvocationMatcher {
        boolean matches(Invocation invocation);
    }

    private static abstract class Result {
        InvocationMatcher invocationMatcher;

        Result(InvocationMatcher invocationMatcher) {
            super();
            this.invocationMatcher = invocationMatcher;
        }

        abstract Object getResult() throws Throwable;
    }

    private static class Answer extends Result {
        private Object answer;

        Answer(InvocationMatcher invocationMatcher, Object answer) {
            super(invocationMatcher);
            this.answer = answer;
        }

        /**
         * Get the answer.
         * @return Object
         */
        public Object getResult() throws Throwable {
            return answer instanceof ObjectProvider<?> ? ((ObjectProvider<?>) answer)
                    .getObject()
                    : answer;
        }
    }

    private static class Throw extends Result {
        private ObjectProvider<? extends Throwable> throwableProvider;

        /**
         * Create a new Throw instance.
         * @param invocationMatcher
         */
        Throw(InvocationMatcher invocationMatcher,
                ObjectProvider<? extends Throwable> throwableProvider) {
            super(invocationMatcher);
            this.throwableProvider = throwableProvider;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        Object getResult() throws Throwable {
            throw throwableProvider.getObject();
        }
    }

    private boolean complete;
    private RecordedInvocation currentInvocation;
    private Deque<Result> resultStack = new ArrayDeque<Result>();

    /**
     * Create a new StubInterceptor instance.
     */
    StubInterceptor() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public Object intercept(Invocation invocation) throws Throwable {
        if (complete) {
            for (Result result : resultStack) {
                if (result.invocationMatcher.matches(invocation)) {
                    return result.getResult();
                }
            }
            return interceptFallback(invocation);
        }
        RecordedInvocation incoming = new RecordedInvocation(invocation
                .getMethod(), invocation.getArguments());
        synchronized (this) {
            if (currentInvocation == null) {
                currentInvocation = incoming;
            } else {
                throw new IllegalStateException("Called " + incoming
                        + " while stubbing of " + currentInvocation
                        + " is incomplete.");
            }
        }
        return ProxyUtils.nullValue(invocation.getMethod().getReturnType());
    }

    void addAnswer(Object o) {
        resultStack.push(validAnswer(o));
    }

    void addThrow(ObjectProvider<? extends Throwable> throwableProvider) {
        resultStack.push(new Throw(currentMatcher(), throwableProvider));
    }

    private synchronized InvocationMatcher currentMatcher() {
        if (complete) {
            throw new IllegalStateException(
                    "Answers not permitted; stubbing already marked as complete.");
        }
        if (currentInvocation == null) {
            throw new IllegalStateException(
                    "No ongoing stubbing found for any method");
        }
        try {
            final RecordedInvocation recordedInvocation = currentInvocation;
            return new InvocationMatcher() {

                public boolean matches(Invocation invocation) {
                    return invocation.getMethod().getName().equals(
                            recordedInvocation.getInvokedMethod().getName())
                            && Arrays.equals(invocation.getArguments(),
                                    recordedInvocation.getArguments());
                }

            };
        } finally {
            currentInvocation = null;
        }
    }

    /**
     * Validate and return the requested answer to the current invocation.
     * @param o
     * @return Answer
     */
    synchronized Answer validAnswer(Object o) {
        if (currentInvocation == null) {
            //fall through and let currentMatcher() throw the exception
        } else if (o instanceof ObjectProvider<?>) {
            // give ObjectProviders the benefit of the doubt?
        } else {
            Method m = currentInvocation.getInvokedMethod();
            if (!TypeUtils.isInstance(o, m.getReturnType())) {
                throw new IllegalArgumentException(String.format("%s does not specify a valid return value for %s", o,
                    m));
            }
        }
        return new Answer(currentMatcher(), o);
    }

    void complete() {
        this.complete = true;
    }

    /**
     * Fallback behavior
     * @param invocation
     * @return result
     * @throws Throwable
     */
    protected abstract Object interceptFallback(Invocation invocation)
            throws Throwable;
}