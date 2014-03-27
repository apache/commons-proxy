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

package org.apache.commons.proxy2.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.util.Date;

import org.apache.commons.proxy2.exception.ObjectProviderException;
import org.apache.commons.proxy2.util.AbstractTestCase;
import org.junit.Test;

public class CloningProviderTest extends AbstractTestCase
{
    //**********************************************************************************************************************
    // Other Methods
    //**********************************************************************************************************************

    @Test
    public void testSerialization()
    {
        assertSerializable(new CloningProvider<Date>(new Date()));
    }

    @Test
    public void testValidCloneable()
    {
        final Date now = new Date();
        final CloningProvider<Date> provider = new CloningProvider<Date>(now);
        final Date clone1 = (Date) provider.getObject();
        assertEquals(now, clone1);
        assertNotSame(now, clone1);
        final Date clone2 = (Date) provider.getObject();
        assertEquals(now, clone2);
        assertNotSame(now, clone2);
        assertNotSame(clone2, clone1);
    }

    @Test
    public void testWithExceptionThrown()
    {
        final CloningProvider<ExceptionCloneable> provider = new CloningProvider<ExceptionCloneable>(
                new ExceptionCloneable());
        try
        {
            provider.getObject();
            fail();
        }
        catch (ObjectProviderException e)
        {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithInvalidCloneable()
    {
        new CloningProvider<InvalidCloneable>(new InvalidCloneable());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithProtectedCloneMethod()
    {
        final CloningProvider<ProtectedCloneable> provider = new CloningProvider<ProtectedCloneable>(
                new ProtectedCloneable());
        provider.getObject();
    }

    //**********************************************************************************************************************
    // Inner Classes
    //**********************************************************************************************************************

    public static class ExceptionCloneable implements Cloneable
    {
        @Override
        public Object clone()
        {
            throw new RuntimeException("No clone for you!");
        }
    }

    public static class InvalidCloneable implements Cloneable
    {
    }

    public static class ProtectedCloneable implements Cloneable
    {
        @Override
        protected Object clone()
        {
            return this;
        }
    }
}