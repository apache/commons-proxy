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

import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.interceptor.matcher.MethodNameMatcher;
import org.apache.commons.proxy2.util.AbstractTestCase;
import org.apache.commons.proxy2.util.Echo;
import org.apache.commons.proxy2.util.MockInvocation;

import java.lang.reflect.Method;

public class TestSwitchInterceptor extends AbstractTestCase
{
//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public void testWithMultipleAdvices() throws Throwable
    {
        SwitchInterceptor interceptor = new SwitchInterceptor();
        interceptor.when(new MethodNameMatcher("echo")).then(new ConstantInterceptor("bar"));
        interceptor.when(new MethodNameMatcher("echoBack")).then(new ConstantInterceptor("baz"));
        Method method = Echo.class.getMethod("echoBack", String.class);
        Invocation invocation = new MockInvocation(method, "foo", "foo");
        assertEquals("baz", interceptor.intercept(invocation));
    }

    public void testWithNoAdvice() throws Throwable
    {
        SwitchInterceptor interceptor = new SwitchInterceptor();
        Method method = Echo.class.getMethod("echoBack", String.class);
        Invocation invocation = new MockInvocation(method, "foo", "foo");
        assertEquals("foo", interceptor.intercept(invocation));
    }

    public void testWithSingleAdviceWhichDoesNotMatch() throws Throwable
    {
        SwitchInterceptor interceptor = new SwitchInterceptor().when(new MethodNameMatcher("echoBackZZZZ")).then(new ConstantInterceptor("bar"));
        Method method = Echo.class.getMethod("echoBack", String.class);
        Invocation invocation = new MockInvocation(method, "foo", "foo");
        assertEquals("foo", interceptor.intercept(invocation));
    }

    public void testWithSingleAdviceWhichMatches() throws Throwable
    {
        SwitchInterceptor interceptor = new SwitchInterceptor().when(new MethodNameMatcher("echoBack")).then(new ConstantInterceptor("bar"));
        Method method = Echo.class.getMethod("echoBack", String.class);
        Invocation invocation = new MockInvocation(method, "foo", "foo");
        assertEquals("bar", interceptor.intercept(invocation));
    }
}
