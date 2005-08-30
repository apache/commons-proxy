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

import org.apache.commons.proxy.DelegateProvider;

/**
 * Wraps another delegate provider, making sure to only call it once, returning the value returned from the wrapped
 * provider on all subsequent invocations.
 *
 * @author James Carman
 * @version 1.0
 */
public class SingletonProvider extends ProviderDecorator
{
    private Object instance;

    public SingletonProvider( DelegateProvider inner )
    {
        super( inner );
    }

    public Object getDelegate()
    {
        synchronized( this )
        {
            if( instance == null )
            {
                instance = super.getDelegate();
                inner = null; // Garbage collection
            }
        }
        return instance;
    }
}
