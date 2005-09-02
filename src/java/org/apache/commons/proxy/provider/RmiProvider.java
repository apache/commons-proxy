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
package org.apache.commons.proxy.provider;

import org.apache.commons.proxy.exception.ObjectProviderException;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;

/**
 * Provides an object by looking it up in an RMI registry.
 *
 * @author James Carman
 * @version 1.0
 */
public class RmiProvider extends AbstractObjectProvider
{
    private String host = "localhost";
    private int port = Registry.REGISTRY_PORT;
    private RMIClientSocketFactory clientSocketFactory;
    private final String name;

    public RmiProvider( String name )
    {
        this.name = name;

    }

    public RmiProvider( String host, String name )
    {
        this.host = host;
        this.name = name;
    }

    public RmiProvider( String host, int port, String name )
    {
        this.host = host;
        this.name = name;
        this.port = port;
    }

    public RmiProvider( String host, int port, RMIClientSocketFactory clientSocketFactory, String name )
    {
        this.host = host;
        this.port = port;
        this.clientSocketFactory = clientSocketFactory;
        this.name = name;
    }

    public Object getDelegate()
    {
        Registry reg = null;
        try
        {
            reg = getRegistry();
            return reg.lookup( name );
        }
        catch( NotBoundException e )
        {
            throw new ObjectProviderException( "Name " + name + " not found in registry at " + host + ":" + port + ".",
                                               e );
        }
        catch( AccessException e )
        {
            throw new ObjectProviderException( "Registry at " + host + ":" + port + " did not allow lookup.", e );
        }
        catch( RemoteException e )
        {
            throw new ObjectProviderException(
                    "Unable to lookup service named " + name + " in registry at " + host + ":" + port + "." );
        }

    }

    private Registry getRegistry()
    {
        try
        {
            if( clientSocketFactory != null )
            {
                log.debug( "Looking up RMI registry at " + host + ":" + port +
                           " using specified client socket factory..." );
                return LocateRegistry.getRegistry( host, port, clientSocketFactory );
            }
            else
            {
                log.debug( "Looking up RMI registry at " + host + ":" + port + "..." );
                return LocateRegistry.getRegistry( host, port );
            }
        }
        catch( RemoteException e )
        {
            throw new ObjectProviderException( "Unable to locate registry at " + host + ":" + port + ".", e );
        }
    }

    public void setHost( String host )
    {
        this.host = host;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public void setClientSocketFactory( RMIClientSocketFactory clientSocketFactory )
    {
        this.clientSocketFactory = clientSocketFactory;
    }
}
