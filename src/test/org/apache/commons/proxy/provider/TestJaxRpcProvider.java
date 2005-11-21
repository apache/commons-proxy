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
    public void testGetObject() throws Exception
    {
        final JaxRpcProvider provider = new JaxRpcProvider( QuoteService.class );
        provider.setWsdlUrl( "http://services.xmethods.net/soap/urn:xmethods-delayed-quotes.wsdl" );
        provider.setServiceNamespaceUri(
                "http://www.themindelectric.com/wsdl/net.xmethods.services.stockquote.StockQuote/" );
        provider.setServiceLocalPart( "net.xmethods.services.stockquote.StockQuoteService" );
        provider.setServicePrefix( "" );
        provider.setPortNamespaceUri(
                "http://www.themindelectric.com/wsdl/net.xmethods.services.stockquote.StockQuote/" );
        provider.setPortLocalPart( "net.xmethods.services.stockquote.StockQuotePort" );
        provider.setPortPrefix( "" );
        final QuoteService quote = ( QuoteService ) provider.getObject();
        assertNotNull( quote );
    }

    public void testGetObjectWithoutPrefix() throws Exception
    {
        final JaxRpcProvider provider = new JaxRpcProvider( QuoteService.class );
        provider.setWsdlUrl( "http://services.xmethods.net/soap/urn:xmethods-delayed-quotes.wsdl" );
        provider.setServiceNamespaceUri(
                "http://www.themindelectric.com/wsdl/net.xmethods.services.stockquote.StockQuote/" );
        provider.setServiceLocalPart( "net.xmethods.services.stockquote.StockQuoteService" );
        provider.setPortNamespaceUri(
                "http://www.themindelectric.com/wsdl/net.xmethods.services.stockquote.StockQuote/" );
        provider.setPortLocalPart( "net.xmethods.services.stockquote.StockQuotePort" );
        final QuoteService quote = ( QuoteService ) provider.getObject();
        assertNotNull( quote );
    }

    public void testGetObjectWithInvalidUrl() throws Exception
    {
        final JaxRpcProvider provider = new JaxRpcProvider( QuoteService.class );
        provider.setWsdlUrl( "yadda yadda yadda" );
        try
        {
            final QuoteService quote = ( QuoteService ) provider.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
            assertTrue( e.getCause() instanceof MalformedURLException );
        }
    }
}