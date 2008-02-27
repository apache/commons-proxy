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

package org.apache.commons.proxy.interceptor;

import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.proxy.factory.javassist.JavassistProxyFactory;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;

public class TestMethodInterceptorAdapter extends TestCase
{
//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    public void testMethodInterception()
    {
        final Echo proxy = ( Echo ) new JavassistProxyFactory().createInterceptorProxy(new EchoImpl(),
                new MethodInterceptorAdapter(new SuffixMethodInterceptor(
                        " suffix")),
                new Class[] {Echo.class});
        assertEquals("message suffix", proxy.echoBack("message"));
    }

    public void testMethodInvocationImplementation() throws Exception
    {
        final InterceptorTester tester = new InterceptorTester();
        final EchoImpl target = new EchoImpl();
        final Echo proxy = ( Echo ) new JavassistProxyFactory().createInterceptorProxy(target, new MethodInterceptorAdapter(tester), new Class[] {Echo.class});
        proxy.echo();
        assertNotNull(tester.invocation.getArguments());
        assertEquals(0, tester.invocation.getArguments().length);
        assertEquals(Echo.class.getMethod("echo", new Class[] {}), tester.invocation.getMethod());
        assertEquals(Echo.class.getMethod("echo", new Class[] {}), tester.invocation.getStaticPart());
        assertEquals(target, tester.invocation.getThis());
        proxy.echoBack("Hello");
        assertNotNull(tester.invocation.getArguments());
        assertEquals(1, tester.invocation.getArguments().length);
        assertEquals("Hello", tester.invocation.getArguments()[0]);
        assertEquals(Echo.class.getMethod("echoBack", new Class[] {String.class}), tester.invocation.getMethod());
        assertEquals(Echo.class.getMethod("echoBack", new Class[] {String.class}), tester.invocation.getStaticPart());
        proxy.echoBack("Hello", "World");
        assertNotNull(tester.invocation.getArguments());
        assertEquals(2, tester.invocation.getArguments().length);
        assertEquals("Hello", tester.invocation.getArguments()[0]);
        assertEquals("World", tester.invocation.getArguments()[1]);
        assertEquals(Echo.class.getMethod("echoBack", new Class[] {String.class, String.class}), tester.invocation.getMethod());
        assertEquals(Echo.class.getMethod("echoBack", new Class[] {String.class, String.class}), tester.invocation.getStaticPart());
    }

//**********************************************************************************************************************
// Inner Classes
//**********************************************************************************************************************

    private static class InterceptorTester implements MethodInterceptor
    {
        private MethodInvocation invocation;

        public Object invoke( MethodInvocation methodInvocation ) throws Throwable
        {
            this.invocation = methodInvocation;
            return methodInvocation.proceed();
        }
    }

    private class SuffixMethodInterceptor implements MethodInterceptor
    {
        private final String suffix;

        public SuffixMethodInterceptor( String suffix )
        {
            this.suffix = suffix;
        }

        public Object invoke( MethodInvocation methodInvocation ) throws Throwable
        {
            return methodInvocation.proceed() + suffix;
        }
    }
}