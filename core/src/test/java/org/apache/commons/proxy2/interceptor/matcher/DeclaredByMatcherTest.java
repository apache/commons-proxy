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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.interceptor.matcher.invocation.DeclaredByMatcher;
import org.apache.commons.proxy2.util.AbstractTestCase;
import org.apache.commons.proxy2.util.Echo;
import org.apache.commons.proxy2.util.EchoImpl;
import org.apache.commons.proxy2.util.MockInvocation;
import org.junit.Test;

public class DeclaredByMatcherTest extends AbstractTestCase
{
    //----------------------------------------------------------------------------------------------------------------------
    // Other Methods
    //----------------------------------------------------------------------------------------------------------------------

    @Test
    public void testExactMatchNonMatching() throws Throwable
    {
        Method method = Echo.class.getMethod("echoBack", String.class);
        Invocation invocation = new MockInvocation(method, "foo");
        InvocationMatcher matcher = new DeclaredByMatcher(EchoImpl.class, true);
        assertFalse(matcher.matches(invocation));
    }

    @Test
    public void testWithSupertypeMatch() throws Throwable
    {
        Method method = Echo.class.getMethod("echoBack", String.class);
        Invocation invocation = new MockInvocation(method, "foo");
        InvocationMatcher matcher = new DeclaredByMatcher(EchoImpl.class);
        assertTrue(matcher.matches(invocation));
    }
}
