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
package org.apache.commons.proxy.provider;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.exception.ObjectProviderException;
import org.apache.commons.proxy.provider.cache.Cache;
import org.apache.commons.proxy.provider.cache.CacheEvictionEvent;
import org.apache.commons.proxy.provider.cache.CacheEvictionListener;

/**
 * @author James Carman
 * @version 1.0
 */
public class PooledProvider extends ProviderDecorator implements CacheEvictionListener
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final Object cacheKey = new Object();
    private final GenericObjectPool pool;
    private Cache cache;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public PooledProvider( ObjectProvider inner )
    {
        super( inner );
        pool = new GenericObjectPool( new Factory() );
    }

//----------------------------------------------------------------------------------------------------------------------
// CacheEvictionListener Implementation
//----------------------------------------------------------------------------------------------------------------------

    public void objectEvicted( CacheEvictionEvent e )
    {
        try
        {
            log.debug( "Returning object to pool in thread " + Thread.currentThread().getName() + "..." );
            pool.returnObject( e.getEvictedObject() );
        }
        catch( Exception e1 )
        {
            // Do nothing.
        }
    }

//----------------------------------------------------------------------------------------------------------------------
// ObjectProvider Implementation
//----------------------------------------------------------------------------------------------------------------------

    public Object getObject()
    {
        try
        {
            log.debug( "Checking for object in cache in thread " + Thread.currentThread().getName() + "..." );
            Object object = cache.retrieveObject( cacheKey );
            if( object == null )
            {
                log.debug( "Did not object in cache; borrowing from pool in thread " +
                           Thread.currentThread().getName() + "..." );
                object = pool.borrowObject();
                cache.storeObject( cacheKey, object, this );
            }
            return object;
        }
        catch( Exception e )
        {
            throw new ObjectProviderException( "Unable to borrow object from pool.", e );
        }
    }

//----------------------------------------------------------------------------------------------------------------------
// Getter/Setter Methods
//----------------------------------------------------------------------------------------------------------------------

    public void setCache( Cache cache )
    {
        this.cache = cache;
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public void setMaxActive( int i )
    {
        pool.setMaxActive( i );
    }

    public void setMaxIdle( int i )
    {
        pool.setMaxIdle( i );
    }

    public void setMaxWait( long l )
    {
        pool.setMaxWait( l );
    }

    public void setMinEvictableIdleTimeMillis( long l )
    {
        pool.setMinEvictableIdleTimeMillis( l );
    }

    public void setMinIdle( int i )
    {
        pool.setMinIdle( i );
    }

    public void setNumTestsPerEvictionRun( int i )
    {
        pool.setNumTestsPerEvictionRun( i );
    }

    public void setTestOnBorrow( boolean b )
    {
        pool.setTestOnBorrow( b );
    }

    public void setTestOnReturn( boolean b )
    {
        pool.setTestOnReturn( b );
    }

    public void setTestWhileIdle( boolean b )
    {
        pool.setTestWhileIdle( b );
    }

    public void setTimeBetweenEvictionRunsMillis( long l )
    {
        pool.setTimeBetweenEvictionRunsMillis( l );
    }

    public void setWhenExhaustedAction( byte b )
    {
        pool.setWhenExhaustedAction( b );
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private class Factory extends BasePoolableObjectFactory
    {
        public Object makeObject() throws Exception
        {
            log.debug( "Creating new object for pool in thread " + Thread.currentThread().getName() + "..." );
            return inner.getObject();
        }
    }
}

