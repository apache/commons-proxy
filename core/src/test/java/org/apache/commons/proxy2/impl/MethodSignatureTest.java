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

package org.apache.commons.proxy2.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.proxy2.util.AbstractEcho;
import org.apache.commons.proxy2.util.AbstractTestCase;
import org.apache.commons.proxy2.util.DuplicateEcho;
import org.apache.commons.proxy2.util.Echo;
import org.apache.commons.proxy2.util.EchoImpl;
import org.junit.Test;

public class MethodSignatureTest extends AbstractTestCase
{
    //**********************************************************************************************************************
    // Other Methods
    //**********************************************************************************************************************

    @Test
    public void testEquals() throws Exception
    {
        final MethodSignature sig = new MethodSignature(Echo.class.getMethod("echoBack", String.class));
        assertTrue(sig.equals(sig));
        assertFalse(sig.equals("echoBack"));
        assertEquals(sig, new MethodSignature(Echo.class.getMethod("echoBack", String.class)));
        assertEquals(sig, new MethodSignature(DuplicateEcho.class.getMethod("echoBack", String.class)));
        assertFalse(sig.equals(new MethodSignature(Echo.class.getMethod("echoBack", String.class, String.class))));
        assertFalse(sig.equals(new MethodSignature(Echo.class.getMethod("echo"))));
    }

    @Test
    public void testSerialization() throws Exception
    {
        final MethodSignature sig = new MethodSignature(Echo.class.getMethod("echoBack", String.class));
        assertEquals(sig, SerializationUtils.clone(sig));
    }

    @Test
    public void testToString() throws Exception
    {
        assertEquals("echo()", new MethodSignature(Echo.class.getMethod("echo")).toString());
        assertEquals("echoBack(Ljava/lang/String;)",
                new MethodSignature(Echo.class.getMethod("echoBack", String.class)).toString());
        assertEquals("echoBack([Ljava/lang/String;)",
                new MethodSignature(Echo.class.getMethod("echoBack", String[].class)).toString());
        assertEquals("echoBack([[Ljava/lang/String;)",
                new MethodSignature(Echo.class.getMethod("echoBack", String[][].class)).toString());
        assertEquals("echoBack([[[Ljava/lang/String;)",
                new MethodSignature(Echo.class.getMethod("echoBack", String[][][].class)).toString());
        assertEquals("echoBack(I)", new MethodSignature(Echo.class.getMethod("echoBack", int.class)).toString());
        assertEquals("echoBack(Z)", new MethodSignature(Echo.class.getMethod("echoBack", boolean.class)).toString());
        assertEquals("echoBack(Ljava/lang/String;Ljava/lang/String;)",
                new MethodSignature(Echo.class.getMethod("echoBack", String.class, String.class)).toString());
        assertEquals("illegalArgument()", new MethodSignature(Echo.class.getMethod("illegalArgument")).toString());
        assertEquals("ioException()", new MethodSignature(Echo.class.getMethod("ioException")).toString());
    }

    @Test
    public void testToMethod() throws Exception
    {
        final MethodSignature sig = new MethodSignature(Echo.class.getMethod("echoBack", String.class));

        assertMethodIs(sig.toMethod(Echo.class), Echo.class, "echoBack", String.class);
        assertMethodIs(sig.toMethod(AbstractEcho.class), AbstractEcho.class, "echoBack", String.class);
        assertMethodIs(sig.toMethod(EchoImpl.class), AbstractEcho.class, "echoBack", String.class);
        assertMethodIs(sig.toMethod(DuplicateEcho.class), DuplicateEcho.class, "echoBack", String.class);
    }

    private void assertMethodIs(Method method, Class<?> declaredBy, String name, Class<?>... parameterTypes)
    {
        assertEquals(declaredBy, method.getDeclaringClass());
        assertEquals(name, method.getName());
        assertArrayEquals(parameterTypes, method.getParameterTypes());
    }
}