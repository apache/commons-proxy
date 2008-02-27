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

package org.apache.commons.proxy.invoker;

import junit.extensions.TestSetup;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.apache.commons.proxy.exception.InvokerException;
import org.apache.commons.proxy.factory.cglib.CglibProxyFactory;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;
import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcClientLite;

/**
 * @author James Carman
 */
public class TestXmlRpcInvoker extends TestCase
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private static WebServer server;
    private static XmlRpcClient client;

//**********************************************************************************************************************
// Static Methods
//**********************************************************************************************************************

    public static Test suite()
    {
        return new TestSetup(new TestSuite(TestXmlRpcInvoker.class))
        {
            public void run( final TestResult testResult )
            {
                Protectable p = new Protectable()
                {
                    public void protect() throws Throwable
                    {
                        try
                        {
                            setUp();
                            basicRun(testResult);
                        }
                        finally
                        {
                            tearDown();
                        }
                    }
                };
                testResult.runProtected(this, p);
            }

            protected void setUp() throws Exception
            {
                server = new WebServer(9999);
                server.addHandler("echo", new EchoImpl());
                server.start();
                client = new XmlRpcClientLite("http://localhost:9999/RPC2");
            }

            protected void tearDown() throws Exception
            {
                server.shutdown();
            }
        };
    }

//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    public void testInvalidHandlerName()
    {
        final XmlRpcInvoker handler = new XmlRpcInvoker(client, "invalid");
        final Echo echo = ( Echo ) new CglibProxyFactory()
                .createInvokerProxy(handler, new Class[] {Echo.class});
        try
        {
            echo.echoBack("Hello");
            fail();
        }
        catch( InvokerException e )
        {
        }
    }

    public void testValidInvocation() throws Exception
    {
        final XmlRpcInvoker handler = new XmlRpcInvoker(client, "echo");
        final Echo echo = ( Echo ) new CglibProxyFactory()
                .createInvokerProxy(handler, new Class[] {Echo.class});
        assertEquals("Hello", echo.echoBack("Hello"));
    }
}