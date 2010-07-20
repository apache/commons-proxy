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

package org.apache.commons.proxy2.provider.remoting;

import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.exception.ObjectProviderException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;

/**
 * Provides an object by looking it up in an RMI registry.
 *
 * @author James Carman
 * @since 1.0
 */
public class RmiProvider<T> implements ObjectProvider<T>
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private String host = "localhost";
    private int port = Registry.REGISTRY_PORT;
    private RMIClientSocketFactory clientSocketFactory;
    private String name;

  //**********************************************************************************************************************
 // Constructors
 //**********************************************************************************************************************

    /**
     * Create a new RmiProvider instance.
     */
    public RmiProvider()
    {
    }

    /**
     * Create a new RmiProvider instance.
     * @param name
     */
    public RmiProvider( String name )
    {
        setName(name);
    }

    /**
     * Create a new RmiProvider instance.
     * @param host
     * @param name
     */
    public RmiProvider( String host, String name )
    {
        setHost(host);
        setName(name);
    }

    /**
     * Create a new RmiProvider instance.
     * @param host
     * @param port
     * @param name
     */
    public RmiProvider( String host, int port, String name )
    {
        setHost(host);
        setName(name);
        setPort(port);
    }

    /**
     * Create a new RmiProvider instance.
     * @param host
     * @param port
     * @param clientSocketFactory
     * @param name
     */
    public RmiProvider( String host, int port, RMIClientSocketFactory clientSocketFactory, String name )
    {
        setHost(host);
        setPort(port);
        setClientSocketFactory(clientSocketFactory);
        setName(name);
    }

  //**********************************************************************************************************************
 // ObjectProvider Implementation
 //**********************************************************************************************************************

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public T getObject()
    {
        Registry reg;
        try
        {
            reg = getRegistry();
            return (T)reg.lookup(name);
        }
        catch( NotBoundException e )
        {
            throw new ObjectProviderException("Name " + name + " not found in registry at " + host + ":" + port + ".",
                    e);
        }
        catch( RemoteException e )
        {
            throw new ObjectProviderException(
                    "Unable to lookup service named " + name + " in registry at " + host + ":" + port + ".", e);
        }
    }

//**********************************************************************************************************************
// Getter/Setter Methods
//**********************************************************************************************************************

    /**
     * Set the clientSocketFactory.
     * @param clientSocketFactory the RMIClientSocketFactory to set
     */
    public void setClientSocketFactory(
            RMIClientSocketFactory clientSocketFactory) {
        this.clientSocketFactory = clientSocketFactory;
    }

    /**
     * Set the host.
     * @param host the String to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Set the name.
     * @param name the String to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the port.
     * @param port the int to set
     */
    public void setPort(int port) {
        this.port = port;
    }

//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    private Registry getRegistry()
    {
        try
        {
            if( clientSocketFactory != null )
            {
                return LocateRegistry.getRegistry(host, port, clientSocketFactory);
            }
            else
            {
                return LocateRegistry.getRegistry(host, port);
            }
        }
        catch( RemoteException e )
        {
            throw new ObjectProviderException("Unable to locate registry at " + host + ":" + port + ".", e);
        }
    }
}

