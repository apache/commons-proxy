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
package org.apache.commons.proxy.provider;

import junit.framework.TestCase;
import org.apache.commons.proxy.exception.ObjectProviderException;
import org.apache.commons.proxy.util.rmi.RmiEcho;
import org.apache.commons.proxy.util.rmi.RmiEchoImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;

public class TestRmiProvider extends TestCase
{
    private RmiEchoImpl implObject;
    private Registry registry;

    public void setUp() throws Exception
    {
        implObject = new RmiEchoImpl();
        registry = LocateRegistry.createRegistry( Registry.REGISTRY_PORT );
        registry.bind( "echo", implObject );
    }

    public void tearDown() throws Exception
    {
        if( registry != null )
        {
            tearDownRegistry();
        }
    }

    private void tearDownRegistry()
            throws RemoteException, NotBoundException
    {
        registry.unbind( "echo" );
        UnicastRemoteObject.unexportObject( implObject, true );
        UnicastRemoteObject.unexportObject( registry, true );
        registry = null;
    }

    public void testWithNoRegistry() throws Exception
    {
        tearDownRegistry();
        final RmiProvider provider = new RmiProvider( "echo" );
        try
        {
            provider.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
        }
    }

    public void testGetObject() throws Exception
    {
        final RmiProvider provider = new RmiProvider( "echo" );
        final RmiEcho echo = ( RmiEcho ) provider.getObject();
        assertEquals( "Hello, World!", echo.echoBack( "Hello, World!" ) );
    }

    public void testGetObjectWithInvalidName()
    {
        final RmiProvider provider = new RmiProvider( "bogus" );
        try
        {
            provider.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
        }
    }

    public void testGetObjectWithHost() throws Exception
    {
        final RmiProvider provider = new RmiProvider( "localhost", "echo" );
        final RmiEcho echo = ( RmiEcho ) provider.getObject();
        assertEquals( "Hello, World!", echo.echoBack( "Hello, World!" ) );
    }

    public void testGetObjectWithPortAndHost() throws Exception
    {
        final RmiProvider provider = new RmiProvider( "localhost", Registry.REGISTRY_PORT, "echo" );
        final RmiEcho echo = ( RmiEcho ) provider.getObject();
        assertEquals( "Hello, World!", echo.echoBack( "Hello, World!" ) );
    }

    public void testGetObjectWithPortAndHostAndFactory() throws Exception
    {
        final RmiProvider provider = new RmiProvider( "localhost", Registry.REGISTRY_PORT,
                                                      RMISocketFactory.getDefaultSocketFactory(), "echo" );
        final RmiEcho echo = ( RmiEcho ) provider.getObject();
        assertEquals( "Hello, World!", echo.echoBack( "Hello, World!" ) );
    }
}