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
package org.apache.commons.proxy.invoker;
import junit.framework.*;
import org.apache.commons.proxy.invoker.InvocationHandlerAdapter;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.ProxyUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TestInvocationHandlerAdapter extends TestCase
{
    public void testMethodInvocation() throws Exception
    {
        InvocationHandlerTester tester = new InvocationHandlerTester();
        final Echo echo = ( Echo ) ProxyUtils.getProxyFactory().createInvokerProxy( new InvocationHandlerAdapter( tester ), new Class[] { Echo.class } );
        echo.echoBack( "hello" );
        assertEquals( Echo.class.getMethod( "echoBack", new Class[] { String.class } ), tester.method );
        assertSame( echo, tester.proxy );
        assertNotNull( tester.arguments );
        assertEquals( 1, tester.arguments.length );
        assertEquals( "hello", tester.arguments[0] );
    }

    private class InvocationHandlerTester implements InvocationHandler
    {
        private Object proxy;
        private Method method;
        private Object[] arguments;

        public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
        {
            this.proxy = proxy;
            this.method = method;
            this.arguments = args;
            return null;
        }
    }
}