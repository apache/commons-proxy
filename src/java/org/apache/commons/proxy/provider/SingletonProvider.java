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

import org.apache.commons.proxy.ObjectProvider;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Wraps another object provider, making sure to only call it once, returning the value
 * returned from the wrapped provider on all subsequent invocations.
 *
 * @author James Carman
 * @version 1.0
 */
public class SingletonProvider<T> extends ProviderDecorator<T>
{
    private T instance;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    public SingletonProvider( ObjectProvider<? extends T> inner )
    {
        super( inner );
    }

    public T getObject()
    {
        rwl.readLock().lock();
        if( instance == null )
        {
            rwl.readLock().unlock();
            rwl.writeLock().lock();
            if( instance == null )
            {
                instance = super.getObject();
                inner = null; // Garbage collection
            }
            rwl.readLock().lock();
            rwl.writeLock().unlock();
        }
        rwl.readLock().unlock();
        return instance;
    }
}
