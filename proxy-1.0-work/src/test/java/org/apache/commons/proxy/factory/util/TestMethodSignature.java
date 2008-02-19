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

package org.apache.commons.proxy.factory.util;
import junit.framework.TestCase;
import org.apache.commons.proxy.util.DuplicateEcho;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.factory.util.MethodSignature;

public class TestMethodSignature extends TestCase
{
    public void testEquals() throws Exception
    {
        final MethodSignature sig = new MethodSignature( Echo.class.getMethod( "echoBack",  new Class[] { String.class } ) );
        assertTrue( sig.equals( sig ) );
        assertFalse( sig.equals( "echoBack" ) );
        assertEquals( sig, new MethodSignature( Echo.class.getMethod( "echoBack",  new Class[] { String.class } ) ) );
        assertEquals( sig, new MethodSignature( DuplicateEcho.class.getMethod( "echoBack",  new Class[] { String.class } ) ) );
        assertFalse( sig.equals( new MethodSignature( Echo.class.getMethod( "echoBack",  new Class[] { String.class, String.class } ) ) ) );
        assertFalse( sig.equals( new MethodSignature( Echo.class.getMethod( "echo",  new Class[] {} ) ) ) );
    }
}