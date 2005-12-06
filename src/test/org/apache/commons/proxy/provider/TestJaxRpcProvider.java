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
import org.apache.commons.proxy.util.QuoteService;

import java.net.MalformedURLException;

public class TestJaxRpcProvider extends TestCase
{
    private static final String WSDL_URL = "http://services.xmethods.net/soap/urn:xmethods-delayed-quotes.wsdl";
    private static final String NAMESPACE_URI =
            "http://www.themindelectric.com/wsdl/net.xmethods.services.stockquote.StockQuote/";
    private static final String SERVICE_LOCAL_PART = "net.xmethods.services.stockquote.StockQuoteService";
    private static final String PORT_LOCAL_PART = "net.xmethods.services.stockquote.StockQuotePort";

    public void testGetObject() throws Exception
    {
        final JaxRpcProvider provider = new JaxRpcProvider();
        provider.setServiceInterface( QuoteService.class );
        provider.setWsdlUrl( WSDL_URL );
        provider.setServiceNamespaceUri( NAMESPACE_URI );
        provider.setServiceLocalPart( SERVICE_LOCAL_PART );
        provider.setServicePrefix( "" );
        provider.setPortNamespaceUri( NAMESPACE_URI );
        provider.setPortLocalPart( PORT_LOCAL_PART );
        provider.setPortPrefix( "" );
        final QuoteService quote = ( QuoteService ) provider.getObject();
        assertNotNull( quote );
    }

    public void testGetObjectWithUnspecifiedPort() throws Exception
    {
        final JaxRpcProvider provider = new JaxRpcProvider( QuoteService.class );
        provider.setWsdlUrl( WSDL_URL );
        provider.setServiceNamespaceUri( NAMESPACE_URI );
        provider.setServiceLocalPart( SERVICE_LOCAL_PART );
        provider.setServicePrefix( "" );
        final QuoteService quote = ( QuoteService ) provider.getObject();
        assertNotNull( quote );
    }

    public void testGetObjectWithoutWsdl() throws Exception
    {
        final JaxRpcProvider provider = new JaxRpcProvider( QuoteService.class );
        provider.setServiceNamespaceUri( NAMESPACE_URI );
        provider.setServiceLocalPart( SERVICE_LOCAL_PART );
        provider.setServicePrefix( "" );
        provider.setPortNamespaceUri( NAMESPACE_URI );
        provider.setPortLocalPart( PORT_LOCAL_PART );
        provider.setPortPrefix( "" );
        try
        {
            provider.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
        }
    }

    public void testGetObjectWithoutPrefix() throws Exception
    {
        final JaxRpcProvider provider = new JaxRpcProvider( QuoteService.class );
        provider.setWsdlUrl( WSDL_URL );
        provider.setServiceNamespaceUri( NAMESPACE_URI );
        provider.setServiceLocalPart( SERVICE_LOCAL_PART );
        provider.setPortNamespaceUri( NAMESPACE_URI );
        provider.setPortLocalPart( PORT_LOCAL_PART );
        final QuoteService quote = ( QuoteService ) provider.getObject();
        assertNotNull( quote );
    }

    public void testGetObjectWithoutPrefixOrNamespaceUri() throws Exception
    {
        final JaxRpcProvider provider = new JaxRpcProvider( QuoteService.class );
        provider.setWsdlUrl( WSDL_URL );
        provider.setServiceLocalPart( SERVICE_LOCAL_PART );
        provider.setPortLocalPart( PORT_LOCAL_PART );
        try
        {
            provider.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
        }
    }

    public void testGetObjectWithJustWsdl()
    {
        final JaxRpcProvider provider = new JaxRpcProvider( QuoteService.class );
        provider.setWsdlUrl( WSDL_URL );
        try
        {
            provider.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
        }
    }

    public void testGetObjectWithoutPrefixOrLocalPart() throws Exception
    {
        final JaxRpcProvider provider = new JaxRpcProvider( QuoteService.class );
        provider.setWsdlUrl( WSDL_URL );
        provider.setServiceNamespaceUri( NAMESPACE_URI );
        provider.setPortNamespaceUri( NAMESPACE_URI );
        try
        {
            provider.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
        }
    }

    public void testGetObjectWithInvalidUrl() throws Exception
    {
        final JaxRpcProvider provider = new JaxRpcProvider( QuoteService.class );
        provider.setWsdlUrl( "yadda yadda yadda" );
        try
        {
            provider.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
            assertTrue( e.getCause() instanceof MalformedURLException );
        }
    }
}