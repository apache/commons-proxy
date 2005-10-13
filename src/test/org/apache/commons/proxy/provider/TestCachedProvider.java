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
import org.apache.commons.proxy.provider.cache.SimpleCache;
import org.apache.commons.proxy.provider.cache.ThreadLocalCache;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;
import EDU.oswego.cs.dl.util.concurrent.CountDown;

public class TestCachedProvider extends TestCase
{
    public void testWithSimpleCache()
    {
        final CountingProvider counter = new CountingProvider( new ConstantProvider( new EchoImpl() ) );
        final CachedProvider provider = new CachedProvider( counter );
        provider.setCache( new SimpleCache() );
        for( int i = 0; i < 10; ++i )
        {
           ( ( Echo )provider.getObject() ).echoBack( "Hello, World" );
        }
        assertEquals( 1, counter.getCount() );
    }

    public void testWithThreadLocalCache() throws Exception
    {
        final CountingProvider counter = new CountingProvider( new ConstantProvider( new EchoImpl() ) );
        final CachedProvider provider = new CachedProvider( counter );
        final ThreadLocalCache cache = new ThreadLocalCache();
        provider.setCache( cache );
        final CountDown latch = new CountDown( 10 );
        for( int i = 0; i < 10; ++i )
        {
            new Thread( new Runnable()
            {
                public void run()
                {
                    ( ( Echo )provider.getObject() ).echoBack( "Hello, World" );
                    cache.clearCache();
                    latch.release();
                }
            }).start();
        }
        latch.acquire();
        assertEquals( 10, counter.getCount() );
    }
}