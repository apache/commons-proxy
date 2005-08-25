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
import org.apache.commons.proxy.util.QuoteService;

public class TestJaxRpcProvider extends TestCase
{
    public void testGetObject() throws Exception
    {
        final JaxRpcProvider provider = new JaxRpcProvider<QuoteService>( QuoteService.class );
        provider.setWsdlUrl( "http://services.xmethods.net/soap/urn:xmethods-delayed-quotes.wsdl" );
        provider.setServiceNamespaceUri( "http://www.themindelectric.com/wsdl/net.xmethods.services.stockquote.StockQuote/" );
        provider.setServiceLocalPart( "net.xmethods.services.stockquote.StockQuoteService" );
        provider.setPortLocalPart( "net.xmethods.services.stockquote.StockQuotePort" );
        final QuoteService quote = ( QuoteService )provider.getObject();
        assertNotNull( quote );
    }
}