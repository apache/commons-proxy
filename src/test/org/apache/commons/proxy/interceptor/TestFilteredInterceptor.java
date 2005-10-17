/*
 *  Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.commons.proxy.interceptor;

import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;
import org.apache.commons.proxy.util.SuffixInterceptor;
import org.apache.commons.proxy.factory.cglib.CglibProxyFactory;
import org.apache.commons.proxy.interceptor.filter.SimpleFilter;
import org.apache.commons.proxy.Interceptor;
import junit.framework.TestCase;

/**
 * @author James Carman
 * @version 1.0
 */
public class TestFilteredInterceptor extends TestCase
{
    public void testFilterAccepts()
    {
        Echo echo = ( Echo ) new InterceptorChain( new Interceptor[] { new FilteredInterceptor( new SuffixInterceptor( "a" ), new SimpleFilter( new String[] { "echoBack" } ) ) } ).createProxyProvider( new CglibProxyFactory(), new EchoImpl() ).getObject();
        assertEquals( "messagea", echo.echoBack( "message" ) );
    }

    public void testFilterDenies()
    {
        Echo echo = ( Echo ) new InterceptorChain( new Interceptor[] { new FilteredInterceptor( new SuffixInterceptor( "a" ), new SimpleFilter() ) } ).createProxyProvider( new CglibProxyFactory(), new EchoImpl() ).getObject();
        assertEquals( "message", echo.echoBack( "message" ) );
    }
}
