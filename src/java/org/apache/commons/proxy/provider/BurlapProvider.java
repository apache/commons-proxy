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

import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.exception.ObjectProviderException;
import com.caucho.burlap.client.BurlapProxyFactory;

import java.net.MalformedURLException;

/**
 * Provides a burlap service object.
 *
 * @author James Carman
 * @version 1.0
 */
public class BurlapProvider<T> implements ObjectProvider
{
    private final Class<T> serviceInterface;
    private final String url;

    public BurlapProvider( Class<T> serviceInterface, String url )
    {
        this.serviceInterface = serviceInterface;
        this.url = url;
    }

    public T getObject()
    {
        try
        {
            return serviceInterface.cast( new BurlapProxyFactory().create( serviceInterface, url ) );
        }
        catch( MalformedURLException e )
        {
            throw new ObjectProviderException( "Invalid url given.", e );
        }
    }
}
