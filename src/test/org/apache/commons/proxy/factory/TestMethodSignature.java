/* $Id$
 *
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.proxy.factory;
import junit.framework.TestCase;
import org.apache.commons.proxy.util.DuplicateEcho;
import org.apache.commons.proxy.util.Echo;

public class TestMethodSignature extends TestCase
{
    public void testEquals() throws Exception
    {
        final MethodSignature sig = new MethodSignature( Echo.class.getMethod( "echoBack", String.class ) );
        assertTrue( sig.equals( sig ) );
        assertFalse( sig.equals( "echoBack" ) );
        assertEquals( sig, new MethodSignature( Echo.class.getMethod( "echoBack", String.class ) ) );
        assertEquals( sig, new MethodSignature( DuplicateEcho.class.getMethod( "echoBack", String.class ) ) );
        assertFalse( sig.equals( new MethodSignature( Echo.class.getMethod( "echoBack", String.class, String.class ) ) ) );
        assertFalse( sig.equals( new MethodSignature( Echo.class.getMethod( "echo" ) ) ) );
    }
}