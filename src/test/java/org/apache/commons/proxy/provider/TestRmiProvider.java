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
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private RmiEchoImpl implObject;
    private Registry registry;
    // A port that is unlikely to clash with another on the build machine
    private int UNLIKELY_PORT = 13099;

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public void setUpRegistry() throws Exception
    {
        implObject = new RmiEchoImpl( UNLIKELY_PORT );
        registry = LocateRegistry.createRegistry( UNLIKELY_PORT );
        registry.bind( "echo", implObject );
    }

    public void tearDown() throws Exception
    {
        if( registry != null )
        {
            tearDownRegistry();
        }
    }

    public void testWithNoRegistry() throws Exception
    {
        final RmiProvider provider = new RmiProvider();
        provider.setName( "echo" );
        provider.setPort( UNLIKELY_PORT );
        try
        {
            provider.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
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

    public void testGetObject() throws Exception
    {
        setUpRegistry();
        final RmiProvider provider = new RmiProvider( "echo" );
        provider.setPort( UNLIKELY_PORT );
        final RmiEcho echo = ( RmiEcho ) provider.getObject();
        assertEquals( "Hello, World!", echo.echoBack( "Hello, World!" ) );
    }

    public void testGetObjectWithHost() throws Exception
    {
        setUpRegistry();
        final RmiProvider provider = new RmiProvider( "localhost", "echo" );
        provider.setPort( UNLIKELY_PORT );
        final RmiEcho echo = ( RmiEcho ) provider.getObject();
        assertEquals( "Hello, World!", echo.echoBack( "Hello, World!" ) );
    }

    public void testGetObjectWithInvalidName() throws Exception
    {
        setUpRegistry();
        final RmiProvider provider = new RmiProvider( "bogus" );
        provider.setPort( UNLIKELY_PORT );
        try
        {
            provider.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
        }
    }

    public void testGetObjectWithPortAndHost() throws Exception
    {
        setUpRegistry();
        final RmiProvider provider = new RmiProvider( "localhost", Registry.REGISTRY_PORT, "echo" );
        provider.setPort( UNLIKELY_PORT );
        final RmiEcho echo = ( RmiEcho ) provider.getObject();
        assertEquals( "Hello, World!", echo.echoBack( "Hello, World!" ) );
    }

    public void testGetObjectWithPortAndHostAndFactory() throws Exception
    {
        setUpRegistry();
        final RmiProvider provider = new RmiProvider( "localhost", UNLIKELY_PORT,
                                                      RMISocketFactory.getDefaultSocketFactory(), "echo" );
        final RmiEcho echo = ( RmiEcho ) provider.getObject();
        assertEquals( "Hello, World!", echo.echoBack( "Hello, World!" ) );
    }


}
