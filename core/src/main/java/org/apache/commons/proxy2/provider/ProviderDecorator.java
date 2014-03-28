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
 * Returns the result of the inner {@link ObjectProvider provider}. Subclasses can override the {@link #getObject()}
 * method and decorate what comes back from the inner provider in some way (by {@link SingletonProvider caching it} for
 * example).
 * 
 * @since 1.0
 */
public class ProviderDecorator<T> implements ObjectProvider<T>
{
    //******************************************************************************************************************
    // Fields
    //******************************************************************************************************************

    private static final long serialVersionUID = 1L;

    /**
     * The wrapped {@link ObjectProvider}.
     */
    private ObjectProvider<? extends T> inner;

    //******************************************************************************************************************
    // Constructors
    //******************************************************************************************************************

    /**
     * Create a new ProviderDecorator instance.
     * 
     * @param inner
     */
    public ProviderDecorator(ObjectProvider<? extends T> inner)
    {
        this.inner = inner;
    }

    //******************************************************************************************************************
    // ObjectProvider Implementation
    //******************************************************************************************************************

    /**
     * {@inheritDoc}
     */
    @Override
    public T getObject()
    {
        return inner.getObject();
    }

    //******************************************************************************************************************
    // Getter/Setter Methods
    //******************************************************************************************************************

    protected ObjectProvider<? extends T> getInner()
    {
        return inner;
    }

    public void setInner(ObjectProvider<? extends T> inner)
    {
        this.inner = inner;
    }
}
