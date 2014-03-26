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

package org.apache.commons.proxy2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.proxy2.util.AbstractTestCase;
import org.apache.commons.proxy2.util.DuplicateEcho;
import org.apache.commons.proxy2.util.Echo;
import org.apache.commons.proxy2.util.EchoImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProxyUtilsTest extends AbstractTestCase
{
    //**********************************************************************************************************************
    // Fields
    //**********************************************************************************************************************

    private Properties prevProperties;

    //**********************************************************************************************************************
    // Other Methods
    //**********************************************************************************************************************

    @Before
    public void setUp() throws Exception
    {
        prevProperties = System.getProperties();
        System.setProperties(new Properties());
    }

    @After
    public void tearDown() throws Exception
    {
        System.setProperties(prevProperties);
    }

    @Test
    public void testNullValue()
    {
        assertNullValue(null, String.class);
        assertNullValue((char) 0, Character.TYPE);
        assertNullValue(0, Integer.TYPE);
        assertNullValue((long) 0, Long.TYPE);
        assertNullValue((short) 0, Short.TYPE);
        assertNullValue((double) 0, Double.TYPE);
        assertNullValue((float) 0, Float.TYPE);
        assertNullValue(false, Boolean.TYPE);
        assertNullValue((byte) 0, Byte.TYPE);
    }

    private void assertNullValue(Object expected, Class<?> type)
    {
        assertEquals(expected, ProxyUtils.nullValue(type));
    }

    @Test
    public void testGetAllInterfaces()
    {
        assertNull(ProxyUtils.getAllInterfaces(null));
        assertEquals(Arrays.asList(new Class[] { DuplicateEcho.class, Serializable.class, Echo.class }),
                Arrays.asList(ProxyUtils.getAllInterfaces(EchoImpl.class)));
    }

    @Test
    public void testGetJavaClassName() throws Exception
    {
        assertEquals("java.lang.Object[]", ProxyUtils.getJavaClassName(Object[].class));
        assertEquals("java.lang.Object[][]", ProxyUtils.getJavaClassName(Object[][].class));
        assertEquals("java.lang.String[][][]", ProxyUtils.getJavaClassName(String[][][].class));
        assertEquals("int", ProxyUtils.getJavaClassName(Integer.TYPE));
        assertEquals("float", ProxyUtils.getJavaClassName(Float.TYPE));
        assertEquals("long", ProxyUtils.getJavaClassName(Long.TYPE));
        assertEquals("double", ProxyUtils.getJavaClassName(Double.TYPE));
        assertEquals("short", ProxyUtils.getJavaClassName(Short.TYPE));
        assertEquals("byte", ProxyUtils.getJavaClassName(Byte.TYPE));
        assertEquals("char", ProxyUtils.getJavaClassName(Character.TYPE));
        assertEquals("boolean", ProxyUtils.getJavaClassName(Boolean.TYPE));
    }
}