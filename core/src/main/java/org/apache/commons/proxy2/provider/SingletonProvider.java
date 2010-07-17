/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.proxy2.provider;

import org.apache.commons.proxy2.ObjectProvider;

/**
 * Wraps another object provider, making sure to only call it once, returning the value returned from the wrapped
 * provider on all subsequent invocations.
 *
 * @author James Carman
 * @since 1.0
 */
public class SingletonProvider<T> extends ProviderDecorator<T>
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private T instance;

//**********************************************************************************************************************
// Constructors
//**********************************************************************************************************************

    public SingletonProvider( ObjectProvider<T> inner )
    {
        super(inner);
    }

//**********************************************************************************************************************
// ObjectProvider Implementation
//**********************************************************************************************************************

    public T getObject()
    {
        synchronized( this )
        {
            if( instance == null )
            {
                instance = super.getObject();
                inner = null; // Garbage collection
            }
        }
        return instance;
    }
}
