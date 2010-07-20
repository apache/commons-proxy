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

import com.caucho.burlap.client.BurlapProxyFactory;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.exception.ObjectProviderException;

import java.io.Serializable;
import java.net.MalformedURLException;

/**
 * Provides a burlap service object.
 * <p/>
 * <p>
 * <b>Dependencies</b>:
 * <ul>
 * <li>Burlap version 2.1.7 or greater</li>
 * </ul>
 * </p>
 *
 * @author James Carman
 * @since 1.0
 */
public class BurlapProvider<T> implements ObjectProvider<T>, Serializable
{
    /** Serialization version */
    private static final long serialVersionUID = 1L;

//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private Class<? extends T> serviceInterface;
    private String url;

  //**********************************************************************************************************************
 // Constructors
 //**********************************************************************************************************************

    /**
     * Create a new BurlapProvider instance.
     */
    public BurlapProvider()
    {
    }

    /**
     * Create a new BurlapProvider instance.
     * @param serviceInterface
     * @param url
     */
    public BurlapProvider( Class<? extends T> serviceInterface, String url )
    {
        this.serviceInterface = serviceInterface;
        this.url = url;
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
        try
        {
            return (T)new BurlapProxyFactory().create(serviceInterface, url);
        }
        catch( MalformedURLException e )
        {
            throw new ObjectProviderException("Invalid url given.", e);
        }
    }

//**********************************************************************************************************************
// Getter/Setter Methods
//**********************************************************************************************************************

    /**
     * Set the serviceInterface.
     * @param serviceInterface the Class to set
     */
    public void setServiceInterface(Class<? extends T> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    /**
     * Set the url.
     * @param url the String to set
     */
    public void setUrl(String url) {
        this.url = url;
    }
}

