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
package org.apache.commons.proxy.factory.reflect;
import junit.framework.TestCase;
import org.apache.commons.proxy.util.Echo;

import java.lang.reflect.Method;

public class TestAbstractInvocationHandler extends TestCase
{
    public void testCreateProxy() throws Exception
    {
        final TestingInvocationHandler handler = new TestingInvocationHandler();
        Echo echo = ( Echo )handler.createProxy( Echo.class );
        echo.echo();
        assertEquals( Echo.class.getMethod( "echo" ), handler.method );
        assertNull( handler.arguments );
        echo.echoBack( "hello" );
        assertEquals( Echo.class.getMethod( "echoBack", String.class ), handler.method );
        assertNotNull( handler.arguments );
        assertEquals( 1, handler.arguments.length );
        assertEquals( "hello", handler.arguments[0] );
        echo.echoBack( "hello", "world" );
        assertEquals( Echo.class.getMethod( "echoBack", String.class, String.class ), handler.method );
        assertNotNull( handler.arguments );
        assertEquals( 2, handler.arguments.length );
        assertEquals( "hello", handler.arguments[0] );
        assertEquals( "world", handler.arguments[1] );
    }

    private static class TestingInvocationHandler extends AbstractInvocationHandler
    {
        private Method method;
        private Object[] arguments;

        public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
        {
            this.method = method;
            this.arguments = args;
            return null;
        }
    }
}