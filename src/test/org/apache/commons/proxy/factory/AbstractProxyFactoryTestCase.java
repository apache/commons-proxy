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
package org.apache.commons.proxy.factory;

import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.provider.BeanProvider;
import org.apache.commons.proxy.provider.SingletonProvider;
import org.apache.commons.proxy.util.AbstractTestCase;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;
import org.apache.commons.proxy.util.SuffixMethodInterceptor;

/**
 * @author James Carman
 * @version 1.0
 */
public abstract class AbstractProxyFactoryTestCase extends AbstractTestCase
{
    private final ProxyFactory factory;

    protected AbstractProxyFactoryTestCase( ProxyFactory factory )
    {
        this.factory = factory;
    }

    public void testCreateProxy()
    {
        final Echo echo = ( Echo )factory.createProxy( new SingletonProvider<Echo>( new BeanProvider<Echo>( EchoImpl.class ) ), Echo.class );
        assertEquals( "message", echo.echoBack( "message" ) );
    }

    public void testCreateInterceptorProxy()
    {
        final Echo target = ( Echo )factory.createProxy( new SingletonProvider<Echo>( new BeanProvider<Echo>( EchoImpl.class ) ), Echo.class );
        final Echo proxy = ( Echo )factory.createInterceptorProxy( target, new SuffixMethodInterceptor( " suffix" ), Echo.class );
        assertEquals( "message suffix", proxy.echoBack( "message" ) );
    }
}
