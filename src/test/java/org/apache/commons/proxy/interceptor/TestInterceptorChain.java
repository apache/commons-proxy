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
import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.factory.cglib.CglibProxyFactory;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;
import org.apache.commons.proxy.util.SuffixInterceptor;

/**
 * @author James Carman
 * @since 1.0
 */
public class TestInterceptorChain extends TestCase
{
//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    public void testWithMultipleInterceptors()
    {
        Echo echo = ( Echo ) new InterceptorChain(new Interceptor[] {new SuffixInterceptor("a"), new SuffixInterceptor("b")}).createProxyProvider(new CglibProxyFactory(), new EchoImpl(), new Class[] {Echo.class}).getObject();
        assertEquals("messageba", echo.echoBack("message"));
    }

    public void testWithSingleInterceptor()
    {
        Echo echo = ( Echo ) new InterceptorChain(new Interceptor[] {new SuffixInterceptor("a")}).createProxyProvider(new CglibProxyFactory(), new EchoImpl(), new Class[] {Echo.class}).getObject();
        assertEquals("messagea", echo.echoBack("message"));
    }
}

