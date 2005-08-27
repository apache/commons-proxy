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
package org.apache.commons.proxy.provider.cache;

import java.util.LinkedList;
import java.util.Map;

/**
 * @author James Carman
 * @version 1.0
 */
public abstract class AbstractCache implements Cache
{
    protected abstract Map<Object,CachedObject> getCachedObjectMap();

    public void storeObject( Object key, Object value )
    {
        getCachedObjectMap().put( key, new CachedObject( value ) );
    }

    public void storeObject( Object key, Object value, CacheEvictionListener listener )
    {
        getCachedObjectMap().put( key, new CachedObject( value, listener ) );
    }

    public Object retrieveObject( Object key )
    {
        CachedObject cachedObject = getCachedObjectMap().get( key );
        return cachedObject == null ? null : cachedObject.getObject();
    }

    public void clearCache()
    {
        for( Object cacheKey: new LinkedList<Object>( getCachedObjectMap().keySet() ) )
        {
            final CachedObject cachedObject = getCachedObjectMap().get( cacheKey );
            if( cachedObject != null )
            {
                getCachedObjectMap().remove( cacheKey );
                if( cachedObject.getListener() != null )
                {
                    cachedObject.getListener().objectEvicted( new CacheEvictionEvent( cacheKey, cachedObject.getObject() ) );
                }
            }
        }
    }


}
