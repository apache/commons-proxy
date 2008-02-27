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

import org.apache.commons.logging.Log;
import org.apache.commons.proxy.factory.cglib.CglibProxyFactory;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.io.IOException;

public class TestLoggingInterceptor extends MockObjectTestCase
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private Mock logMock;
    private Echo echo;

//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    protected void setUp() throws Exception
    {
        logMock = mock(Log.class);
        echo = ( Echo ) new CglibProxyFactory()
                .createInterceptorProxy(new EchoImpl(), new LoggingInterceptor(( Log ) logMock.proxy()),
                        new Class[] {Echo.class});
    }

    public void testException()
    {
        logMock.expects(once()).method("isDebugEnabled").will(returnValue(true));
        logMock.expects(once()).method("debug").with(eq("BEGIN ioException()"));
        logMock.expects(once()).method("debug").with(eq("EXCEPTION ioException() -- java.io.IOException"), isA(IOException.class));
        try
        {
            echo.ioException();
            fail();
        }
        catch( IOException e )
        {
        }
    }

    public void testMultipleParameters()
    {
        logMock.expects(once()).method("isDebugEnabled").will(returnValue(true));
        logMock.expects(once()).method("debug").with(eq("BEGIN echoBack(Hello, World)"));
        logMock.expects(once()).method("debug").with(eq("END echoBack() [HelloWorld]"));
        echo.echoBack("Hello", "World");
    }

    public void testNonVoidMethod()
    {
        logMock.expects(once()).method("isDebugEnabled").will(returnValue(true));
        logMock.expects(once()).method("debug").with(eq("BEGIN echoBack(Hello)"));
        logMock.expects(once()).method("debug").with(eq("END echoBack() [Hello]"));
        echo.echoBack("Hello");
    }

    public void testNullReturnValue()
    {
        logMock.expects(once()).method("isDebugEnabled").will(returnValue(true));
        logMock.expects(once()).method("debug").with(eq("BEGIN echoBack(<null>)"));
        logMock.expects(once()).method("debug").with(eq("END echoBack() [<null>]"));
        echo.echoBack(( String ) null);
    }

    public void testRuntimeException()
    {
        logMock.expects(once()).method("isDebugEnabled").will(returnValue(true));
        logMock.expects(once()).method("debug").with(eq("BEGIN illegalArgument()"));
        logMock.expects(once()).method("debug").with(eq("EXCEPTION illegalArgument() -- java.lang.IllegalArgumentException"), isA(IllegalArgumentException.class));
        try
        {
            echo.illegalArgument();
            fail();
        }
        catch( IllegalArgumentException e )
        {
        }
    }

    public void testVoidMethod()
    {
        logMock.expects(once()).method("isDebugEnabled").will(returnValue(true));
        logMock.expects(once()).method("debug").with(eq("BEGIN echo()"));
        logMock.expects(once()).method("debug").with(eq("END echo()"));
        echo.echo();
    }

    public void testWhenLoggingDisabled()
    {
        logMock = mock(Log.class);
        echo = ( Echo ) new CglibProxyFactory()
                .createInterceptorProxy(new EchoImpl(), new LoggingInterceptor(( Log ) logMock.proxy()),
                        new Class[] {Echo.class});
        logMock.expects(once()).method("isDebugEnabled").will(returnValue(false));
        echo.echoBack("Hello");
    }

    public void testWithArrayParameter()
    {
        logMock.expects(once()).method("isDebugEnabled").will(returnValue(true));
        logMock.expects(once()).method("debug").with(eq("BEGIN echoBack((java.lang.String[]){Hello, World})"));
        logMock.expects(once()).method("debug").with(eq("END echoBack() [HelloWorld]"));
        echo.echoBack(new String[] {"Hello", "World"});
    }
}