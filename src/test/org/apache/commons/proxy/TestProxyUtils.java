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
package org.apache.commons.proxy;
import junit.framework.TestCase;
import org.apache.commons.proxy.factory.javassist.JavassistProxyFactory;
import org.apache.commons.proxy.util.Echo;

public class TestProxyUtils extends TestCase
{
    public void testCreateNullObject() throws Exception
    {
        final Echo nullEcho = ( Echo )ProxyUtils.createNullObject( new JavassistProxyFactory(),  new Class[] { Echo.class } );
        assertNull( nullEcho.echoBack( "hello" ) );
        assertNull( nullEcho.echoBack( "hello", "world" ) );
        assertEquals( ( int ) 0, nullEcho.echoBack( 12345 ) );
    }

    public void testCreateNullObjectWithClassLoader() throws Exception
    {
        final Echo nullEcho = ( Echo )ProxyUtils.createNullObject( new JavassistProxyFactory(), Echo.class.getClassLoader(),  new Class[] { Echo.class } );
        assertNull( nullEcho.echoBack( "hello" ) );
        assertNull( nullEcho.echoBack( "hello", "world" ) );
        assertEquals( ( int ) 0, nullEcho.echoBack( 12345 ) );
    }
}