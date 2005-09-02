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
import org.apache.commons.proxy.util.rmi.RmiEcho;
import org.apache.commons.proxy.util.rmi.RmiEchoImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestRmiProvider extends TestCase
{
    RmiProvider rmiProvider;

    public void testGetObject() throws Exception
    {
        final Registry registry = LocateRegistry.createRegistry( Registry.REGISTRY_PORT );
        registry.bind( "echo", new RmiEchoImpl() );
        final RmiProvider provider = new RmiProvider( "echo" );
        final RmiEcho echo = ( RmiEcho )provider.getObject();
        assertEquals( "Hello, World!", echo.echoBack( "Hello, World!" ) );
    }
}