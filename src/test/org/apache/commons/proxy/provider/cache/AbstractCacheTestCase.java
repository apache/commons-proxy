/* $Id$
 *
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.proxy.provider.cache;

import junit.framework.TestCase;

/**
 * @author James Carman
 * @version 1.0
 */
public abstract class AbstractCacheTestCase extends TestCase
{
//----------------------------------------------------------------------------------------------------------------------
// Abstract Methods
//----------------------------------------------------------------------------------------------------------------------

    protected abstract Cache createCache();

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public void testEviction()
    {
        final Cache cache = createCache();
        final CacheEvictionTester tester = new CacheEvictionTester();
        cache.storeObject( "hello", "world", tester );
        cache.clearCache();
        assertNull( cache.retrieveObject( "hello" ) );
    }

    public void testEvictionWithListener()
    {
        final Cache cache = createCache();
        final CacheEvictionTester tester = new CacheEvictionTester();
        cache.storeObject( "hello", "world", tester );
        cache.clearCache();
        assertNull( cache.retrieveObject( "hello" ) );
        assertNotNull( tester.event );
        assertEquals( "world", tester.event.getEvictedObject() );
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private static class CacheEvictionTester implements CacheEvictionListener
    {
        private CacheEvictionEvent event;

        public void objectEvicted( CacheEvictionEvent e )
        {
            event = e;
        }
    }
}

