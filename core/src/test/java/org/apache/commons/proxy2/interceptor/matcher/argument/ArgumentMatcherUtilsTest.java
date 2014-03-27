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

package org.apache.commons.proxy2.interceptor.matcher.argument;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.proxy2.interceptor.matcher.ArgumentMatcher;
import org.junit.Test;

@SuppressWarnings("boxing") // test code
public class ArgumentMatcherUtilsTest
{
    @Test
    public void testAny() throws Exception
    {
        ArgumentMatcher<Object> matcher = ArgumentMatcherUtils.any();
        assertTrue(matcher.matches(null));
        assertTrue(matcher.matches("Hello!"));
        assertTrue(matcher.matches(12345));
    }

    @Test
    public void testEq() throws Exception
    {
        ArgumentMatcher<String> matcher = ArgumentMatcherUtils.eq("Hello");
        assertTrue(matcher.matches("Hello"));
        assertFalse(matcher.matches(null));
    }

    @Test
    public void testGt() throws Exception
    {
        ArgumentMatcher<Integer> matcher = ArgumentMatcherUtils.gt(5);
        assertTrue(matcher.matches(6));
        assertFalse(matcher.matches(5));
        assertFalse(matcher.matches(1));
        assertFalse(matcher.matches(null));
    }

    @Test
    public void testGte() throws Exception
    {
        ArgumentMatcher<Integer> matcher = ArgumentMatcherUtils.gte(5);
        assertTrue(matcher.matches(6));
        assertTrue(matcher.matches(5));
        assertFalse(matcher.matches(1));
        assertFalse(matcher.matches(null));
    }

    @Test
    public void testIsA() throws Exception
    {
        ArgumentMatcher<Object> matcher = ArgumentMatcherUtils.isA(String.class);
        assertFalse(matcher.matches(null));
        assertTrue(matcher.matches("Hello"));
        assertFalse(matcher.matches(123));
    }

    @Test
    public void testIsNull() throws Exception
    {
        ArgumentMatcher<Object> matcher = ArgumentMatcherUtils.isNull();
        assertTrue(matcher.matches(null));
        assertFalse(matcher.matches("Hello"));
        assertFalse(matcher.matches(123));
    }

    @Test
    public void testLt() throws Exception
    {
        ArgumentMatcher<Integer> matcher = ArgumentMatcherUtils.lt(5);
        assertTrue(matcher.matches(4));
        assertFalse(matcher.matches(5));
        assertFalse(matcher.matches(19));
        assertFalse(matcher.matches(null));
    }

    @Test
    public void testLte() throws Exception
    {
        ArgumentMatcher<Integer> matcher = ArgumentMatcherUtils.lte(5);
        assertTrue(matcher.matches(4));
        assertTrue(matcher.matches(5));
        assertFalse(matcher.matches(19));
        assertFalse(matcher.matches(null));
    }

    @Test
    public void testMatches() throws Exception
    {
        ArgumentMatcher<String> matcher = ArgumentMatcherUtils.matches("(abc)+");
        assertTrue(matcher.matches("abc"));
        assertTrue(matcher.matches("abcabc"));
        assertFalse(matcher.matches(""));
        assertFalse(matcher.matches(null));
    }

    @Test
    public void testNotNull() throws Exception
    {
        ArgumentMatcher<String> matcher = ArgumentMatcherUtils.notNull();
        assertTrue(matcher.matches("Hello"));
        assertFalse(matcher.matches(null));
    }

    @Test
    public void testStartsWith() throws Exception
    {
        ArgumentMatcher<String> matcher = ArgumentMatcherUtils.startsWith("abc");
        assertTrue(matcher.matches("abc"));
        assertTrue(matcher.matches("abcd"));
        assertFalse(matcher.matches("ab"));
        assertFalse(matcher.matches(null));
    }

    @Test
    public void testEndsWith() throws Exception
    {
        ArgumentMatcher<String> matcher = ArgumentMatcherUtils.endsWith("abc");
        assertTrue(matcher.matches("abc"));
        assertTrue(matcher.matches("dabc"));
        assertFalse(matcher.matches("ab"));
        assertFalse(matcher.matches(null));
    }
}
