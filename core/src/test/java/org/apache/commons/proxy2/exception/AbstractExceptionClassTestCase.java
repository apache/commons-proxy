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

package org.apache.commons.proxy2.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.commons.proxy2.util.AbstractTestCase;
import org.junit.Test;

public abstract class AbstractExceptionClassTestCase extends AbstractTestCase
{
    //**********************************************************************************************************************
    // Fields
    //**********************************************************************************************************************

    private final Class<?> exceptionClass;

    //**********************************************************************************************************************
    // Constructors
    //**********************************************************************************************************************

    public AbstractExceptionClassTestCase(Class<?> exceptionClass)
    {
        this.exceptionClass = exceptionClass;
    }

    //**********************************************************************************************************************
    // Other Methods
    //**********************************************************************************************************************

    @Test
    public void testCauseOnlyConstructor() throws Exception
    {
        final Exception cause = new Exception();
        Exception e = (Exception) exceptionClass.getConstructor(new Class[] { Throwable.class }).newInstance(
                new Object[] { cause });
        assertEquals(cause.toString(), e.getMessage());
        assertEquals(cause, e.getCause());
    }

    @Test
    public void testMessageAndCauseConstructor() throws Exception
    {
        final Exception cause = new Exception();
        final String message = "message";
        Exception e = (Exception) exceptionClass.getConstructor(new Class[] { String.class, Throwable.class })
                .newInstance(new Object[] { message, cause });
        assertEquals(message, e.getMessage());
        assertEquals(cause, e.getCause());
    }

    @Test
    public void testMessageOnlyConstructor() throws Exception
    {
        final String message = "message";
        Exception e = (Exception) exceptionClass.getConstructor(new Class[] { String.class }).newInstance(
                new Object[] { message });
        assertEquals(message, e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    public void testNoArgConstructor() throws Exception
    {
        Exception e = (Exception) exceptionClass.getConstructor(new Class[] {}).newInstance(new Object[] {});
        assertNull(e.getMessage());
        assertNull(e.getCause());
    }
}
