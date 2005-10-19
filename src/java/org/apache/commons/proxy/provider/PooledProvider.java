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
 * Uses <a href="http://jakarta.apache.org/commons/pool/">Jakarta Commons Pool</a> to maintain a pool of target
 * objects provided by an <code>inner</code> object provider.
 *
 * <p>
 * <b>Dependencies</b>:
 * <ul>
 *   <li>Jakarta Commons Pool version 1.2 or greater</li>
 * </ul>
 * </p>
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
        this( inner, new GenericObjectPool.Config() );
    }

    public PooledProvider( ObjectProvider inner, GenericObjectPool.Config config )
    {
        super( inner );
        pool = new GenericObjectPool( new Factory(), config );
    }

//----------------------------------------------------------------------------------------------------------------------
// CacheEvictionListener Implementation
//----------------------------------------------------------------------------------------------------------------------

    public void objectEvicted( CacheEvictionEvent e )
    {
        try
        {
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
            Object object = cache.retrieveObject( cacheKey );
            if( object == null )
            {
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
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private class Factory extends BasePoolableObjectFactory
    {
        public Object makeObject() throws Exception
        {
            return inner.getObject();
        }
    }
}

