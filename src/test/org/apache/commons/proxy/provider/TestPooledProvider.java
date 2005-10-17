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

import EDU.oswego.cs.dl.util.concurrent.CountDown;
import junit.framework.TestCase;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.proxy.provider.cache.SimpleCache;
import org.apache.commons.proxy.provider.cache.ThreadLocalCache;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.ProxyUtils;
import org.apache.commons.proxy.exception.ObjectProviderException;

public class TestPooledProvider extends TestCase
{
    public void testWithSimpleCache()
    {
        final CountingProvider counter = new CountingProvider( ProviderUtils.constantProvider( new EchoImpl() ) );
        final PooledProvider provider = new PooledProvider( counter );
        final SimpleCache cache = new SimpleCache();
        provider.setCache( cache );
        for( int i = 0; i < 10; ++i )
        {
            ( ( Echo )provider.getObject() ).echoBack( "Hello, World" );
            cache.clearCache();
        }
        assertEquals( 1, counter.getCount() );

    }

    public void testWithThreadLocalCache() throws Exception
    {
        final CountingProvider counter = new CountingProvider( ProviderUtils.constantProvider( new EchoImpl() ) );
        final GenericObjectPool.Config config = new GenericObjectPool.Config();
        config.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;
        final PooledProvider provider = new PooledProvider( counter, config );
        final ThreadLocalCache cache = new ThreadLocalCache();
        provider.setCache( cache );
        final CountDown goLatch = new CountDown( 1 );
        final CountDown borrowedLatch = new CountDown( 10 );
        final CountDown finished = new CountDown( 10 );
        for( int i = 0; i < 10; ++i )
        {
            new Thread( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        ( ( Echo )provider.getObject() ).echoBack( "Hello, World" );
                        borrowedLatch.release();
                        goLatch.acquire();
                        for( int i = 0; i < 10; ++i )
                        {
                            ( ( Echo )provider.getObject() ).echoBack( "Hello, World" );

                        }
                        cache.clearCache();
                        finished.release();
                    }
                    catch( InterruptedException e )
                    {
                    }
                }
            } ).start();
        }
        borrowedLatch.acquire();
        goLatch.release();
        finished.acquire();
        assertEquals( 10, counter.getCount() );
    }

    public void testWithExceptionFromInner()
    {
        final PooledProvider provider = new PooledProvider( new ExceptionProvider() );
                final SimpleCache cache = new SimpleCache();
                provider.setCache( cache );

        final Echo echo = ( Echo )ProxyUtils.getProxyFactory().createDelegatorProxy( provider, new Class[] { Echo.class } );
        try
        {
            echo.echoBack( "Hello." );
            fail();
        }
        catch( ObjectProviderException e )
        {

        }
    }
    private class ExceptionProvider implements ObjectProvider
    {
        public Object getObject()
        {
            throw new ObjectProviderException( "Ha ha!" );
        }
    }
}