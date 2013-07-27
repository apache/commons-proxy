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

import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.invoker.RecordedInvocation;

import java.lang.reflect.Method;
import java.util.*;

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

    /**
     * This is an interface because we plan to add more sophisticated stub matching in the future.
     */
    private interface InvocationMatcher {
        boolean matches(Invocation invocation);
    }

    private interface Result {
        Object getResult() throws Throwable;
    }

    private static class Answer implements Result {
        private Object answer;

        Answer(Object answer) {
            this.answer = answer;
        }

        /**
         * Get the answer.
         * @return Object
         */
        public Object getResult() throws Throwable {
            return answer instanceof ObjectProvider<?> ? ((ObjectProvider<?>) answer).getObject() : answer;
        }
    }

    private static class Throw implements Result {
        private ObjectProvider<? extends Throwable> throwableProvider;

        /**
         * Create a new Throw instance.
         * @param throwableProvider the throwable provider
         */
        Throw(ObjectProvider<? extends Throwable> throwableProvider) {
            this.throwableProvider = throwableProvider;
        }

        /**
         * {@inheritDoc}
         */
        public Object getResult() throws Throwable {
            throw throwableProvider.getObject();
        }
    }

    private boolean complete;
    private RecordedInvocation currentInvocation;
    private Map<String, Result> noArgResults = new HashMap<String, Result>();

    // we generalize to the List interface here so that we can replace an empty set of results with a shared immutable instance:
    private List<Pair<InvocationMatcher, ? extends Result>> matchingResultStack =
        new ArrayList<Pair<InvocationMatcher, ? extends Result>>();

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
            if (invocation.getMethod().getParameterTypes().length == 0) {
                Result result = noArgResults.get(invocation.getMethod().getName());
                if (result != null) {
                    return result.getResult();
                }
            } else {
                for (Pair<InvocationMatcher, ? extends Result> pair : matchingResultStack) {
                    if (pair.getLeft().matches(invocation)) {
                        return pair.getRight().getResult();
                    }
                }
            }
            return interceptFallback(invocation);
        }
        RecordedInvocation incoming = new RecordedInvocation(invocation.getMethod(), invocation.getArguments());
        synchronized (this) {
            if (currentInvocation == null) {
                currentInvocation = incoming;
            } else {
                throw new IllegalStateException("Called " + incoming + " while stubbing of " + currentInvocation
                    + " is incomplete.");
            }
        }
        return ProxyUtils.nullValue(invocation.getMethod().getReturnType());
    }

    /**
     * Provide a return value to the currently stubbed method.
     * @param o {@link ObjectProvider} or hard value
     */
    synchronized void addAnswer(Object o) {
        assertCanAddResult();
        Method m = currentInvocation.getInvokedMethod();
        boolean valid;
        if (o instanceof ObjectProvider<?>) {
            //compiler checked:
            valid = true;
        } else {
            valid = acceptsValue(m, o);
        }
        if (!valid) {
            throw new IllegalArgumentException(String.format("%s does not specify a valid return value for %s", o,
                m));
        }
        addResult(new Answer(o));
    }

    /**
     * Respond to the currently stubbed method with a thrown exception. 
     * @param throwableProvider
     */
    synchronized void addThrow(ObjectProvider<? extends Throwable> throwableProvider) {
        assertCanAddResult();
        addResult(new Throw(throwableProvider));
    }

    private void assertCanAddResult() {
        if (complete) {
            throw new IllegalStateException("Answers not permitted; stubbing already marked as complete.");
        }
        if (currentInvocation == null) {
            throw new IllegalStateException("No ongoing stubbing found for any method");
        }
    }

    private void addResult(Result result) {
        try {
            if (currentInvocation.getInvokedMethod().getParameterTypes().length == 0) {
                //match on method name only:
                noArgResults.put(currentInvocation.getInvokedMethod().getName(), result);
            } else {
                InvocationMatcher invocationMatcher;
                //TODO use an approach like that of Mockito wrt capturing arg matchers, falling back to force equality like so:
                final RecordedInvocation recordedInvocation = currentInvocation;
                invocationMatcher = new RecordedInvocationMatcher(recordedInvocation);
                //add to beginning, for priority, hence "stack" nomenclature:
                matchingResultStack.add(0, Pair.of(invocationMatcher, result));
            }
        } finally {
            currentInvocation = null;
        }
    }

    /**
     * Mark stubbing as complete.
     */
    synchronized void complete() {
        this.complete = true;
        if (noArgResults.isEmpty()) {
            noArgResults = Collections.emptyMap();
        }
        if (matchingResultStack.isEmpty()) {
            matchingResultStack = Collections.emptyList();
        }
    }

    /**
     * Provide fallback behavior.
     * @param invocation
     * @return result
     * @throws Throwable
     */
    protected abstract Object interceptFallback(Invocation invocation) throws Throwable;

    /**
     * Learn whether the specified method accepts the specified return value.
     * Default implementation defers to {@link TypeUtils#isInstance(Object, java.lang.reflect.Type)}.
     * @param m
     * @param o
     * @return result of compatibility comparison
     */
    protected boolean acceptsValue(Method m, Object o) {
        return TypeUtils.isInstance(o, m.getReturnType());
    }

    private static final class RecordedInvocationMatcher implements InvocationMatcher {

        private final RecordedInvocation recordedInvocation;

        private RecordedInvocationMatcher(RecordedInvocation recordedInvocation) {
            this.recordedInvocation = recordedInvocation;
        }

        public boolean matches(Invocation invocation) {
            return invocation.getMethod().getName().equals(recordedInvocation.getInvokedMethod().getName())
                && Arrays.equals(invocation.getArguments(), recordedInvocation.getArguments());
        }

    }
}