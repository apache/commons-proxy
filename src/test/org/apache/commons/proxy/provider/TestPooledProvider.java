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
        final PooledProvider provider = new PooledProvider( counter );
        provider.setMaxActive( 10 );
        provider.setMinIdle( 5 );
        provider.setWhenExhaustedAction( GenericObjectPool.WHEN_EXHAUSTED_GROW );
        provider.setMaxWait( 1000 );
        provider.setMinEvictableIdleTimeMillis( 10000 );
        provider.setTestOnBorrow( false );
        provider.setTestOnReturn( false );
        provider.setTestWhileIdle( false );
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
}