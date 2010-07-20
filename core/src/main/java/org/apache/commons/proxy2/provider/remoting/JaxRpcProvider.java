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

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.ServiceFactory;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Returns a proxy2 for a JAX-RPC-based service.
 * <p/>
 * <p>
 * <b>Dependencies</b>:
 * <ul>
 * <li>A JAX-RPC implementation</li>
 * </ul>
 * </p>
 *
 * @author James Carman
 * @since 1.0
 */
public class JaxRpcProvider<T> implements ObjectProvider<T>
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private Class<? extends T> serviceInterface;
    private String wsdlUrl;
    private String serviceNamespaceUri;
    private String serviceLocalPart;
    private String servicePrefix;
    private String portNamespaceUri;
    private String portLocalPart;
    private String portPrefix;

  //**********************************************************************************************************************
 // Constructors
 //**********************************************************************************************************************

    /**
     * Create a new JaxRpcProvider instance.
     */
    public JaxRpcProvider()
    {
    }

    /**
     * Create a new JaxRpcProvider instance.
     * @param serviceInterface
     */
    public JaxRpcProvider( Class<? extends T> serviceInterface )
    {
        this.serviceInterface = serviceInterface;
    }

  //**********************************************************************************************************************
 // ObjectProvider Implementation
 //**********************************************************************************************************************

    /**
     * {@inheritDoc}
     */
    public T getObject()
    {
        try
        {
            final Service service = ( wsdlUrl == null ?
                    ServiceFactory.newInstance().createService(getServiceQName()) : ServiceFactory
                    .newInstance().createService(new URL(wsdlUrl), getServiceQName()) );
            final QName portQName = getPortQName();
            return serviceInterface.cast(portQName == null ? service.getPort(serviceInterface) :
                    service.getPort(portQName, serviceInterface));
        }
        catch( ServiceException e )
        {
            throw new ObjectProviderException("Unable to create JAX-RPC service proxy2.", e);
        }
        catch( MalformedURLException e )
        {
            throw new ObjectProviderException("Invalid URL given.", e);
        }
    }

//**********************************************************************************************************************
// Getter/Setter Methods
//**********************************************************************************************************************

    /**
     * Set the portLocalPart.
     * @param portLocalPart the String to set
     */
    public void setPortLocalPart(String portLocalPart) {
        this.portLocalPart = portLocalPart;
    }

    /**
     * Set the portNamespaceUri.
     * @param portNamespaceUri the String to set
     */
    public void setPortNamespaceUri(String portNamespaceUri) {
        this.portNamespaceUri = portNamespaceUri;
    }

    /**
     * Set the portPrefix.
     * @param portPrefix the String to set
     */
    public void setPortPrefix(String portPrefix) {
        this.portPrefix = portPrefix;
    }

    /**
     * Set the serviceInterface.
     * @param serviceInterface the Class to set
     */
    public void setServiceInterface(Class<? extends T> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    /**
     * Set the serviceLocalPart.
     * @param serviceLocalPart the String to set
     */
    public void setServiceLocalPart(String serviceLocalPart) {
        this.serviceLocalPart = serviceLocalPart;
    }

    /**
     * Set the serviceNamespaceUri.
     * @param serviceNamespaceUri the String to set
     */
    public void setServiceNamespaceUri(String serviceNamespaceUri) {
        this.serviceNamespaceUri = serviceNamespaceUri;
    }

    /**
     * Set the servicePrefix.
     * @param servicePrefix the String to set
     */
    public void setServicePrefix(String servicePrefix) {
        this.servicePrefix = servicePrefix;
    }

    /**
     * Set the wsdlUrl.
     * @param wsdlUrl the String to set
     */
    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    private QName getPortQName()
    {
        return getQName(portNamespaceUri, portLocalPart, portPrefix);
    }

    private QName getQName( String namespaceUri, String localPart, String prefix )
    {
        if( namespaceUri != null && localPart != null && prefix != null )
        {
            return new QName(namespaceUri, localPart, prefix);
        }
        else if( namespaceUri != null && localPart != null )
        {
            return new QName(namespaceUri, localPart);
        }
        else if( localPart != null )
        {
            return new QName(localPart);
        }
        return null;
    }

    private QName getServiceQName()
    {
        return getQName(serviceNamespaceUri, serviceLocalPart, servicePrefix);
    }
}

