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
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.proxy.provider.cache.SimpleCache;
import org.apache.commons.proxy.provider.cache.ThreadLocalCache;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;

import java.util.concurrent.CountDownLatch;
import static org.apache.commons.proxy.provider.ProviderUtils.constantProvider;

public class TestPooledProvider extends TestCase
{
    public void testWithSimpleCache()
    {
        final CountingProvider counter = new CountingProvider( constantProvider( new EchoImpl() ) );
        final PooledProvider provider = new PooledProvider( counter );
        final SimpleCache cache = new SimpleCache();
        provider.setCache( cache );
        for( int i = 0; i < 10; ++i )
        {
            ( ( Echo )provider.getDelegate() ).echoBack( "Hello, World" );
            cache.clearCache();
        }
        assertEquals( 1, counter.getCount() );

    }

    public void testWithThreadLocalCache() throws Exception
    {
        final CountingProvider counter = new CountingProvider( constantProvider( new EchoImpl() ) );
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
        final CountDownLatch goLatch = new CountDownLatch( 1 );
        final CountDownLatch borrowedLatch = new CountDownLatch( 10 );
        final CountDownLatch finished = new CountDownLatch( 10 );
        for( int i = 0; i < 10; ++i )
        {
            new Thread( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        ( ( Echo )provider.getDelegate() ).echoBack( "Hello, World" );
                        borrowedLatch.countDown();
                        goLatch.await();
                        for( int i = 0; i < 10; ++i )
                        {
                            ( ( Echo )provider.getDelegate() ).echoBack( "Hello, World" );

                        }
                        cache.clearCache();
                        finished.countDown();
                    }
                    catch( InterruptedException e )
                    {
                    }
                }
            } ).start();
        }
        borrowedLatch.await();
        goLatch.countDown();
        finished.await();
        assertEquals( 10, counter.getCount() );
    }
}